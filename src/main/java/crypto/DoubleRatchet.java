package crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

public class DoubleRatchet {
    private String clientId;
    private KeyPair DHKeyPair;
    private PublicKey DHRemotePublicKey;

    private byte[] rootKey;
    private byte[] sendingChainKey;
    private byte[] receivingChainKey;

    private int sendMessageNumber = 0;
    private int receiveMessageNumber = 0;
    private int previousChainLength = 0;

    private final Map<Integer, List<byte[]>> skippedMessageKeys = new HashMap<>();
    private boolean initialKeyExchangeCompleted = false;

    private long totalRatchetSteps = 0;
    private long totalDHRatchets = 0;

    // Add field to store last derived keys
    private byte[][] lastDerivedKeys;

    public DoubleRatchet(String clientId) {
        this.clientId = clientId;
        try {
            this.DHKeyPair = DiffieHellman.generateKeyPair();
            this.rootKey = "DOUBLE_RATCHET_INITIAL_ROOT_KEY".getBytes();

            System.out.println("[DEBUG] Initial root key set: " +
                    Base64.getEncoder().encodeToString(this.rootKey));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Double Ratchet: " + e.getMessage(), e);
        }
    }

    public byte[] generateInitialKeyExchangeMessage() {
        try {
            return DHKeyPair.getPublic().getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key exchange message: " + e.getMessage(), e);
        }
    }

