package edu.wallet.server;

/**
 *
 */
public class SystemOutLogger implements ILogger {
    private static final SystemOutLogger instance = new SystemOutLogger();

    public static ILogger instance() {
        return instance;
    }

    private SystemOutLogger() {}

    @Override public void debug(String message, Throwable t) {
        System.out.println("DEBUG " + message);
        if (t != null) {
            t.printStackTrace(System.out);
        }
    }

    @Override public void info(String message, Throwable t) {
        System.out.println("INFO " + message);
        if (t != null) {
            t.printStackTrace(System.out);
        }

    }

    @Override public void error(String message, Throwable t) {
        System.out.println("ERROR " + message);
        if (t != null) {
            t.printStackTrace(System.out);
        }

    }
}
