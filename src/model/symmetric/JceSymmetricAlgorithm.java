package model.symmetric;

import common.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;

public class JceSymmetricAlgorithm implements SymmetricAlgorithm {
    public static final String PARAM_NONE = "none";
    public static final String PARAM_IV = "iv";
    public static final String PARAM_GCM = "gcm";
    public static final String PARAM_CHACHA20 = "chacha20";

    private final String transformation;
    private final String keyAlgorithm;
    private final int[] keySizes;
    private final int[] keyBytes;
    private final int ivSizeBytes;
    private final String parameterType;

    public JceSymmetricAlgorithm(String transformation,
                                 String keyAlgorithm,
                                 int[] keySizes,
                                 int[] keyBytes,
                                 int ivSizeBytes,
                                 String parameterType) {
        this.transformation = transformation;
        this.keyAlgorithm = keyAlgorithm;
        this.keySizes = keySizes.clone();
        this.keyBytes = keyBytes.clone();
        this.ivSizeBytes = ivSizeBytes;
        this.parameterType = parameterType;
    }

    @Override
    public String encrypt(String plaintext, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        init(cipher, Cipher.ENCRYPT_MODE, key, iv);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return CryptoUtils.toBase64(ciphertext);
    }

    @Override
    public String decrypt(String ciphertextBase64, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        init(cipher, Cipher.DECRYPT_MODE, key, iv);
        byte[] plaintext = cipher.doFinal(CryptoUtils.fromBase64(ciphertextBase64));
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    @Override
    public int[] supportedKeySizes() {
        return keySizes.clone();
    }

    @Override
    public int ivSizeBytes() {
        return ivSizeBytes;
    }

    @Override
    public int keySizeBytes(int keySizeBits) {
        for (int i = 0; i < keySizes.length; i++) {
            if (keySizes[i] == keySizeBits) {
                return keyBytes[i];
            }
        }
        return keySizeBits / 8;
    }

    public static boolean isAvailable(String transformation) {
        try {
            Cipher.getInstance(transformation);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void init(Cipher cipher, int mode, byte[] key, byte[] iv) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        AlgorithmParameterSpec parameterSpec = parameterSpec(iv);
        if (parameterSpec == null) {
            cipher.init(mode, keySpec);
        } else {
            cipher.init(mode, keySpec, parameterSpec);
        }
    }

    private AlgorithmParameterSpec parameterSpec(byte[] iv) {
        if (PARAM_IV.equals(parameterType)) {
            return new IvParameterSpec(iv);
        }
        if (PARAM_GCM.equals(parameterType)) {
            return new GCMParameterSpec(128, iv);
        }
        if (PARAM_CHACHA20.equals(parameterType)) {
            return new ChaCha20ParameterSpec(iv, 1);
        }
        return null;
    }
}
