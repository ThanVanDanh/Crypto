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
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAKey;

public class RsaAlgorithm {
    private static final int[] KEY_SIZES = {2048, 3072, 4096};
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int FILE_AES_KEY_SIZE_BITS = 128;
    private static final int FILE_IV_SIZE_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PublicKey generatedPublicKey;
    private PrivateKey generatedPrivateKey;

    public String getPublicKey() {
        return generatedPublicKey == null ? "" : CryptoUtils.toBase64(generatedPublicKey.getEncoded());
    }

    public String getPrivateKey() {
        return generatedPrivateKey == null ? "" : CryptoUtils.toBase64(generatedPrivateKey.getEncoded());
    }

    public String encrypt(String data, PublicKey publicKey) throws Exception {
        if (publicKey == null) throw new IllegalArgumentException("Chua truyen public key.");
        byte[] plainBytes = data.getBytes(StandardCharsets.UTF_8);
        int maxLength = maxPlaintextLength(publicKey);
        if (plainBytes.length > maxLength)
            throw new IllegalArgumentException("RSA chi ma hoa du lieu ngan, toi da " + maxLength + " bytes voi key hien tai.");
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return CryptoUtils.toBase64(cipher.doFinal(plainBytes));
    }

    public String decrypt(String data, PrivateKey privateKey) throws Exception {
        if (privateKey == null) throw new IllegalArgumentException("Chua truyen private key.");
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(CryptoUtils.fromBase64(data)), StandardCharsets.UTF_8);
    }

    public void encryptFile(String inputPath, String outputPath, PublicKey publicKey) throws Exception {
        if (publicKey == null) throw new IllegalArgumentException("Chua truyen public key.");
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(FILE_AES_KEY_SIZE_BITS);
        SecretKey aesKey = keyGen.generateKey();

        byte[] iv = new byte[FILE_IV_SIZE_BYTES];
        RANDOM.nextBytes(iv);

        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)))) {
            out.writeUTF(CryptoUtils.toBase64(rsaCipher.doFinal(aesKey.getEncoded())));
            out.writeUTF(CryptoUtils.toBase64(rsaCipher.doFinal(iv)));
            Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputPath));
                 CipherOutputStream co = new CipherOutputStream(out, aesCipher)) {
                CryptoUtils.transferStream(in, co);
            }
        }
    }

    public void decryptFile(String inputPath, String outputPath, PrivateKey privateKey) throws Exception {
        if (privateKey == null) throw new IllegalArgumentException("Chua truyen private key.");
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputPath)))) {
            byte[] keyBytes = rsaCipher.doFinal(CryptoUtils.fromBase64(in.readUTF()));
            byte[] ivBytes = rsaCipher.doFinal(CryptoUtils.fromBase64(in.readUTF()));
            SecretKeySpec aesKey = new SecretKeySpec(keyBytes, "AES");
            Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(ivBytes));
            try (CipherInputStream ci = new CipherInputStream(in, aesCipher);
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath))) {
                CryptoUtils.transferStream(ci, out);
            }
        }
    }

    public void genKeyPair(int keySize) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize);
        KeyPair keyPair = generator.generateKeyPair();
        generatedPublicKey = keyPair.getPublic();
        generatedPrivateKey = keyPair.getPrivate();
    }

    public int[] supportedKeySizes() {
        return KEY_SIZES.clone();
    }

    private int maxPlaintextLength(PublicKey publicKey) {
        if (publicKey instanceof RSAKey rsaKey) {
            return (rsaKey.getModulus().bitLength() + 7) / 8 - 11;
        }
        return 0;
    }
}
