package com.kozhekin.transferring;

import com.kozhekin.transferring.dao.ApplicationDao;
import com.kozhekin.transferring.dao.ApplicationDaoImpl;
import com.kozhekin.transferring.datastore.Datastore;
import com.kozhekin.transferring.datastore.InMemoryDatastoreImpl;
import io.undertow.Undertow;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ApplicationContext implements AutoCloseable {

    private final CountDownLatch latch = new CountDownLatch(1);
    private UndertowJaxrsServer server;
    private Datastore datastore;
    private ApplicationDao applicationDao;
    private Properties appProperties;
    private boolean initialized;
    private boolean destroyed;

    @Override
    public void close() {
        stop();
    }

    public void stop() {
        if (!initialized || destroyed) {
            return;
        }
        stopJaxrsServer(server);
        destroyDatastore(datastore);
        destroyed = true;
        latch.countDown();
    }

    public void start() {
        if (initialized || destroyed) {
            return;
        }
        readProperties();

        getApplicationDao();
        Application.setApplicationDao(applicationDao);

        server = startJaxrsServer();
        initialized = true;
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void waitSigTerm() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ApplicationDao getApplicationDao() {
        if (applicationDao == null) {
            applicationDao = createApplicationDao(getDatastore());
        }
        return applicationDao;
    }

    public ApplicationDao createApplicationDao(Datastore datastore) {
        return new ApplicationDaoImpl(datastore);
    }

    public Datastore getDatastore() {
        if (datastore == null) {
            datastore = createDatastore();
        }
        return datastore;
    }

    public Datastore createDatastore() {
        Datastore datastore = new InMemoryDatastoreImpl();
        datastore.start();
        return datastore;
    }

    public UndertowJaxrsServer startJaxrsServer() {
        Undertow.Builder builder = Undertow.builder().addHttpListener(
                Integer.parseInt(appProperties.getProperty("app.server.port")),
                appProperties.getProperty("app.server.host"));
        UndertowJaxrsServer server = new UndertowJaxrsServer().start(builder);
        server.deploy(Application.class);
        return server;
    }

    public void destroyDatastore(Datastore datastore) {
        if (datastore != null) {
            datastore.stop();
        }
    }

    private void stopJaxrsServer(UndertowJaxrsServer server) {
        if (server != null) {
            server.stop();
        }
    }

    private void readProperties() {
        try (InputStream is = ApplicationContext.class.getClassLoader().getResourceAsStream("app.properties")) {
            appProperties = new Properties();
            appProperties.load(Objects.requireNonNull(is, "Cannot load app.properties"));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load app.properties");
        }
    }

}
