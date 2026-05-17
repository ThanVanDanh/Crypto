package model.hash;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashAlgorithm {
    public String checkSum(String input, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, digest);
        return number.toString(16);
    }

    public String hashFile(String path, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream is = new BufferedInputStream(new FileInputStream(path));
             DigestInputStream dis = new DigestInputStream(is, digest)) {
            byte[] buffer = new byte[1024];
            int read;
            do{
                read = dis.read(buffer);
            }
            while (read != -1);
            return new BigInteger(1, digest.digest()).toString(16);
        }
    }
}
