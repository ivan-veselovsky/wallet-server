package edu.wallet.server;

import org.json.*;

/**
 * transaction id, error code, balance version, balance change, balance after change
 */
public class Response {

    long transactionId;
    int errorCode;
    long balanceVersion;
    int balanceChange; // TODO: why the change is sent back to the client?
    int outgoingBalance;

    Response(long tx, int errorCode, long balanceVersion, int balanceChange, int outgoingBalance) {
        this.transactionId = tx;
        this.errorCode = errorCode;
        this.balanceVersion = balanceVersion;
        this.balanceChange = balanceChange;
        this.outgoingBalance = outgoingBalance;
    }

    public Response() {

    }

    private String serialize0() {
        return new JSONObject()
            .accumulate(Field.transactionId.name(), transactionId)
            .accumulate(Field.errorCode.name(), errorCode)
            .accumulate(Field.balanceVersion.name(), balanceVersion)
            .accumulate(Field.balanceChange.name(), balanceChange)
            .accumulate(Field.outgoingBalance.name(), outgoingBalance)
            .toString();
    }

    public byte[] serialize() {
        return serialize0().concat(Const.EOM_MARKER).getBytes(Const.UTF8);
    }

    public void deserialize(byte[] bb) {
        JSONObject obj = new JSONObject(new String(bb, Const.UTF8));

        transactionId = obj.getLong(Field.transactionId.name());
        errorCode = obj.getInt(Field.errorCode.name());
        balanceVersion = obj.getLong(Field.balanceVersion.name());
        balanceChange = obj.getInt(Field.balanceChange.name());
        outgoingBalance = obj.getInt(Field.outgoingBalance.name());
    }

    @Override public String toString() {
        // used in logging
        return serialize0();
    }
}
