package edu.wallet.server;

import edu.wallet.config.*;
import org.junit.*;

/**
 *
 */
public class Base {
    @Before
    public void before() {
        Cfg cfg = Cfg.getEntryBean();
    }

    @After
    public void after() throws Exception {
        Cfg cfg = Cfg.getEntryBean();

        cfg.getProcessor().close();
        cfg.getPersistentStorage().close();

        Cfg.destroy();
    }
}
