package model.symmetric;

import common.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class JceSymmetricAlgorithm {
    private final String keyAlgorithm;
    private final int[] keySizes;
    private final int[] keyBytes;
    private String mode = "CBC";
    private String padding = "PKCS5Padding";
    private byte[] currentKey;

    public JceSymmetricAlgorithm(String keyAlgorithm, int[] keySizes, int[] keyBytes) {
        this.keyAlgorithm = keyAlgorithm;
        this.keySizes = keySizes.clone();
        this.keyBytes = keyBytes.clone();
    }

    public void updateConfig(String mode, String padding) {
        this.mode = mode;
        this.padding = padding;
    }

    public void loadKey(byte[] key) {
        this.currentKey = key.clone();
    }

    public String encryptText(String plaintext) throws Exception {
        return CryptoUtils.toBase64(encryptBytes(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    public String decryptText(String ciphertextBase64) throws Exception {
        byte[] plaintext = decryptBytes(CryptoUtils.fromBase64(ciphertextBase64));
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    public void encryptFile(String inputPath, String outputPath) throws Exception {
        Cipher cipher = Cipher.getInstance(getTransformation());
        SecretKeySpec keySpec = keySpec();
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputPath));
             OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(outputPath))) {
            if (requiresIv(cipher)) {
                byte[] iv = CryptoUtils.randomBytes(cipher.getBlockSize());
                fileOut.write(iv);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            }
            try (CipherOutputStream out = new CipherOutputStream(fileOut, cipher)) {
                processFile(in, out);
            }
        }
    }

    public void decryptFile(String inputPath, String outputPath) throws Exception {
        Cipher cipher = Cipher.getInstance(getTransformation());
        SecretKeySpec keySpec = keySpec();
        try (DataInputStream fileIn = new DataInputStream(new BufferedInputStream(new FileInputStream(inputPath)));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath))) {
            if (requiresIv(cipher)) {
                byte[] iv = new byte[cipher.getBlockSize()];
                fileIn.readFully(iv);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
            }
            try (CipherInputStream in = new CipherInputStream(fileIn, cipher)) {
                processFile(in, out);
            }
        }
    }

    public int[] supportedKeySizes() {
        return keySizes.clone();
    }

    public int keySizeBytes(int keySizeBits) {
        for (int i = 0; i < keySizes.length; i++) {
            if (keySizes[i] == keySizeBits) {
                return keyBytes[i];
            }
        }
        return keySizeBits / 8;
    }

    public static boolean isAvailable(String keyAlgorithm) {
        try {
            Cipher.getInstance(keyAlgorithm + "/CBC/PKCS5Padding");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isTransformationSupported(String mode, String padding) {
        try {
            Cipher.getInstance(keyAlgorithm + "/" + mode + "/" + padding);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private byte[] encryptBytes(byte[] plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance(getTransformation());
        SecretKeySpec keySpec = keySpec();
        if (!requiresIv(cipher)) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(plaintext);
        }

        byte[] iv = CryptoUtils.randomBytes(cipher.getBlockSize());
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        byte[] ciphertext = cipher.doFinal(plaintext);
        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
        return result;
    }

    private byte[] decryptBytes(byte[] input) throws Exception {
        Cipher cipher = Cipher.getInstance(getTransformation());
        SecretKeySpec keySpec = keySpec();
        if (!requiresIv(cipher)) {
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(input);
        }

        int ivSize = cipher.getBlockSize();
        if (input.length < ivSize) {
            throw new IllegalArgumentException("Du lieu ma hoa khong hop le.");
        }
        byte[] iv = new byte[ivSize];
        byte[] ciphertext = new byte[input.length - ivSize];
        System.arraycopy(input, 0, iv, 0, ivSize);
        System.arraycopy(input, ivSize, ciphertext, 0, ciphertext.length);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return cipher.doFinal(ciphertext);
    }

    private SecretKeySpec keySpec() {
        if (currentKey == null) {
            throw new IllegalStateException("Chua nap key.");
        }
        return new SecretKeySpec(currentKey, keyAlgorithm);
    }

    private String getTransformation() {
        return keyAlgorithm + "/" + mode + "/" + padding;
    }

    private boolean requiresIv(Cipher cipher) {
        return !"ECB".equalsIgnoreCase(mode) && cipher.getBlockSize() > 0;
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
