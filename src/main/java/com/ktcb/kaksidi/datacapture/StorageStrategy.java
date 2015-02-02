package com.ktcb.kaksidi.datacapture;

public interface StorageStrategy {
    void apply(String responseBody);
}
