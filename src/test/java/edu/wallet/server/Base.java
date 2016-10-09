package edu.wallet.server;

import edu.wallet.config.Cfg;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;

import java.util.Map;

import static org.junit.Assert.assertEquals;

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

    /**
     * Expectes the 2 Strings to be simple serialized JSON objects, and
     *
     * @param expect
     * @param actual
     */
    public static void assertJsonObjectsEqual(String expect, String actual) {
        Map<String, Object> e = new JSONObject(expect).toMap();
        Map<String, Object> a = new JSONObject(actual).toMap();

        assertEquals("expected=[" + expect + "], actual=[" + a + "]", e, a);
    }
}
