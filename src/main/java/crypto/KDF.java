// crypto/KDF.java
package crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class KDF {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final byte[] DERIVE_ROOT_KEY_INFO = "ROOT_KEY".getBytes();
    private static final byte[] DERIVE_CHAIN_KEY_1_INFO = "CHAIN_KEY_1".getBytes();
    private static final byte[] DERIVE_CHAIN_KEY_2_INFO = "CHAIN_KEY_2".getBytes();
    private static final byte[] DERIVE_MESSAGE_KEY_INFO = "MESSAGE_KEY".getBytes();
    private static final byte[] DERIVE_NEXT_CHAIN_KEY_INFO = "NEXT_CHAIN_KEY".getBytes();

    public static byte[][] deriveKeys(byte[] rootKey, byte[] sharedSecret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        System.out.println("[DEBUG-KDF] Deriving keys");
        System.out.println("[DEBUG-KDF] Root key length: " + rootKey.length);
        System.out.println("[DEBUG-KDF] Shared secret length: " + sharedSecret.length);
        System.out.println("[DEBUG-KDF] Root key hash: " + 
            Base64.getEncoder().encodeToString(java.security.MessageDigest.getInstance("SHA-256").digest(rootKey)));
        System.out.println("[DEBUG-KDF] Shared secret hash: " + 
            Base64.getEncoder().encodeToString(java.security.MessageDigest.getInstance("SHA-256").digest(sharedSecret)));
        
        // HKDF-like key derivation
        // Derive: rootKey, chainKey1, chainKey2
        byte[] newRootKey = hmacDerive(rootKey, sharedSecret, DERIVE_ROOT_KEY_INFO);
        byte[] chainKey1 = hmacDerive(rootKey, sharedSecret, DERIVE_CHAIN_KEY_1_INFO);
        byte[] chainKey2 = hmacDerive(rootKey, sharedSecret, DERIVE_CHAIN_KEY_2_INFO);

        System.out.println("[DEBUG-KDF] New root key hash: " + 
            Base64.getEncoder().encodeToString(java.security.MessageDigest.getInstance("SHA-256").digest(newRootKey)));
        System.out.println("[DEBUG-KDF] Chain key 1 hash: " + 
            Base64.getEncoder().encodeToString(java.security.MessageDigest.getInstance("SHA-256").digest(chainKey1)));
        System.out.println("[DEBUG-KDF] Chain key 2 hash: " + 
            Base64.getEncoder().encodeToString(java.security.MessageDigest.getInstance("SHA-256").digest(chainKey2)));

        return new byte[][] { newRootKey, chainKey1, chainKey2 };
    }

    public static byte[][] deriveMessageKeys(byte[] chainKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        // Derive: messageKey, nextChainKey
        byte[] messageKey = hmacDerive(chainKey, new byte[0], DERIVE_MESSAGE_KEY_INFO);
        byte[] nextChainKey = hmacDerive(chainKey, new byte[0], DERIVE_NEXT_CHAIN_KEY_INFO);

        return new byte[][] { messageKey, nextChainKey };
    }

    private static byte[] hmacDerive(byte[] key, byte[] input, byte[] info)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_ALGORITHM);
        mac.init(keySpec);

        // Debug the derivation inputs
        System.out.println("[DEBUG-KDF] HMAC derive with info: " + new String(info));
        
        // Process input
        mac.update(input);
        // Process info
        mac.update(info);

        byte[] result = mac.doFinal();
        System.out.println("[DEBUG-KDF] HMAC result length: " + result.length);
        
        return result;
    }
}