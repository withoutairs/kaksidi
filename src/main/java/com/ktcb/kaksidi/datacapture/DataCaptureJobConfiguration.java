package com.ktcb.kaksidi.datacapture;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class DataCaptureJobConfiguration extends Configuration {

    @NotEmpty
    private boolean enabled;
    @NotEmpty
    private int attemptFrequencySeconds;
    @NotEmpty
    private int sampleFrequencySeconds;
    @NotEmpty
    private int unhealthyThresholdSeconds;
    private String zipStorageDirectory;

    @JsonProperty
    public int getUnhealthyThresholdSeconds() {
        return unhealthyThresholdSeconds;
    }

    @JsonProperty
    public void setUnhealthyThresholdSeconds(int unhealthyThresholdSeconds) {
        this.unhealthyThresholdSeconds = unhealthyThresholdSeconds;
    }

    @JsonProperty
    public boolean isEnabled() {
        return enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty
    public int getSampleFrequencySeconds() {
        return sampleFrequencySeconds;
    }

    @JsonProperty
    public void setSampleFrequencySeconds(int sampleFrequencySeconds) {
        this.sampleFrequencySeconds = sampleFrequencySeconds;
    }

    @JsonProperty
    public int getAttemptFrequencySeconds() {
        return attemptFrequencySeconds;
    }

    @JsonProperty
    public void setAttemptFrequencySeconds(int attemptFrequencySeconds) {
        this.attemptFrequencySeconds = attemptFrequencySeconds;
    }

    @JsonProperty
    public String getZipStorageDirectory() {
        return zipStorageDirectory;
    }

    @JsonProperty
    public void setZipStorageDirectory(String zipStorageDirectory) {
        this.zipStorageDirectory = zipStorageDirectory;
    }
}
