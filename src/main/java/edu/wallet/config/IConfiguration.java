package edu.wallet.config;

import java.util.*;

/**
 * Configuration
 */
public interface IConfiguration {
    /**
     * Max Tx id History size.
     * @return
     */
    int getMaxHistory();

    /**
     * Number of server threads.
     * @return
     */
    int getNumThreads();

    /**
     * Black list.
     * @return
     */
    Set<String> getBlackList();

    /**
     * Max balance change.
     * @return
     */
    int getMaxBalanceChange();

    /**
     * Db write period.
     * @return
     */
    int getDbWritePeriodSec();

    /**
     * Server port.
     * @return
     */
    int getServerPort();
}

