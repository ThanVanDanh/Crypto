package common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class CryptoUtils {
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    public static String toBase64(byte[] data) {
        return data == null ? "" : BASE64_ENCODER.encodeToString(data);
    }

    public static byte[] fromBase64(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new byte[0];
        }
        return BASE64_DECODER.decode(value.trim());
    }

    public static void writeTextFile(String path, String content) throws IOException {
        byte[] bytes = content == null ? new byte[0] : content.getBytes(StandardCharsets.UTF_8);
        Files.write(Path.of(path), bytes);
    }

    public static String readTextFile(String path) throws IOException {
        return new String(Files.readAllBytes(Path.of(path)), StandardCharsets.UTF_8);
    }

    public static File withTxtExtension(File file) {
        if (file == null || file.getName().toLowerCase().endsWith(".txt")) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + ".txt");
    }

    public static void transferStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }
}
