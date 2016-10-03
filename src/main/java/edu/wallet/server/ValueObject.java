package edu.wallet.server;

/**
 * Represents a row in the database.
 */
public class ValueObject {
    public final String userName;
    public final int currentBalance;
    public final long balanceVersion;

    ValueObject(String userName, int currentBalance, long balanceVersion) {
        this.userName = userName;
        this.currentBalance = currentBalance;
        this.balanceVersion = balanceVersion;
    }
}
