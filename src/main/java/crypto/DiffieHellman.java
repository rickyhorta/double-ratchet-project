// crypto/DiffieHellman.java
package crypto;

import java.security.*;
import java.security.spec.*;
import javax.crypto.KeyAgreement;

public class DiffieHellman {
    private static final String KEY_AGREEMENT_ALGORITHM = "ECDH";
    private static final String KEY_PAIR_ALGORITHM = "EC";
    private static final String CURVE_NAME = "secp256r1"; // NIST P-256
    
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
        ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE_NAME);
        keyPairGenerator.initialize(ecSpec, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }
    
    public static byte[] calculateSharedSecret(PrivateKey privateKey, PublicKey publicKey) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM);
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        return keyAgreement.generateSecret();
    }
    
    public static PublicKey decodePublicKey(byte[] encodedKey) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_PAIR_ALGORITHM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
        return keyFactory.generatePublic(keySpec);
    }
}