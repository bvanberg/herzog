package com.herzog.api;

import com.herzog.module.HerzogApiModule;
import com.hubspot.dropwizard.guice.GuiceBundle;
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
        GuiceBundle<IdentificationConfiguration> guiceBundle = GuiceBundle.<IdentificationConfiguration>newBuilder()
                .addModule(new HerzogApiModule())
                .enableAutoConfig(getClass().getPackage().getName())
                .setConfigClass(IdentificationConfiguration.class)
                .build();

        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(IdentificationConfiguration configuration, Environment environment) {

    }

}