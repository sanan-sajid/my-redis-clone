package com.sanan;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.io.IOException;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Map<String, RedisEntry> dataStore;

    public ClientHandler(Socket socket, Map<String, RedisEntry> dataStore) {
        this.clientSocket = socket;
        this.dataStore = dataStore;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();
            String line;
            while ((line = in.readLine()) != null) {

                String[] parts = line.trim().split("\\s+");
                if (parts.length == 0)
                    continue;
                String command = parts[0].toUpperCase();
                if (command.equals("SET")) {
                    if (parts.length >= 3) {
                        String key = parts[1];
                        String value = parts[2];
                        long expiryTime = -1; // Default: Never expires

                        if (parts.length > 4 && parts[3].equalsIgnoreCase("px")) {
                            long ttl = Long.parseLong(parts[4]);
                            expiryTime = System.currentTimeMillis() + ttl;
                        }

                        // Save the Entry
                        dataStore.put(key, new RedisEntry(key, value, expiryTime));
                        out.write("+OK\r\n".getBytes());
                    }
                } else if (command.equals("GET")) {
                    if (parts.length > 1) {
                        String key = parts[1];
                        RedisEntry redisEntry = dataStore.get(key);
                        if (redisEntry != null) {
                            if (redisEntry.isExpired()) {
                                dataStore.remove(key);
                                out.write("Not found\r\n".getBytes());
                            } else {
                                out.write(toBulkString(redisEntry.getValue()).getBytes());
                            }
                        } else {
                            out.write("Not found\r\n".getBytes());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        }
    }

    // Helper to format as Redis Bulk String: "$3\r\nhey\r\n"
    // Your Python script will receive this raw string.
    private String toBulkString(String s) {
        return "$" + s.length() + "\r\n" + s + "\r\n";
    }
}