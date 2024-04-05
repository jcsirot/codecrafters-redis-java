package org.chelonix.redis;

import org.chelonix.redis.command.*;
import org.chelonix.redis.resp.RESPParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final String PING = "*1\r\n$4\r\nping\r\n";
    private static final String PONG = "+PONG\r\n";

    private static final Map<String, String> MAP = new HashMap<>();

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        //  Uncomment this block to pass the first stage
        ServerSocket serverSocket;
        int port = 6379;
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            while (true) {
                // Wait for connection from client.
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleCommand(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleCommand(Socket clientSocket) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            RedisCommandDecoder commandParser = new RedisCommandDecoder(new RESPParser(br));
            while (true) {
                RedisCommand cmd = commandParser.decode();
                if (cmd == null) {
                    return;
                }
                switch (cmd) {
                    case PingCommand __ -> {
                        bw.write(PONG);
                        bw.flush();
                    }
                    case EchoCommand echoCommand -> {
                        bw.write("+%s\r\n".formatted(echoCommand.getMessage()));
                        bw.flush();
                    }
                    case GetCommand getCommand -> {
                        String key = getCommand.getKey();
                        String value = MAP.get(key);
                        bw.write("$%d\r\n%s\r\n".formatted(value.length(), value));
                        bw.flush();
                    }
                    case SetCommand setCommand -> {
                        String key = setCommand.getKey();
                        String value = setCommand.getValue();
                        MAP.put(key, value);
                        bw.write("+OK\r\n");
                        bw.flush();
                    }
                    default -> throw new UnsupportedOperationException("Unhandeled command: " + cmd.getType());
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}
