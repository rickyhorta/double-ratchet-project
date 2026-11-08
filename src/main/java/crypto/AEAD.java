package crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.security.MessageDigest;

public class AEAD {
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128; // in bits

    public static byte[] encrypt(byte[] key, byte[] plaintext, byte[] associatedData) {
        try {
            // Create a new random IV for each encryption
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // Debug: Log key and IV hashes
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            System.out.println("AEAD Encrypt - Key hash: " +
                    Base64.getEncoder().encodeToString(digest.digest(key)));
            System.out.println("AEAD Encrypt - IV: " +
                    Base64.getEncoder().encodeToString(iv));

            // Create GCM parameter specification
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // Initialize cipher for encryption
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

            // Add associated data
            if (associatedData != null) {
                cipher.updateAAD(associatedData);
            }

            // Encrypt the plaintext
            byte[] ciphertext = cipher.doFinal(plaintext);

            // Combine IV and ciphertext
            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    public static byte[] decrypt(byte[] key, byte[] ciphertext, byte[] associatedData) {
        try {
            // Extract IV from the ciphertext
            if (ciphertext.length < GCM_IV_LENGTH) {
                throw new RuntimeException("Invalid ciphertext. IV not included.");
            }

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[ciphertext.length - GCM_IV_LENGTH];

            System.arraycopy(ciphertext, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

            // Debug: Log key and IV hashes
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            System.out.println("AEAD Decrypt - Key hash: " +
                    Base64.getEncoder().encodeToString(digest.digest(key)));
            System.out.println("AEAD Decrypt - IV: " +
                    Base64.getEncoder().encodeToString(iv));

            // Create GCM parameter specification
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            // Add associated data
            if (associatedData != null) {
                cipher.updateAAD(associatedData);
            }

            // Decrypt the ciphertext
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }
}