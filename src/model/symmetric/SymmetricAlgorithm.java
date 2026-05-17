package model.symmetric;

import common.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class SymmetricAlgorithm {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String jceName;
    private final int[] keySizes;
    private final int[] keyBytes;
    private final String[] supportedModes;
    private final String[] supportedPaddings;

    private SecretKey secretKey;
    private String mode;
    private String padding;

    public SymmetricAlgorithm(String jceName, int[] keySizes, int[] keyBytes,
                              String[] supportedModes, String[] supportedPaddings) {
        this.jceName = jceName;
        this.keySizes = keySizes.clone();
        this.keyBytes = keyBytes.clone();
        this.supportedModes = supportedModes.clone();
        this.supportedPaddings = supportedPaddings.clone();
        this.mode = supportedModes[0];
        this.padding = supportedPaddings[0];
    }

    public SymmetricAlgorithm(String jceName, int[] keySizes, int[] keyBytes) {
        this.jceName = jceName;
        this.keySizes = keySizes.clone();
        this.keyBytes = keyBytes.clone();
        this.supportedModes = null;
        this.supportedPaddings = null;
    }

    public boolean isStreamCipher() {
        return supportedModes == null;
    }

    public String[] getSupportedModes() {
        return supportedModes == null ? new String[0] : supportedModes.clone();
    }

    public String[] getSupportedPaddings() {
        return supportedPaddings == null ? new String[0] : supportedPaddings.clone();
    }

    public void updateConfig(String mode, String padding) {
        if (!isStreamCipher()) {
            this.mode = mode;
            this.padding = padding;
        }
    }

    public void genKey(int keySizeBits) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(jceName);
        keyGen.init(keySizeBits);
        this.secretKey = keyGen.generateKey();
    }

    public void importKeyFromBase64(String base64Key) {
        byte[] raw = CryptoUtils.fromBase64(base64Key);
        this.secretKey = new SecretKeySpec(raw, 0, raw.length, jceName);
    }

    public String exportKeyToBase64() {
        return secretKey == null ? "" : CryptoUtils.toBase64(secretKey.getEncoded());
    }

    public void clearKey() {
        this.secretKey = null;
    }

    public String encryptText(String plainText) throws Exception {
        if (isStreamCipher()) {
            Cipher cipher = Cipher.getInstance(jceName);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return CryptoUtils.toBase64(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        }
        Cipher cipher = Cipher.getInstance(getTransformation());
        if (!requiresIv(cipher)) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return CryptoUtils.toBase64(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        }
        byte[] iv = new byte[cipher.getBlockSize()];
        RANDOM.nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + cipherBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);
        return CryptoUtils.toBase64(combined);
    }

    public String decryptText(String cipherText) throws Exception {
        byte[] combined = CryptoUtils.fromBase64(cipherText);
        if (isStreamCipher()) {
            Cipher cipher = Cipher.getInstance(jceName);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(combined), StandardCharsets.UTF_8);
        }
        Cipher cipher = Cipher.getInstance(getTransformation());
        if (!requiresIv(cipher)) {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(combined), StandardCharsets.UTF_8);
        }
        int ivSize = cipher.getBlockSize();
        if (combined.length < ivSize) throw new IllegalArgumentException("Du lieu ma hoa khong hop le.");
        byte[] iv = new byte[ivSize];
        byte[] cipherBytes = new byte[combined.length - ivSize];
        System.arraycopy(combined, 0, iv, 0, ivSize);
        System.arraycopy(combined, ivSize, cipherBytes, 0, cipherBytes.length);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
    }

    public void encryptFile(String filePath, String destFile) throws Exception {
        Cipher cipher = Cipher.getInstance(isStreamCipher() ? jceName : getTransformation());
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(filePath));
             BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(destFile))) {
            if (!isStreamCipher() && requiresIv(cipher)) {
                byte[] iv = new byte[cipher.getBlockSize()];
                RANDOM.nextBytes(iv);
                fos.write(iv);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            }
            try (CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
                CryptoUtils.transferStream(fis, cos);
            }
        }
    }

    public void decryptFile(String filePath, String destFile) throws Exception {
        Cipher cipher = Cipher.getInstance(isStreamCipher() ? jceName : getTransformation());
        try (DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
             BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(destFile))) {
            if (!isStreamCipher() && requiresIv(cipher)) {
                byte[] iv = new byte[cipher.getBlockSize()];
                fis.readFully(iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }
            try (CipherInputStream cis = new CipherInputStream(fis, cipher)) {
                CryptoUtils.transferStream(cis, fos);
            }
        }
    }

    public int[] supportedKeySizes() {
        return keySizes.clone();
    }

    public int keySizeBytes(int keySizeBits) {
        for (int i = 0; i < keySizes.length; i++) {
            if (keySizes[i] == keySizeBits) return keyBytes[i];
        }
        return keySizeBits / 8;
    }

    private String getTransformation() {
        return jceName + "/" + mode + "/" + padding;
    }

    private boolean requiresIv(Cipher cipher) {
        return !"ECB".equalsIgnoreCase(mode) && cipher.getBlockSize() > 0;
    }
}
