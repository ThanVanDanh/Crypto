package model.asymmetric;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface AsymmetricAlgorithm {
    String encrypt(String plaintext, PublicKey publicKey) throws Exception;

    String decrypt(String ciphertextBase64, PrivateKey privateKey) throws Exception;

    KeyPair generateKeyPair(int keySize) throws Exception;

    int[] supportedKeySizes();
}

