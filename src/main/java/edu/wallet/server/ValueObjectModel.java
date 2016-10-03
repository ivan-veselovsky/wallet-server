package edu.wallet.server;

import edu.wallet.server.model.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 *
 */
public class ValueObjectModel {

    private final LazyConcurrentMap<String, AtomicReference<ValueObject>> lazyMap;

    ValueObjectModel() {
        LazyConcurrentMap.ValueFactory<String, AtomicReference<ValueObject>> fac
            = new LazyConcurrentMap.ValueFactory<String, AtomicReference<ValueObject>>() {
            @Override public AtomicReference<ValueObject> createValue(String key) throws IOException {
                // This may be heavy operation since it accesses DB:
                ValueObject vo = getFromDB(key);

                if (vo == null) {
                    vo = new ValueObject(key/*user name*/, 0/*initial account balance*/, 0L/*version*/);
                }
                // Take the value from DB by user name
                return new AtomicReference<>(vo);
            }
        };

        lazyMap = new LazyConcurrentMap<>(fac, new ConcurrentHashMap<String, Object>());
    }

    ValueObject getFromDB(String userName) {
        // TODO: gets from DB, null if no such user exists.
        return null;
    }
}
