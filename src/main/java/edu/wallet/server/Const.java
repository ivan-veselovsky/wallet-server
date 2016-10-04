package edu.wallet.server;

import java.nio.charset.*;

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
    public static enum ErrorCode {
        Okay,                       // 0
        UserBlacklisted,            // 1
        BalanceChangeLimitExceeded, // 2
        DuplicateTransactionId,     // 3
        NegativeBalance,            // 4
        badRequest,                 // 5
        InternalServerError,        // 6
    }

}
