package edu.wallet.server;

import java.util.logging.*;

/**
 * Created by ivan on 03.10.16.
 */
public class LoggerImpl implements ILogger {

    private final java.util.logging.Logger lg;

    LoggerImpl() {
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
