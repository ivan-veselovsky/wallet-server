package edu.wallet.server;

/**
 * Simple logger, without any levels for now.
 */
public interface ILogger {
    void debug(String message, Throwable t);
    void info(String message, Throwable t);
    void error(String message, Throwable t);
}
