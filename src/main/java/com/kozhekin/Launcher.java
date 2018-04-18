package com.kozhekin;

import com.kozhekin.service.TransferringService;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher assembles all parts of an application also initializes and destroys all modules
 */
public class Launcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    private UndertowJaxrsServer server;
    private boolean initialized;
    private boolean destroyed;

    public void launch() {
        if (initialized || destroyed) {
            return;
        }
        LOGGER.info("Initializing application...");
        Hibernate.init();
        TransferringService.init();
        server = new UndertowJaxrsServer().start();
        server.deploy(Application.class);
        initialized = true;
    }

    public void shutdown() {
        if (!initialized || destroyed) {
            return;
        }
        LOGGER.info("Stopping application...");
        server.stop();
        TransferringService.destroy();
        Hibernate.destroy();
        destroyed = true;
    }

    public static void main(String[] args) {
        final Launcher launcher = new Launcher();
        launcher.launch();
        Runtime.getRuntime().addShutdownHook(new Thread(launcher::shutdown));
    }
}
