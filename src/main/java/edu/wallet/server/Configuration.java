package edu.wallet.server;

import java.util.*;

/**
 * Represents a read-only configuration.
 */
public class Configuration {

    private static final int DEFAULT_MAX_HISTORY = 1000;

    public int getMaxHistory() {
        return DEFAULT_MAX_HISTORY;
    }

    public int getNumThreads() {
        return 20;
    }

    /**
     * TODO: take from ext source
     * @return
     */
    public Set<String> getBlackList() {
        return Collections.emptySet();
    }

    /**
     *
     * @return
     */
    public int getMaxBalanceChange() {
        return 500;
    }


    public int getDbWritePeriodSec() {
        return 45;
    }


    public int getServerPort() {
        return 8888;
    }

//    public String getServerHost() {
//        return null;
//    }

}
