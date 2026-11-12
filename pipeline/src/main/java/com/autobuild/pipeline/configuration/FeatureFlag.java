package com.autobuild.pipeline.configuration;

/** Enum of all feature flag keys used in this application. */
public enum FeatureFlag {
    ENABLE_EDIT_PIPELINE("ENABLE_EDIT_PIPELINE"),
    ENABLE_MESSAGE_QUEUE("ENABLE_MESSAGE_QUEUE");

    private final String key;

    FeatureFlag(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
