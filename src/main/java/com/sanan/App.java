package com.sanan;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class App {
    private static final Map<String, RedisEntry> dataStore = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        int port = 6379;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("Multithreaded Redis Server listening on port " + port);

            ActiveExpiryService janitor = new ActiveExpiryService(dataStore);
            new Thread(janitor).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, dataStore);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
