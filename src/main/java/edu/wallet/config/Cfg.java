package edu.wallet.config;

import edu.wallet.log.*;
import org.springframework.context.support.*;

/**
 * Top level bean composing all the subsystem implementations
 * injection, such as Configuration, Logging, etc.
 */
public class Cfg {

    private static final String CTX_FILE = "config.xml";

    private static final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(CTX_FILE);

    private static final Cfg cfg = (Cfg)ctx.getBean("cfg-id");

    public static Cfg getEntryBean() {
        return cfg;
    }

    private IConfiguration configuration;

    public IConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    private ILogger logger;

    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

    public ILogger getLogger() {
        return logger;
    }
}
