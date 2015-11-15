package com.herzog.api;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class IdentificationApplication extends Application<IdentificationConfiguration> {
    public static void main(String[] args) throws Exception {
        new IdentificationApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<IdentificationConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(IdentificationConfiguration configuration,
                    Environment environment) {
        final IdentificationResource resource = new IdentificationResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );
        environment.jersey().register(resource);
    }

}