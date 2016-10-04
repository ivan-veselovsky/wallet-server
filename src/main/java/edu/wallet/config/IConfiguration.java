package edu.wallet.config;

import java.util.*;

/**
 * Configuration interface to allow pluggable config taken from an arbitrary source..
 * Example implementations are {@link DefaultConfiguration} and {@link PropertyFileConfiguration}.
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

