package edu.wallet.server;

import edu.wallet.client.*;
import edu.wallet.server.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 */
public class TestNetServer {

    @Test
    public void serverBasic() throws Exception {
        ILogger logger = SystemOutLogger.instance();

        Configuration c = new Configuration();

        IProcessor proc = new LogicServer(c, logger) {
            @Override ValueObject getFromDB(String userName) {
                return new ValueObject(userName, 500, 28);
            }
        };

        NetServer server = new NetServer(proc, logger, c);

        server.start();

        Client client = new Client();

        String actualResponse = client.send("{\"transactionId\":12345,\"balanceChange\":-499,\"user\":\"ivan\"}\n");

        assertEquals("{\"transactionId\":12345,\"outgoingBalance\":1,\"balanceChange\":-499,\"errorCode\":0," +
            "\"balanceVersion\":29}", actualResponse);

        server.stop();
    }

    @Test
    public void serverConcurrentRequests() throws Exception {
        final int clientThreads = 40;
        final int consequentRequests = 2500;

        final boolean testingDuiplicates = true;

        final ILogger logger = SystemOutLogger.instance();

        final Configuration c = new Configuration();

        final int iniBal = 500;
        final int iniBalVersion = 28;

        final IProcessor proc = new LogicServer(c, logger) {
            @Override ValueObject getFromDB(String userName) {
                return new ValueObject(userName, iniBal, iniBalVersion);
            }
        };

        final NetServer server = new NetServer(proc, logger, c);

        server.start();

        final AtomicReference<Throwable> th = new AtomicReference<>();

        final ExecutorService svc =  Executors.newFixedThreadPool(clientThreads);

        final AtomicLong txId = new AtomicLong(12345);

        for (int i=0; i<clientThreads; i++) {
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
                            for (int j=0; j < consequentRequests; j++) {
                                if (th.get() != null) {
                                    break;
                                }

                                if (dupDelta > 0) {
                                    dupDelta--; // dup count down
                                }

                                if (testingDuiplicates && dupDelta == 0) {
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
                                    String u = "ivan" + threadIndex;
                                    long tx = txId.incrementAndGet();
                                    int balChange = j <= c.getMaxBalanceChange() ? j : c.getMaxBalanceChange();

                                    Request rq = new Request(u, tx, balChange);

                                    String actualResponse = client.send(rq.serialize());

                                    bal += balChange;
                                    balVersion++;

                                    String expected = "{\"transactionId\":" + tx + ",\"outgoingBalance\":" + bal
                                        + ",\"balanceChange\":" + balChange + ",\"errorCode\":0,"
                                        + "\"balanceVersion\":" + balVersion + "}";

                                    assertEquals(expected, actualResponse);

                                    if (testingDuiplicates && dupDelta == -1 && (j % 11) == 0) {
                                        dupExpected = expected;
                                        dupRq = rq;
                                        dupDelta = 10;
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
        svc.awaitTermination(3 * 60, TimeUnit.SECONDS);

        server.stop();

        assertNull(th.get());
    }

}
