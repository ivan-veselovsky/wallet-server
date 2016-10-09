package edu.wallet.server;

import edu.wallet.config.Cfg;
import edu.wallet.config.IConfiguration;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TestConfig extends Base {

    @Test
    public void configFromProperties() {
        Cfg cfg = Cfg.getEntryBean();

        IConfiguration c = cfg.getConfiguration();

        assertEquals(new HashSet<String>() {{
            add("vasya");
            add("vova");
        }}, c.getBlackList());
        assertEquals(45, c.getDbWritePeriodSec());
        assertEquals(500, c.getMaxBalanceChange());
        assertEquals(1000, c.getMaxHistory());
        assertEquals(20, c.getNumThreads());
        assertEquals(8888, c.getServerPort());
    }

    @Test
    public void testAbs() {
        assertEquals(Integer.MAX_VALUE, Const.safeAbs(Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, Const.safeAbs(Integer.MIN_VALUE + 1));
        assertEquals(1, Const.safeAbs(-1));
        assertEquals(0, Const.safeAbs(0));
        assertEquals(1, Const.safeAbs(1));
        assertEquals(Integer.MAX_VALUE, Const.safeAbs(Integer.MAX_VALUE));
    }
}
