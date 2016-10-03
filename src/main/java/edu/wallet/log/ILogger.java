package edu.wallet.log;

/**
 * Logger interface.
 * Used for more flexibility.
 * Exact implementation injected upon application start up.
 */
public interface ILogger {
    void debug(String message, Throwable t);
    void info(String message, Throwable t);
    void error(String message, Throwable t);
}
