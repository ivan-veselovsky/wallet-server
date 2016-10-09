package edu.wallet.config;

import java.util.Collections;
import java.util.Set;

/**
 * Simple POJO bean with mutable properties.
 * Can be used as default configuration implementation, or
 * reused as a container in other implementations taking values from external sources.
 */
public class DefaultConfiguration implements IConfiguration {

    private int maxHistory = 1001;
    private int numThreads = 21;
    private Set<String> blackList = Collections.singleton("vasya");
    private int maxBalanceChange = 501;
    private int dbWritePeriodSec = 46;
    private int serverPort = 8889;

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public Set<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(Set<String> blackList) {
        this.blackList = blackList;
    }

    public int getMaxBalanceChange() {
        return maxBalanceChange;
    }

    public void setMaxBalanceChange(int maxBalanceChange) {
        this.maxBalanceChange = maxBalanceChange;
    }

    public int getDbWritePeriodSec() {
        return dbWritePeriodSec;
    }

    public void setDbWritePeriodSec(int dbWritePeriodSec) {
        this.dbWritePeriodSec = dbWritePeriodSec;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
