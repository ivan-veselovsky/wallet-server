package edu.wallet.server;

import edu.wallet.config.*;
import java.util.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 */
public class TestConfig {

    @Test
    public void configFromProperties() {
        Cfg cfg = Cfg.getEntryBean();

        IConfiguration c = cfg.getConfiguration();

        assertEquals(new HashSet<String>() {{ add("vasya"); add("vova"); }}, c.getBlackList());
        assertEquals(45, c.getDbWritePeriodSec());
        assertEquals(500, c.getMaxBalanceChange());
        assertEquals(1000, c.getMaxHistory());
        assertEquals(20, c.getNumThreads());
        assertEquals(8888, c.getServerPort());
    }

}
