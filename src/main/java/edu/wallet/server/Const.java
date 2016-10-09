package edu.wallet.server;

import java.nio.charset.Charset;

/**
 * Some project-wide utility constants.
 */
public class Const {
    /**
     * Frequently used upon byte-to-String and back conversions.
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * End Of Message marker.
     */
    public static final String EOM_MARKER = "\n";

    /**
     * Ordinals of this enum are sent to client as the error codes.
     */
    public enum ErrorCode {
        Okay,                       // 0
        UserBlacklisted,            // 1
        BalanceChangeLimitExceeded, // 2
        DuplicateTransactionId,     // 3 TODO: it was an idea to send this error code, when a history response is sent.
                                        // But it was not yet implemented.
        NegativeBalance,            // 4
        badRequest,                 // 5
        InternalServerError,        // 6
    }

    public static int safeAbs(int x) {
        if (x == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE; // closest approximation (?).
        }
        return Math.abs(x);
    }
}
