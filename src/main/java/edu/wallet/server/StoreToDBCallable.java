package edu.wallet.server;

import edu.wallet.server.model.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 *
 */
public class StoreToDBCallable implements Runnable {

    private final LazyConcurrentMap<String, AtomicReference<ValueObject>> valueObjectLazyMap;

    private final Configuration cfg;


    StoreToDBCallable(LazyConcurrentMap<String, AtomicReference<ValueObject>> m, Configuration c) {
        valueObjectLazyMap = m;
        cfg = c;
    }


    @Override public void run() {
        Collection<AtomicReference<ValueObject>> values = valueObjectLazyMap.values();

        for (AtomicReference<ValueObject> ref: values) {
            saveToDb(ref.get());
        }
    }

    void saveToDb(ValueObject vo) {
        // TODO
    }

    void start() {
        long periodSec = cfg.getDbWritePeriodSec();
        ScheduledExecutorService srvc = Executors.newSingleThreadScheduledExecutor();
        srvc.scheduleAtFixedRate(this, 0, periodSec, TimeUnit.SECONDS);
    }
}
