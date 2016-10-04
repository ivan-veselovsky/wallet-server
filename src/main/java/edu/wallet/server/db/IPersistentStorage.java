package edu.wallet.server.db;

import edu.wallet.server.*;
import java.io.*;
import java.util.*;

/**
 * Decouples persistent storage from model and transport.
 * Note: operations may be long-running.
 */
public interface IPersistentStorage {
    /**
     * As parameter suggests, implies some batch-processing.
     * Values that exist assumed to be replaced.
     * @param voCollection The collection to save persistently.
     * @return How many objects were really written (equal objects may be skipped).
     * @throws IOException
     */
    int save(Collection<ValueObject> voCollection) throws IOException;

    /**
     * Retrieves data (1 cortege) from persistent storage.
     * @param userNameKey The key.
     * @return The value object, or null, if no such object exists.
     */
    ValueObject retrieve(String userNameKey);
}
