package com.autobuild.pipeline.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.openfeature.contrib.providers.ofrep.OfrepProvider;
import dev.openfeature.contrib.providers.ofrep.OfrepProviderOptions;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;

/** Wires an OpenFeature provider and exposes a {@link FeatureFlagService} bean. */
@Configuration
public class FeatureFlagConfig {

    @Bean
    @ConditionalOnProperty("feature-flags.provider.uri")
    public FeatureFlagService openFeatureFlagService(
            @Value("${feature-flags.provider.uri}") String uri) throws Exception {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProviderAndWait(OfrepProvider.constructProvider(OfrepProviderOptions.builder().baseUrl(uri).build()));
        Client client = api.getClient();
        return flagKey -> client.getBooleanValue(flagKey, false);
    }

    @Bean
    @ConditionalOnMissingBean
    public FeatureFlagService noOpFeatureFlagService() {
        return flagKey -> false;
    }
}
