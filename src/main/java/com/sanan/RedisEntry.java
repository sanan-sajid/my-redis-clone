package com.sanan;

public class RedisEntry {

    private final String key;
    private final String value;
    private final long expiryTime;

    RedisEntry(String key, String value, long expiryTime) {
        this.key = key;
        this.expiryTime = expiryTime;
        this.value = value;
    }

    public boolean isExpired() {
        if (expiryTime == -1)
            return false; // -1 means never expires
        return System.currentTimeMillis() > expiryTime;
    }

    public String getValue() {
        return value;
    }

}
