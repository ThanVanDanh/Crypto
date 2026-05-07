package model.symmetric;

import common.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class TripleDesAlgorithm implements SymmetricAlgorithm {
    private static final int[] KEY_SIZES = new int[]{112, 168};

    @Override
    public String encrypt(String plaintext, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "DESede"), new IvParameterSpec(iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return CryptoUtils.toBase64(ciphertext);
    }

    @Override
    public String decrypt(String ciphertextBase64, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "DESede"), new IvParameterSpec(iv));
        byte[] plaintext = cipher.doFinal(CryptoUtils.fromBase64(ciphertextBase64));
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    @Override
    public int[] supportedKeySizes() {
        return KEY_SIZES.clone();
    }

    @Override
    public int ivSizeBytes() {
        return 8;
    }

    @Override
    public int keySizeBytes(int keySizeBits) {
        if (keySizeBits == 112) {
            return 16;
        }
        if (keySizeBits == 168) {
            return 24;
        }
        return keySizeBits / 8;
    }
}
