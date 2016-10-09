package edu.wallet.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Old-school property file implementation: takes the properties from
 * file named "config.properties found in the classpath.
 * (Used solely to demonstrate how to get configuration from the external source).
 */
public class PropertyFileConfiguration implements IConfiguration {
    private static final String fileName = "config.properties";

    private final DefaultConfiguration c = new DefaultConfiguration();

    public PropertyFileConfiguration() {
        init();
    }

    private void init() {
        final Properties p = new Properties();

        try {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
                if (is == null) {
                    throw new IllegalStateException("please make sure file [" + fileName
                            + "] is present in the application class path.");
                }

                p.load(is);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        String x;

        x = p.getProperty("blackList");
        if (x != null) {
            Set<String> b = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(x.split("\\s*,\\s*"))));
            c.setBlackList(b);
        }

        x = p.getProperty("dbWritePeriosSec");
        if (x != null) {
            c.setDbWritePeriodSec(Integer.parseInt(x));
        }

        x = p.getProperty("maxBalanceChange");
        if (x != null) {
            c.setMaxBalanceChange(Integer.parseInt(x));
        }

        x = p.getProperty("maxHistory");
        if (x != null) {
            c.setMaxHistory(Integer.parseInt(x));
        }

        x = p.getProperty("numThreads");
        if (x != null) {
            c.setNumThreads(Integer.parseInt(x));
        }

        x = p.getProperty("serverPort");
        if (x != null) {
            c.setServerPort(Integer.parseInt(x));
        }
    }

    @Override
    public Set<String> getBlackList() {
        return c.getBlackList();
    }

    @Override
    public int getMaxHistory() {
        return c.getMaxHistory();
    }

    @Override
    public int getNumThreads() {
        return c.getNumThreads();
    }

    @Override
    public int getMaxBalanceChange() {
        return c.getMaxBalanceChange();
    }

    @Override
    public int getDbWritePeriodSec() {
        return c.getDbWritePeriodSec();
    }

    @Override
    public int getServerPort() {
        return c.getServerPort();
    }
}
