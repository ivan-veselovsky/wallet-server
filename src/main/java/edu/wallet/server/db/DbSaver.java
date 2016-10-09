package edu.wallet.server.db;

import edu.wallet.config.IConfiguration;
import edu.wallet.log.ILogger;
import edu.wallet.server.ValueObject;
import edu.wallet.server.model.LazyConcurrentMap;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TODO: better tests for saver.
 */
public class DbSaver implements Closeable {

    private final LazyConcurrentMap<String, AtomicReference<ValueObject>> valueObjectLazyMap;

    private final IConfiguration configuration;

    private final ILogger logger;

    private final IPersistentStorage persistentStorage;

    private ScheduledExecutorService srvc;

    public DbSaver(LazyConcurrentMap<String, AtomicReference<ValueObject>> m, IConfiguration c, ILogger l, IPersistentStorage p) {
        valueObjectLazyMap = m;

        this.configuration = Objects.requireNonNull(c);
        this.logger = Objects.requireNonNull(l);
        this.persistentStorage = Objects.requireNonNull(p);
    }

    void runImpl() {
        try {
            Collection<AtomicReference<ValueObject>> values = valueObjectLazyMap.values();

            List<ValueObject> list = new ArrayList<>(values.size());

            for (AtomicReference<ValueObject> ref : values) {
                ValueObject vo = ref.get();

                assert vo != null; // LazyConcurrentMap contracts guarantee that.

                list.add(vo);
            }

            persistentStorage.save(list);
        } catch (Throwable t) {
            logger.error("In persistent save: ", t);
        }
    }

    /**
     * Schedules saving of the model to persistent storage.
     */
    public void start() {
        srvc = Executors.newSingleThreadScheduledExecutor();

        final long periodSec = configuration.getDbWritePeriodSec();

        srvc.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runImpl();
            }
        }, 0, periodSec, TimeUnit.SECONDS);

        logger.info("Saving scheduled with rate " + periodSec + " sec.", null);
    }

    public void close() throws IOException {
        ScheduledExecutorService svc = srvc;
        if (svc != null) {
            svc.shutdownNow();

            try {
                boolean clo = svc.awaitTermination(10, TimeUnit.SECONDS);

                if (!clo) {
                    throw new IOException("failed to close persistent component.");
                }
            } catch (InterruptedException ie) {
                throw new IOException(ie);
            }
        }
    }
}
