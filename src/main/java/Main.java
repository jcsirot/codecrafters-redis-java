import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final String PING = "*1\r\n$4\r\nping\r\n";
    private static final String PONG = "+PONG\r\n";

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
                executorService.submit(() -> handlePing(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handlePing(Socket clientSocket) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (true) {
                String s = br.readLine();
                if ("ping".equalsIgnoreCase(s)) {
                    clientSocket.getOutputStream().write(PONG.getBytes(StandardCharsets.UTF_8));
                    clientSocket.getOutputStream().flush();
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
