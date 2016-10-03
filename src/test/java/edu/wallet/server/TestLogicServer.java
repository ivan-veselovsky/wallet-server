package edu.wallet.server;

import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 */
public class TestLogicServer {

    @Test
    public void basicProcessing() throws Exception {
        Configuration c = new Configuration();

        ILogger logger = SystemOutLogger.instance();

        LogicServer srv = new LogicServer(c, logger) {
            @Override ValueObject getFromDB(String userName) {
                return new ValueObject(userName, 500, 28);
            }
        };

        Request rq = new Request("ivan", 12345, -499);

        System.out.println(rq);

        Response rsp = srv.process(rq);

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
