package edu.wallet.log;

import edu.wallet.log.*;
import java.util.logging.*;

/**
 * Java util logger implementation.
 */
public class JavaUtilLoggerImpl implements ILogger {

    private final java.util.logging.Logger lg;

    public JavaUtilLoggerImpl() {
        lg = java.util.logging.Logger.getGlobal();

        assert lg != null;
    }

    @Override public void debug(String message, Throwable t) {
        lg.log(Level.FINE, message, t);
    }

    @Override public void info(String message, Throwable t) {
        lg.log(Level.INFO, message, t);
    }

    @Override public void error(String message, Throwable t) {
        lg.log(Level.SEVERE, message, t);
    }
}
