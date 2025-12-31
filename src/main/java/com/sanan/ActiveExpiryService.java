package com.sanan;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActiveExpiryService implements Runnable {
    private final Map<String, RedisEntry> dataStore;
    private final int SAMPLE_SIZE = 20; // keys to check per cycle
    private boolean running = true; // flag to turn off or on

    public ActiveExpiryService(Map<String, RedisEntry> dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void run() {
        System.out.println("Active Expiry Service (Janitor) started...");
        while (running) {
            try {
                TimeUnit.SECONDS.sleep(1);
                if (dataStore.isEmpty())
                    continue;
                int keysChecked = 0;
                int keysDeleted = 0;

                Iterator<String> iterator = dataStore.keySet().iterator();

                while (iterator.hasNext() && keysChecked < SAMPLE_SIZE) {
                    String key = iterator.next();
                    RedisEntry entry = dataStore.get(key);

                    // Check and Delete
                    if (entry != null && entry.isExpired()) {
                        dataStore.remove(key);
                        keysDeleted++;
                    }

                    keysChecked++;
                }

                // Logging
                if (keysDeleted > 0) {
                    System.out.println("[Janitor] Removed " + keysDeleted + " expired keys.");
                }
            } catch (InterruptedException e) {

            }
        }
    }

    public void stop() {
        this.running = false;
    }
}
