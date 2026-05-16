package common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtils {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    public static String toBase64(byte[] data) {
        return data == null ? "" : BASE64_ENCODER.encodeToString(data);
    }

    public static byte[] fromBase64(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new byte[0];
        }
        return BASE64_DECODER.decode(value.trim());
    }

    public static byte[] utf8(String value) {
        return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
    }

    public static String fromUtf8(byte[] value) {
        return value == null ? "" : new String(value, StandardCharsets.UTF_8);
    }

    public static String toHex(byte[] data) {
        if (data == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] readFile(String path) throws IOException {
        return Files.readAllBytes(Path.of(path));
    }

    public static void writeFile(String path, byte[] data) throws IOException {
        Files.write(Path.of(path), data == null ? new byte[0] : data);
    }

    public static void writeTextFile(String path, String content) throws IOException {
        writeFile(path, utf8(content));
    }

    public static String readTextFile(String path) throws IOException {
        return fromUtf8(readFile(path));
    }

    public static File withTxtExtension(File file) {
        if (file == null || file.getName().toLowerCase().endsWith(".txt")) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + ".txt");
    }
}
