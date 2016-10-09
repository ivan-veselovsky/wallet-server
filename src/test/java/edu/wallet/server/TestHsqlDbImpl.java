package edu.wallet.server;

import edu.wallet.config.Cfg;
import edu.wallet.server.db.HsqlEmbeddedPersistentStorage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TestHsqlDbImpl extends Base {
    @Test
    public void simplePutAndRetrieve() throws Exception {
        Cfg cfg = Cfg.getEntryBean();

        try (HsqlEmbeddedPersistentStorage st = (HsqlEmbeddedPersistentStorage) cfg.getPersistentStorage()) {
            ValueObject vo1 = new ValueObject("ivan", 10, 0);

            int upd = st.save(Collections.singleton(vo1));

            assertEquals(1, upd);

            ValueObject vo2 = st.retrieve("ivan");

            assertEquals(vo1.userName, vo2.userName);
            assertEquals(vo1.currentBalance, vo2.currentBalance);
            assertEquals(vo1.balanceVersion, vo2.balanceVersion);


            ValueObject vo3 = new ValueObject("ivan", 20, 1);
            upd = st.save(Collections.singleton(vo3));
            assertEquals(1, upd);

            ValueObject vo4 = st.retrieve("ivan");

            assertEquals(vo3.userName, vo4.userName);
            assertEquals(vo3.currentBalance, vo4.currentBalance);
            assertEquals(vo3.balanceVersion, vo4.balanceVersion);


            Collection<ValueObject> c = new ArrayList() {{
                add(new ValueObject("ivan", 300, 3));
                add(new ValueObject("x", 1, 1));
                add(new ValueObject("y", 1, 1));
                add(new ValueObject("z", 1, 1));
            }};

            upd = st.save(c);
            assertEquals(4, upd);

            ValueObject vo5 = st.retrieve("ivan");
            assertEquals(vo5.currentBalance, 300);

            ValueObject vo6 = st.retrieve("z");
            assertEquals(vo6.currentBalance, 1);
        }
    }
}
