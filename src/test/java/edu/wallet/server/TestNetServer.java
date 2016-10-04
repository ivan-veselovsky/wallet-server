package edu.wallet.server;

import edu.wallet.client.*;
import edu.wallet.config.*;
import edu.wallet.server.db.*;
import edu.wallet.server.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 */
public class TestNetServer extends Base {

    @Test
    public void serverBasic() throws Exception {
        Cfg cfg = Cfg.getEntryBean();

        IPersistentStorage ps = cfg.getPersistentStorage();

        ps.clear();

        ps.save(Collections.singleton(new ValueObject("ivan", 500, 28)));

        try (final NetServer server = new NetServer(cfg)) {
            server.start();

            Client client = new Client();

            String actualResponse = client.send("{\"transactionId\":12345,\"balanceChange\":-499,\"user\":\"ivan\"}\n");

            assertEquals("{\"transactionId\":12345,\"outgoingBalance\":1,\"balanceChange\":-499,\"errorCode\":0," + "\"balanceVersion\":29}", actualResponse);
        }
    }

    @Test
    public void serverConcurrentRequests() throws Exception {
        final int clientThreads = 40;
        final int consequentRequests = 2500;

        final int duplicateDistance = 4;

        // [!!!] Note: this opetion makes the test flaky.
        // Problem is that currently we cannot guarantee that the duplicated request
        // really stays in history -- it can be evicted.
        // Please bear that in mind while analyzing failures of this test.
        // This option can be switched off for better reliability.
        final boolean testingDuplicates = true;

        final Cfg cfg = Cfg.getEntryBean();

        final IConfiguration c = cfg.getConfiguration();

        cfg.getPersistentStorage().clear();

        final int iniBal = 500;
        final int iniBalVersion = 28;

        List<ValueObject> list = new ArrayList<>(clientThreads);

        for (int i = 0; i < clientThreads; i++) {
            final String u = "ivan" + i;
            list.add(new ValueObject(u, iniBal, iniBalVersion));
        }

        int upd = cfg.getPersistentStorage().save(list);

        assertEquals(clientThreads, upd);

        try (final NetServer server = new NetServer(cfg)) {
            server.start();

            final AtomicReference<Throwable> th = new AtomicReference<>();

            final ExecutorService svc = Executors.newFixedThreadPool(clientThreads);

            final AtomicLong txId = new AtomicLong(12345);

            for (int i = 0; i < clientThreads; i++) {
                final int threadIndex = i;

                Runnable r = new Runnable() {
                    @Override public void run() {
                        try {
                            int bal = iniBal;
                            long balVersion = iniBalVersion;

                            Request dupRq = null;
                            String dupExpected = null;
                            int dupDelta = -1;

                            try (Client client = new Client()) {
                                for (int j = 0; j < consequentRequests; j++) {
                                    if (th.get() != null) {
                                        break;
                                    }

                                    if (dupDelta > 0) {
                                        dupDelta--; // dup count down
                                    }

                                    if (testingDuplicates && dupDelta == 0) {
                                        // re-send a duplicate and expect the same response:
                                        assert dupRq != null;
                                        assert dupExpected != null;

                                        System.out.println("sending dup: " + dupRq);

                                        String actualResponse = client.send(dupRq.serialize());

                                        assertEquals(dupExpected, actualResponse);

                                        dupExpected = null;
                                        dupRq = null;
                                        dupDelta = -1;
                                    }
                                    else {
                                        final String u = "ivan" + threadIndex;
                                        long tx = txId.incrementAndGet();
                                        int balChange = j <= c.getMaxBalanceChange() ? j : c.getMaxBalanceChange();

                                        Request rq = new Request(u, tx, balChange);

                                        String actualResponse = client.send(rq.serialize());

                                        bal += balChange;
                                        balVersion++;

                                        String expected = "{\"transactionId\":" + tx + ",\"outgoingBalance\":" + bal + ",\"balanceChange\":" + balChange + ",\"errorCode\":0," + "\"balanceVersion\":" + balVersion + "}";


                                        assertEquals(expected, actualResponse);

                                        if (testingDuplicates && dupDelta == -1 && (j % 11) == 0) {
                                            // remember a request to duplicate it later:
                                            dupExpected = expected;
                                            dupRq = rq;
                                            dupDelta = duplicateDistance;
                                        }
                                    }
                                }
                            }
                        }
                        catch (Throwable t) {
                            t.printStackTrace();

                            th.compareAndSet(null, t);
                        }
                    }
                };

                svc.submit(r);
            }

            svc.shutdown();
            svc.awaitTermination(3 * 60, TimeUnit.SECONDS); // Timeout of 3 minutes.

            assertNull(th.get());
        }
    }

}
