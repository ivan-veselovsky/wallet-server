package edu.wallet.server;

import org.json.JSONObject;

/**
 * client->server: username, transaction id, balance change
 */
public class Request {
    String userName;
    long transactionId;
    int balanceChange;

    public Request() {

    }

    public Request(String u, long tid, int bal) {
        userName = u;
        transactionId = tid;
        balanceChange = bal;
    }

    public byte[] serialize() {
        return toStr().concat(Const.EOM_MARKER).getBytes(Const.UTF8);
    }

    private String toStr() {
        return new JSONObject()
                .accumulate(Field.user.name(), userName)
                .accumulate(Field.transactionId.name(), transactionId)
                .accumulate(Field.balanceChange.name(), balanceChange)
                .toString();
    }

    public Request deserialize(byte[] bb) {
        JSONObject obj = new JSONObject(new String(bb, Const.UTF8));

        userName = obj.getString(Field.user.name());
        transactionId = obj.getLong(Field.transactionId.name());
        balanceChange = obj.getInt(Field.balanceChange.name());

        return this;
    }

    @Override
    public String toString() {
        return toStr();
    }

    @Override
    public int hashCode() {
        return (int) (transactionId ^ (transactionId >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != Request.class) {
            return false;
        }

        return ((Request) obj).transactionId == transactionId;
    }
}
