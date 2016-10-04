package edu.wallet.config;

import edu.wallet.log.*;
import edu.wallet.server.*;
import edu.wallet.server.db.*;
import org.springframework.context.support.*;

/**
 * Top level bean composing all the subsystem implementations
 * injection, such as Configuration, Logging, PersistentStorage, etc.
 */
public class Cfg {

    private static final String CTX_FILE = "config.xml";

    private static Cfg cfg;

    public static synchronized Cfg getEntryBean() {
        if (cfg == null) {
            AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(CTX_FILE);

            cfg = (Cfg)ctx.getBean("cfg-id");
        }

        return cfg;
    }

    public static synchronized void destroy() {
        cfg = null;
    }

    private IConfiguration configuration;
    private ILogger logger;
    private IPersistentStorage persistentStorage;
    private IProcessor processor;

    public IConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    public ILogger getLogger() {
        return logger;
    }

    public IPersistentStorage getPersistentStorage() {
        return persistentStorage;
    }

    public void setPersistentStorage(IPersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public IProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(IProcessor processor) {
        this.processor = processor;
    }
}
