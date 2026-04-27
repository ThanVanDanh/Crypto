package model.classic;

import java.security.SecureRandom;

public class CaesarAlgorithm implements ClassicAlgorithm {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String key() {
        return "caesar";
    }

    @Override
    public String displayName() {
        return "Caesar Cipher";
    }

    @Override
    public String encrypt(String plaintext, String key) {
        return transform(plaintext, parseShift(key));
    }

    @Override
    public String decrypt(String ciphertext, String key) {
        return transform(ciphertext, -parseShift(key));
    }

    @Override
    public String generateKey() {
        return String.valueOf(RANDOM.nextInt(26));
    }

    @Override
    public boolean isValidKey(String key) {
        try {
            parseShift(key);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    @Override
    public String keyHint() {
        return "Khóa Caesar phải là số nguyên (ví dụ: 3, 11, -2).";
    }

    private String transform(String input, int shift) {
        StringBuilder out = new StringBuilder(input.length());
        int normalized = Math.floorMod(shift, 26);
        for (char ch : input.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                out.append((char) ('A' + (ch - 'A' + normalized) % 26));
            } else if (Character.isLowerCase(ch)) {
                out.append((char) ('a' + (ch - 'a' + normalized) % 26));
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    private int parseShift(String key) {
        return Integer.parseInt(key.trim());
    }
}

