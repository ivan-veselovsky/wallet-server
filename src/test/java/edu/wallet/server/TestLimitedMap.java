package edu.wallet.server;

import edu.wallet.server.model.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 */
public class TestLimitedMap extends Base {

    static class EvictableLong implements EvictableValue<Long> {
        @Override public void evicted(Long key) {
            // noop
        }
    }

    @Test
    public void limitNotExceeded() throws Exception {
        final int threads = 10;
        final int hardLimit = 100;

        final int numPutsInEachThread = 500_000;

        final Random rnd = new Random();

        ConcurrentMap<Long, EvictableLong> map = new ConcurrentHashMap<>();
        final LimitedConcurrentMap<Long, EvictableLong> lim = new LimitedConcurrentMap<>(map, hardLimit, threads);

        ExecutorService srv = Executors.newFixedThreadPool(threads);

        final AtomicInteger max = new AtomicInteger();

        final EvictableLong v = new EvictableLong();

        for (int i = 0; i<threads; i++) {
            srv.submit(new Runnable() {
                @Override public void run() {
                    int maxSize = 0;

                    for (int i = 0; i<numPutsInEachThread; i++) {
                        long k = rnd.nextLong();

                        lim.putIfAbsent(k, v);

                        int size = lim.size();

                        assertTrue(size <= hardLimit);

                        if (size > maxSize) {
                            maxSize = size;
                        }
                    }

                    System.out.println("Completed. Max size ever seen = " + maxSize);

                    while (true) {
                        int m = max.get();

                        if (maxSize > m) {
                            if (max.compareAndSet(m, maxSize)) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            });
        }

        srv.shutdown();
        assertTrue(srv.awaitTermination(60, TimeUnit.SECONDS));

        System.out.println("Completed All. Global max size ever seen = " + max.get());
    }
}
