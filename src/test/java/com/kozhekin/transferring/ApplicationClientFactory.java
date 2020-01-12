package com.kozhekin.transferring;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class ApplicationClientFactory {
    public static ApplicationClient create() {
        return create("http://localhost:8080");
    }

    public static ApplicationClient create(String host) {
        final ResteasyClient client = new ResteasyClientBuilder().build();
        final ResteasyWebTarget target = client.target(host);
        return target.proxy(ApplicationClient.class);
    }
}
