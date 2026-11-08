// messaging/MessageHandler.java
package messaging;

import crypto.DoubleRatchet;
import metrics.PerformanceMetrics;

public class MessageHandler {
    protected final DoubleRatchet doubleRatchet;
    protected final NetworkManager networkManager;
    protected final PerformanceMetrics metrics;

    public MessageHandler(DoubleRatchet doubleRatchet, NetworkManager networkManager, PerformanceMetrics metrics) {
        this.doubleRatchet = doubleRatchet;
        this.networkManager = networkManager;
        this.metrics = metrics;
    }

    public void sendMessage(String messageText) {
        try {
            if (!doubleRatchet.isInitialized()) {
                System.out.println("Cannot send message yet. Key exchange not completed.");
                return;
            }

            long startTime = System.nanoTime();

            // Encrypt the message
            byte[] encryptedMessage = doubleRatchet.encrypt(messageText);

            // Wrap in a Message object
            Message message = new Message(Message.Type.TEXT_MESSAGE, encryptedMessage);

            // Send over the network
            networkManager.sendMessage(message);

            long endTime = System.nanoTime();
            metrics.recordEncryption(endTime - startTime);
            metrics.incrementMessagesSent();

        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleIncomingMessage(Message message) {
        try {
            switch (message.getType()) {
                case TEXT_MESSAGE:
                    handleTextMessage(message);
                    break;
                case KEY_EXCHANGE:
                    handleKeyExchange(message);
                    break;
                default:
                    System.out.println("Unknown message type: " + message.getType());
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling incoming message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void handleTextMessage(Message message) {
        try {
            if (!doubleRatchet.isInitialized()) {
                System.out.println("Received text message but key exchange not completed");
                return;
            }

            long startTime = System.nanoTime();

            // Decrypt the message
            String decryptedText = doubleRatchet.decrypt(message.getContent());

            long endTime = System.nanoTime();
            metrics.recordDecryption(endTime - startTime);
            metrics.incrementMessagesReceived();

            System.out.println("Received: " + decryptedText);

        } catch (Exception e) {
            System.err.println("Failed to process text message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void handleKeyExchange(Message message) {
        System.out.println("Key exchange message received in base handler - should be overridden");
    }
}