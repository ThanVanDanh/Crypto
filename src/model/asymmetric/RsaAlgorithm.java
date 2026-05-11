package model.asymmetric;

import common.CryptoUtils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;

public class RsaAlgorithm implements AsymmetricAlgorithm {
    private static final int[] KEY_SIZES = new int[]{2048, 3072, 4096};

    @Override
    public String encrypt(String plaintext, PublicKey publicKey) throws Exception {
        byte[] input = plaintext.getBytes(StandardCharsets.UTF_8);
        int maxLength = maxPlaintextLength(publicKey);
        if (input.length > maxLength) {
            throw new IllegalArgumentException("RSA chỉ mã hóa dữ liệu ngắn, tối đa " + maxLength + " bytes với key hiện tại.");
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] ciphertext = cipher.doFinal(input);
        return CryptoUtils.toBase64(ciphertext);
    }

    @Override
    public String decrypt(String ciphertextBase64, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plaintext = cipher.doFinal(CryptoUtils.fromBase64(ciphertextBase64));
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    @Override
    public KeyPair generateKeyPair(int keySize) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize);
        return generator.generateKeyPair();
    }

    @Override
    public int[] supportedKeySizes() {
        return KEY_SIZES.clone();
    }

    private int maxPlaintextLength(PublicKey publicKey) {
        if (publicKey instanceof RSAKey) {
            int keyBytes = (((RSAKey) publicKey).getModulus().bitLength() + 7) / 8;
            return keyBytes - 11;
        }
        return 0;
    }
}

