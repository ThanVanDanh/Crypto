package model.symmetric;

import common.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;

public class JceSymmetricAlgorithm {
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
    private byte[] currentKey;
    private byte[] currentIv;

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

    public void loadKey(byte[] key, byte[] iv) {
        this.currentKey = key.clone();
        this.currentIv = iv.clone();
    }

    public String encryptText(String plaintext) throws Exception {
        return CryptoUtils.toBase64(encryptRaw(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    public String decryptText(String ciphertextBase64) throws Exception {
        byte[] plaintext = decryptRaw(CryptoUtils.fromBase64(ciphertextBase64));
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    private byte[] encryptRaw(byte[] plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        init(cipher, Cipher.ENCRYPT_MODE);
        return cipher.doFinal(plaintext);
    }

    private byte[] decryptRaw(byte[] ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        init(cipher, Cipher.DECRYPT_MODE);
        return cipher.doFinal(ciphertext);
    }

    public void encryptFile(String inputPath, String outputPath) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        init(cipher, Cipher.ENCRYPT_MODE);
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputPath));
             CipherOutputStream out = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)), cipher)) {
            processFile(in, out);
        }
    }

    public void decryptFile(String inputPath, String outputPath) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        init(cipher, Cipher.DECRYPT_MODE);
        try (CipherInputStream in = new CipherInputStream(new BufferedInputStream(new FileInputStream(inputPath)), cipher);
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath))) {
            processFile(in, out);
        }
    }

    public int[] supportedKeySizes() {
        return keySizes.clone();
    }

    public int ivSizeBytes() {
        return ivSizeBytes;
    }

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

    private void init(Cipher cipher, int mode) throws Exception {
        if (currentKey == null) {
            throw new IllegalStateException("Chua nap key.");
        }
        SecretKeySpec keySpec = new SecretKeySpec(currentKey, keyAlgorithm);
        AlgorithmParameterSpec parameterSpec = parameterSpec(currentIv);
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

    private void processFile(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }
}
