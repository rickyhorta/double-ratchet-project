import crypto.DoubleRatchet;
import messaging.Message;
import messaging.MessageHandler;
import messaging.NetworkManager;
import metrics.PerformanceMetrics;
import message_gui.MessageGUI;

import java.util.Scanner;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Usage: mvn exec:java -Dexec.mainClass=\"Main\" -Dexec.args=\"<name> <remote_name> <remote_ip> <local_port> <remote_port>\"");
            System.out.println("Example: mvn exec:java -Dexec.mainClass=\"Main\" -Dexec.args=\"David Charles localhost 8082 8081\"");
            System.exit(1);
        }

        String name = args[0];
        String remoteName = args[1];
        String remoteIp = args[2];
        int localPort = Integer.parseInt(args[3]);
        int remotePort = Integer.parseInt(args[4]);

        // Determine who is the initiator based on lexicographic order
        boolean isInitiator = name.compareTo(remoteName) < 0;
        
        try {
            System.out.println("Starting " + name + " client...");
            System.out.println("Remote party: " + remoteName);
            System.out.println("Role: " + (isInitiator ? "INITIATOR" : "RESPONDER"));
            System.out.println("Local port: " + localPort);
            System.out.println("Remote endpoint: " + remoteIp + ":" + remotePort);

            PerformanceMetrics metrics = new PerformanceMetrics();
            NetworkManager networkManager = new NetworkManager(localPort, remoteIp, remotePort);
            DoubleRatchet doubleRatchet = new DoubleRatchet(name);

            // Flag para indicar que o key exchange foi concluído
            boolean[] keyExchangeCompleted = { false };
            MessageGUI[] gui = { null }; // Array to hold GUI reference

            // MessageHandler modificado para integrar com GUI
            MessageHandler messageHandler = new MessageHandler(doubleRatchet, networkManager, metrics) {
                @Override
                public void handleIncomingMessage(Message message) {
                    try {
                        if (message.getType() == Message.Type.KEY_EXCHANGE) {
                            System.out.println("[DEBUG-MAIN] Received KEY_EXCHANGE message");
                            System.out.println("[DEBUG-MAIN] Key exchange data length: " + message.getContent().length);
                            
                            // Processar a troca de chaves
                            doubleRatchet.processInitialKeyExchange(message.getContent());
                            
                            // Set chain keys based on role
                            byte[][] derivedKeys = doubleRatchet.getLastDerivedKeys();
                            doubleRatchet.setChainKeys(isInitiator, derivedKeys[1], derivedKeys[2]);
                            
                            System.out.println("[DEBUG-MAIN] Processed key exchange from remote party");
                            
                            // ONLY the responder should respond to the initiator's key exchange
                            if (!keyExchangeCompleted[0] && !isInitiator) {
                                System.out.println("[DEBUG-MAIN] Generating key exchange response");
                                byte[] responseKeyExchange = doubleRatchet.generateInitialKeyExchangeMessage();
                                System.out.println("[DEBUG-MAIN] Response key exchange length: " + responseKeyExchange.length);
                                Message responseMessage = new Message(Message.Type.KEY_EXCHANGE, responseKeyExchange);
                                networkManager.sendMessage(responseMessage);
                                System.out.println("[DEBUG-MAIN] Sent key exchange response");
                            }

                            keyExchangeCompleted[0] = true;
                            System.out.println("[DEBUG-MAIN] Key exchange completed flag set to true");
                            
                            // Launch GUI after key exchange is completed
                            if (gui[0] == null) {
                                SwingUtilities.invokeLater(() -> {
                                    gui[0] = new MessageGUI(this, metrics, name, remoteName);
                                    System.out.println("GUI launched successfully!");
                                });
                            }
                            
                        } else if (message.getType() == Message.Type.TEXT_MESSAGE) {
                            // Handle text messages - decrypt here and pass to GUI
                            System.out.println("[DEBUG-MAIN] Received TEXT_MESSAGE, processing...");
                            
                            if (!doubleRatchet.isInitialized()) {
                                System.out.println("Received text message but key exchange not completed");
                                return;
                            }

                            long startTime = System.nanoTime();

                            try {
                                // Decrypt the message using the MessageHandler's decrypt method
                                String decryptedText = doubleRatchet.decrypt(message.getContent());
                                
                                long endTime = System.nanoTime();
                                metrics.recordDecryption(endTime - startTime);
                                metrics.incrementMessagesReceived();

                                System.out.println("Received: " + decryptedText);
                                
                                // If GUI is open, display the decrypted message
                                if (gui[0] != null) {
                                    gui[0].displayReceivedMessage(decryptedText);
                                }
                                
                            } catch (Exception e) {
                                System.err.println("Failed to decrypt message: " + e.getMessage());
                                e.printStackTrace();
                                if (gui[0] != null) {
                                    gui[0].showErrorMessage("Failed to decrypt message: " + e.getMessage());
                                }
                            }
                        } else {
                            // Para outros tipos de mensagens, segue o comportamento original
                            System.out.println("[DEBUG-MAIN] Received other message type, forwarding to super");
                            super.handleIncomingMessage(message);
                        }
                    } catch (Exception e) {
                        System.err.println("[ERROR-MAIN] Error handling incoming message: " + e.getMessage());
                        e.printStackTrace();
                        if (gui[0] != null) {
                            gui[0].showErrorMessage("Error handling message: " + e.getMessage());
                        }
                    }
                }
            };

            // Começa a escutar as conexões
            Thread listenerThread = new Thread(() -> {
                try {
                    networkManager.startListening(messageHandler::handleIncomingMessage);
                } catch (Exception e) {
                    System.err.println("Error in listener thread: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();

            // Loop principal para enviar mensagens (manter para comando init)
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nEnter 'init' to initiate key exchange (if you are the initiator), 'exit' to quit:");

            while (true) {
                System.out.print(name + "> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    break;
                } else if (input.equalsIgnoreCase("init") && !keyExchangeCompleted[0]) {
                    // ONLY the initiator should be able to initiate
                    if (isInitiator) {
                        System.out.println("Initiating key exchange...");
                        byte[] initialKeyExchangeMessage = doubleRatchet.generateInitialKeyExchangeMessage();
                        Message keyExchangeMessage = new Message(Message.Type.KEY_EXCHANGE, initialKeyExchangeMessage);
                        networkManager.sendMessage(keyExchangeMessage);
                        System.out.println("Sent initial key exchange data");
                    } else {
                        System.out.println("Only the initiator (" + (name.compareTo(remoteName) < 0 ? name : remoteName) + 
                                          ") can initiate key exchange. Responder should wait for the initiator's message.");
                    }
                } else if (keyExchangeCompleted[0]) {
                    if (gui[0] != null) {
                        System.out.println("GUI is active. Use the GUI window to send messages or type 'exit' to quit.");
                    } else {
                        // Fallback: send via console if GUI not available
                        messageHandler.sendMessage(input);
                    }
                } else {
                    System.out.println("Cannot send message yet. Key exchange not completed. " + 
                                     (isInitiator ? "Type 'init' to initiate key exchange." : 
                                      "Wait for the initiator to start key exchange."));
                }
            }

            scanner.close();
            System.out.println("Client shutting down...");
            networkManager.shutdown();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}