    public void processInitialKeyExchange(byte[] keyExchangeData) {
        try {
            this.DHRemotePublicKey = DiffieHellman.decodePublicKey(keyExchangeData);
            System.out.println("[DEBUG] Remote public key received: " + 
                Base64.getEncoder().encodeToString(this.DHRemotePublicKey.getEncoded()));

            // Calculate the shared secret using our private key and their public key
            byte[] sharedSecret = DiffieHellman.calculateSharedSecret(DHKeyPair.getPrivate(), DHRemotePublicKey);

            // Debug: Print shared secret hash to verify it matches between parties
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] sharedSecretHash = digest.digest(sharedSecret);
            System.out.println("[DEBUG] Shared secret hash: " + Base64.getEncoder().encodeToString(sharedSecretHash));
            
            // Debug: Print root key before derivation
            System.out.println("[DEBUG] Root key before derivation: " + Base64.getEncoder().encodeToString(rootKey));

            // Derive root key and chain keys from the shared secret
            byte[][] keys = KDF.deriveKeys(rootKey, sharedSecret);
            
            // Debug: Print all derived keys
            System.out.println("[DEBUG] New root key: " + Base64.getEncoder().encodeToString(keys[0]));
            System.out.println("[DEBUG] Chain key 1: " + Base64.getEncoder().encodeToString(keys[1]));
            System.out.println("[DEBUG] Chain key 2: " + Base64.getEncoder().encodeToString(keys[2]));
            
            this.rootKey = keys[0];
            this.lastDerivedKeys = keys; // Store for later use

            System.out.println("[DEBUG] Client ID: " + clientId);
            
            initialKeyExchangeCompleted = true;
            System.out.println("[DEBUG] Initial key exchange completed.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to process key exchange: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process key exchange: " + e.getMessage(), e);
        }
    }

    // Add this new method to set chain keys after determining roles
    public void setChainKeys(boolean isInitiator, byte[] chainKey1, byte[] chainKey2) {
        if (isInitiator) {
            this.sendingChainKey = chainKey1;
            this.receivingChainKey = chainKey2;
        } else {
            this.sendingChainKey = chainKey2;
            this.receivingChainKey = chainKey1;
        }

        // Debug: Print key assignments
        System.out.println("[DEBUG] Assigned sending chain: " + 
            Base64.getEncoder().encodeToString(this.sendingChainKey));
        System.out.println("[DEBUG] Assigned receiving chain: " + 
            Base64.getEncoder().encodeToString(this.receivingChainKey));
    }

    // Add getter for derived keys
    public byte[][] getLastDerivedKeys() {
        return lastDerivedKeys;
    }
    
    public byte[] encrypt(String plaintext) {
        if (!initialKeyExchangeCompleted) {
            throw new IllegalStateException("Cannot encrypt before key exchange is completed");
        }

        try {
            // Derive message key and update sending chain key
            byte[][] sendingKeys = KDF.deriveMessageKeys(sendingChainKey);
            byte[] messageKey = sendingKeys[0];
            this.sendingChainKey = sendingKeys[1];

            // Debug: Print message key hash to verify encryption keys
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            System.out.println("ENCRYPTION - Message key hash: " +
                    Base64.getEncoder().encodeToString(digest.digest(messageKey)));

            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] header = createHeader();
            byte[] ciphertext = AEAD.encrypt(messageKey, plaintextBytes, header);

            byte[] message = new byte[header.length + ciphertext.length];
            System.arraycopy(header, 0, message, 0, header.length);
            System.arraycopy(ciphertext, 0, message, header.length, ciphertext.length);

            System.out.println("🔒 SENDING ENCRYPTED MESSAGE:");
            System.out.println("   Original text: \"" + plaintext + "\"");
            System.out.println("   Encrypted data (Base64): " + Base64.getEncoder().encodeToString(message));
            System.out.println("   Message length: " + message.length + " bytes");
            System.out.println("   Header length: " + header.length + " bytes");
            System.out.println("   Ciphertext length: " + ciphertext.length + " bytes");
            System.out.println("   Message number: " + sendMessageNumber);
            System.out.println();

            sendMessageNumber++;
            totalRatchetSteps++;

            return message;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt message: " + e.getMessage(), e);
        }
    }

    public String decrypt(byte[] message) {
        if (!initialKeyExchangeCompleted) {
            throw new IllegalStateException("Cannot decrypt before key exchange is completed");
        }
        
        System.out.println("🔓 RECEIVED ENCRYPTED MESSAGE:");
        System.out.println("   Encrypted data (Base64): " + Base64.getEncoder().encodeToString(message));
        System.out.println("   Message length: " + message.length + " bytes");

        int headerSize = getHeaderSize();
        if (message.length < headerSize) {
            throw new RuntimeException("Message too short to contain valid header");
        }

        try {
            // Extract header and ciphertext
            byte[] header = new byte[headerSize];
            byte[] ciphertext = new byte[message.length - headerSize];
            System.arraycopy(message, 0, header, 0, headerSize);
            System.arraycopy(message, headerSize, ciphertext, 0, ciphertext.length);

            // Parse header
            Map<String, Object> headerData = parseHeader(header);
            PublicKey messagePublicKey = (PublicKey) headerData.get("publicKey");
            int messageNumber = (Integer) headerData.get("messageNumber");

            System.out.println("DECRYPTION - Message number: " + messageNumber);

            // Check if we need to perform DH ratchet
            if (!messagePublicKey.equals(DHRemotePublicKey)) {
                System.out.println("DECRYPTION - Performing DH ratchet");
                performDHRatchet(messagePublicKey);
            }

            // Try to decrypt with current receiving chain
            byte[] messageKey = null;
            
            // Skip messages if necessary
            while (receiveMessageNumber < messageNumber) {
                byte[][] keys = KDF.deriveMessageKeys(receivingChainKey);
                skippedMessageKeys.computeIfAbsent(receiveMessageNumber, k -> new ArrayList<>()).add(keys[0]);
                receivingChainKey = keys[1];
                receiveMessageNumber++;
            }

            if (receiveMessageNumber == messageNumber) {
                // Current message
                byte[][] keys = KDF.deriveMessageKeys(receivingChainKey);
                messageKey = keys[0];
                receivingChainKey = keys[1];
                receiveMessageNumber++;
            } else {
                // Check skipped messages
                List<byte[]> skippedKeys = skippedMessageKeys.get(messageNumber);
                if (skippedKeys != null && !skippedKeys.isEmpty()) {
                    messageKey = skippedKeys.remove(0);
                    if (skippedKeys.isEmpty()) {
                        skippedMessageKeys.remove(messageNumber);
                    }
                }
            }

            if (messageKey == null) {
                throw new RuntimeException("Could not derive message key for message number: " + messageNumber);
            }

            // Debug: Print message key hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            System.out.println("DECRYPTION - Message key hash: " +
                    Base64.getEncoder().encodeToString(digest.digest(messageKey)));

            // Decrypt
            byte[] plaintextBytes = AEAD.decrypt(messageKey, ciphertext, header);
            totalRatchetSteps++;

            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt message: " + e.getMessage(), e);
        }
    }

    private void performDHRatchet(PublicKey newRemotePublicKey) {
        try {
            // Store the previous chain length
            previousChainLength = sendMessageNumber;
            
            // Generate new DH key pair
            DHKeyPair = DiffieHellman.generateKeyPair();
            DHRemotePublicKey = newRemotePublicKey;

            // Calculate new shared secret
            byte[] sharedSecret = DiffieHellman.calculateSharedSecret(DHKeyPair.getPrivate(), DHRemotePublicKey);

            // Derive new root key and receiving chain key
            byte[][] keys = KDF.deriveKeys(rootKey, sharedSecret);
            rootKey = keys[0];
            receivingChainKey = keys[1];

            // Reset message numbers for new chain
            receiveMessageNumber = 0;
            sendMessageNumber = 0;
            
            totalDHRatchets++;
            System.out.println("[DEBUG] DH Ratchet completed. Total DH ratchets: " + totalDHRatchets);
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform DH ratchet: " + e.getMessage(), e);
        }
    }

    private byte[] createHeader() {
        try {
            byte[] publicKeyBytes = DHKeyPair.getPublic().getEncoded();
            ByteBuffer buffer = ByteBuffer.allocate(publicKeyBytes.length + 4);
            buffer.put(publicKeyBytes);
            buffer.putInt(sendMessageNumber);
            return buffer.array();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create header: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> parseHeader(byte[] header) {
        try {
            int publicKeyLength = header.length - 4;
            byte[] publicKeyBytes = new byte[publicKeyLength];
            System.arraycopy(header, 0, publicKeyBytes, 0, publicKeyLength);
            
            ByteBuffer buffer = ByteBuffer.wrap(header, publicKeyLength, 4);
            int messageNumber = buffer.getInt();
            
            PublicKey publicKey = DiffieHellman.decodePublicKey(publicKeyBytes);
            
            Map<String, Object> result = new HashMap<>();
            result.put("publicKey", publicKey);
            result.put("messageNumber", messageNumber);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse header: " + e.getMessage(), e);
        }
    }

    private int getHeaderSize() {
        // Calcula dinamicamente o tamanho do header baseado na chave pública atual
        return DHKeyPair.getPublic().getEncoded().length + 4;
    }

    public long getTotalRatchetSteps() {
        return totalRatchetSteps;
    }

    public long getTotalDHRatchets() {
        return totalDHRatchets;
    }

    public boolean isInitialized() {
        return initialKeyExchangeCompleted;
    }
}