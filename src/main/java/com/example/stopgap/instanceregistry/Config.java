package com.example.stopgap.instanceregistry;

public interface Config {

    void set(String key, String value);

    String get(String key);

}
