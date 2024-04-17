package org.chelonix.redis;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.chelonix.redis.command.EchoCommand;
import org.chelonix.redis.command.GetCommand;
import org.chelonix.redis.command.InfoCommand;
import org.chelonix.redis.command.PingCommand;
import org.chelonix.redis.command.RedisCommand;
import org.chelonix.redis.command.RedisCommandDecoder;
import org.chelonix.redis.command.SetCommand;
import org.chelonix.redis.resp.RESPParser;

public class Main {

  public static class Args {

    @Parameter(names = {"--port", "-p"}, description = "Server port")
    private Integer port = 6379;

    @Parameter(names = { "--replicaof" }, arity = 2, description = "master host and port")
    private List<String> masterHostAndPort;
  }

  private static final String PING = "*1\r\n$4\r\nping\r\n";
  private static final String PONG = "+PONG\r\n";
  private static final Map<String, String> MAP = new HashMap<>();
  private static final Map<String, ZonedDateTime> TIMEOUT = new HashMap<>();

  private static String bulkString(String s) {
    return "$%d\r\n%s\r\n".formatted(s.length(), s);
  }

  public static void main(String[] argv) {
    Args args = new Args();
    JCommander.newBuilder()
        .addObject(args)
        .build()
        .parse(argv);

    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment this block to pass the first stage
    ServerSocket serverSocket;
    int port = args.port.intValue();
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    try {
      serverSocket = new ServerSocket(port);
      serverSocket.setReuseAddress(true);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      while (true) {
        // Wait for connection from client.
        Socket clientSocket = serverSocket.accept();
        executorService.submit(() -> handleCommand(clientSocket, args));
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }

  private static void handleCommand(Socket clientSocket, Args args) {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      BufferedWriter bw = new BufferedWriter(
          new OutputStreamWriter(clientSocket.getOutputStream()));
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
          case InfoCommand infoCommand -> {
            if (args.masterHostAndPort == null) {
              bw.write(bulkString("role:master"));
            } else {
              bw.write(bulkString("role:slave"));
            }
            bw.flush();
          }
          case GetCommand getCommand -> {
            String key = getCommand.getKey();
            String value = MAP.get(key);
            if (value == null) {
              bw.write("$-1\r\n");
              bw.flush();
              return;
            }
            ZonedDateTime timeout = TIMEOUT.get(key);
            if (timeout != null && timeout.isBefore(ZonedDateTime.now())) {
              bw.write("$-1\r\n");
              bw.flush();
              TIMEOUT.remove(key);
              MAP.remove(key);
              return;
            }
            bw.write("$%d\r\n%s\r\n".formatted(value.length(), value));
            bw.flush();
          }
          case SetCommand setCommand -> {
            String key = setCommand.getKey();
            String value = setCommand.getValue();
            MAP.put(key, value);
            if (setCommand.hasExpiry()) {
              TIMEOUT.put(key, ZonedDateTime.now().plus(setCommand.getDuration()));
            }
            bw.write("+OK\r\n");
            bw.flush();
          }
          default ->
              throw new UnsupportedOperationException("Unhandeled command: " + cmd.getType());
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
