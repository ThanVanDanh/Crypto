package model.asymmetric;

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
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;

public class RsaAlgorithm {
    private static final int[] KEY_SIZES = new int[]{2048, 3072, 4096};
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int FILE_SYMMETRIC_KEY_SIZE = 128;
    private static final int FILE_IV_SIZE_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private KeyPair keyPair;

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKeyBase64() {
        return publicKey == null ? "" : CryptoUtils.toBase64(publicKey.getEncoded());
    }

    public String getPrivateKeyBase64() {
        return privateKey == null ? "" : CryptoUtils.toBase64(privateKey.getEncoded());
    }

    public String encryptBase64(String data) throws Exception {
        return CryptoUtils.toBase64(encrypt(data));
    }

    public byte[] encrypt(String data) throws Exception {
        if (publicKey == null) {
            throw new IllegalStateException("Chua nap public key.");
        }
        byte[] plainBytes = data.getBytes(StandardCharsets.UTF_8);
        int maxLength = maxPlaintextLength(publicKey);
        if (plainBytes.length > maxLength) {
            throw new IllegalArgumentException("RSA chi ma hoa du lieu ngan, toi da " + maxLength + " bytes voi key hien tai.");
        }
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plainBytes);
    }

    public String decrypt(String data) throws Exception {
        if (privateKey == null) {
            throw new IllegalStateException("Chua nap private key.");
        }
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plainBytes = cipher.doFinal(CryptoUtils.fromBase64(data));
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    public void encryptFile(String inputPath, String outputPath) throws Exception {
        if (publicKey == null) {
            throw new IllegalStateException("Chua nap public key.");
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(FILE_SYMMETRIC_KEY_SIZE);
        SecretKey secretKey = keyGenerator.generateKey();

        byte[] iv = new byte[FILE_IV_SIZE_BYTES];
        RANDOM.nextBytes(iv);

        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)))) {
            out.writeUTF(CryptoUtils.toBase64(rsaCipher.doFinal(secretKey.getEncoded())));
            out.writeUTF(CryptoUtils.toBase64(rsaCipher.doFinal(iv)));

            Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

            try (InputStream in = new BufferedInputStream(new FileInputStream(inputPath));
                 CipherOutputStream cipherOut = new CipherOutputStream(out, aesCipher)) {
                processFile(in, cipherOut);
            }
        }
    }

    public void decryptFile(String inputPath, String outputPath) throws Exception {
        if (privateKey == null) {
            throw new IllegalStateException("Chua nap private key.");
        }
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputPath)))) {
            byte[] keyBytes = rsaCipher.doFinal(CryptoUtils.fromBase64(in.readUTF()));
            byte[] ivBytes = rsaCipher.doFinal(CryptoUtils.fromBase64(in.readUTF()));

            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));

            try (CipherInputStream cipherIn = new CipherInputStream(in, aesCipher);
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath))) {
                processFile(cipherIn, out);
            }
        }
    }

    public void genKeyPair(int keySize) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize);
        keyPair = generator.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

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

    private void processFile(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }
}
