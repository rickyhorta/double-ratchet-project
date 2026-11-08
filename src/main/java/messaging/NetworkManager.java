package messaging;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NetworkManager {
    private final int localPort;
    private final String remoteHost;
    private final int remotePort;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean isRunning = false;

    public NetworkManager(int localPort, String remoteHost, int remotePort) {
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.executorService = Executors.newCachedThreadPool();
    }

    public void startListening(Consumer<Message> messageHandler) throws IOException {
        serverSocket = new ServerSocket(localPort);
        isRunning = true;

        System.out.println("Listening for incoming connections on port " + localPort);

        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClientConnection(clientSocket, messageHandler));
            } catch (SocketException e) {
                if (isRunning) {
                    System.err.println("Socket error: " + e.getMessage());
                }
                // Expected on shutdown, ignore if not running
            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
            }
        }
    }

    private void handleClientConnection(Socket clientSocket, Consumer<Message> messageHandler) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            Object obj = in.readObject();
            if (obj instanceof Message) {
                Message message = (Message) obj;
                messageHandler.accept(message);
            } else {
                System.err.println("Received unknown object type: " + obj.getClass());
            }
        } catch (Exception e) {
            System.err.println("Error handling client connection: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    public void sendMessage(Message message) {
        try (Socket socket = new Socket(remoteHost, remotePort);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(message);
            out.flush();

        } catch (ConnectException e) {
            System.err.println("Connection refused: The remote endpoint might not be running.");
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdown() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        executorService.shutdown();
    }
}
