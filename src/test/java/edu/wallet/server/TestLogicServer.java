package edu.wallet.server;

import edu.wallet.config.Cfg;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TestLogicServer extends Base {

    @Test
    public void basicProcessing() throws Exception {
        Cfg cfg = Cfg.getEntryBean();

        cfg.getPersistentStorage().clear();

        cfg.getPersistentStorage().save(Collections.singleton(new ValueObject("ivan", 500, 28)));

        LogicServer srv = (LogicServer) cfg.getProcessor();

        Request rq = new Request("ivan", 12345, -499);

        System.out.println(rq);

        Response rsp = ((LogicServer) cfg.getProcessor()).process(rq);

        System.out.println(rsp);

        assertEquals(-499, rsp.balanceChange);
        assertEquals(0, rsp.errorCode);
        assertEquals(1, rsp.outgoingBalance);
        assertEquals(29, rsp.balanceVersion);
        assertEquals(12345, rsp.transactionId);

        // Same Rsp should be returned from the history:
        Response rsp2 = srv.process(rq);

        assertEquals(rsp.balanceChange, rsp2.balanceChange);
        assertEquals(rsp.errorCode, rsp2.errorCode);
        assertEquals(rsp.outgoingBalance, rsp2.outgoingBalance);
        assertEquals(rsp.balanceVersion, rsp2.balanceVersion);
        assertEquals(rsp.transactionId, rsp2.transactionId);
    }
}
