package edu.wallet.server;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestSerialization extends Base {
    @Test
    public void serDeRequest() {
        Request rq = new Request("user", 12345, -5);

        byte[] bb = rq.serialize();

        String s = new String(bb, Const.UTF8);

        System.out.println(s);

        assertTrue(s.startsWith("{"));

        Request rq2 = new Request();
        rq2.deserialize(bb);

        assertEquals(rq.userName, rq2.userName);
        assertEquals(rq.transactionId, rq2.transactionId);
        assertEquals(rq.balanceChange, rq2.balanceChange);
    }

    @Test
    public void serDeResponse() {
        Response rsp = new Response(12345, 0, 11, -5, 520);

        byte[] bb = rsp.serialize();

        String s = new String(bb, Const.UTF8);

        System.out.println(s);

        assertTrue(s.startsWith("{"));

        Response rsp2 = new Response();
        rsp2.deserialize(bb);

        assertEquals(rsp.transactionId, rsp2.transactionId);
        assertEquals(rsp.errorCode, rsp2.errorCode);
        assertEquals(rsp.balanceVersion, rsp2.balanceVersion);
        assertEquals(rsp.balanceChange, rsp2.balanceChange);
        assertEquals(rsp.outgoingBalance, rsp2.outgoingBalance);
    }
}
