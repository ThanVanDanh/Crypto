package model.hash;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    public String checkSum(String input, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        BigInteger bi = new BigInteger(1, digest);
        String hashText = bi.toString(16);
        while (hashText.length() < 32) {
            hashText = "0" + hashText;
        }
        return hashText;
    }

    public String hashFile(String path, String algorithm) throws NoSuchAlgorithmException, FileNotFoundException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        InputStream input = new BufferedInputStream(new FileInputStream(path));
        DigestInputStream digestInput = new DigestInputStream(input, md);
        byte[] buffer = new byte[1024];
        int read;
        do {
            try {
                read = digestInput.read(buffer);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } while (read != -1);
        BigInteger bi = new BigInteger(1, digestInput.getMessageDigest().digest());
        return bi.toString(16);
    }
}
