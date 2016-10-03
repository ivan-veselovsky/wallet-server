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
        Okay,
        UserBlacklisted,
        BalanceChangeLimitExceeded,
        DuplicateTransactionId,
        NegativeBalance,
        badRequest,
        InternalServerError,
    }

}
