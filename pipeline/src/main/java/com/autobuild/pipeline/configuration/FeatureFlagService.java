package com.autobuild.pipeline.configuration;

/** Service interface for evaluating feature flags. */
@FunctionalInterface
public interface FeatureFlagService {
    boolean getBooleanValue(String flagKey);
}
