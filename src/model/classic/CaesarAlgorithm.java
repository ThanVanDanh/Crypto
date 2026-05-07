package model.classic;

import common.AlphabetConstants;

import java.security.SecureRandom;

public class CaesarAlgorithm implements ClassicAlgorithm {
    private static final SecureRandom RANDOM = new SecureRandom();

    public String encryptENG(String plaintext, String key) {
        return handle(plaintext, parseKey(key), AlphabetConstants.ALPHABET_ENG);
    }

    public String encryptVIE(String plaintext, String key) {
        return handle(plaintext, parseKey(key), AlphabetConstants.ALPHABET_VIE);
    }

    public String decryptENG(String ciphertext, String key) {
        return handle(ciphertext, -parseKey(key), AlphabetConstants.ALPHABET_ENG);
    }

    public String decryptVIE(String ciphertext, String key) {
        return handle(ciphertext, -parseKey(key), AlphabetConstants.ALPHABET_VIE);
    }

    public boolean isValidKey(String key, String language) {
        try {
            parseKey(key);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public String keyHint(String language) {
        return "Khóa Caesar phải là số nguyên (ví dụ: 3, 11, -2).";
    }

    public String genKey(String language) {
        String alphabet = "VIE".equalsIgnoreCase(language)
                ? AlphabetConstants.ALPHABET_VIE
                : AlphabetConstants.ALPHABET_ENG;
        return String.valueOf(RANDOM.nextInt(alphabet.length()));
    }

    private String handle(String input, int k, String alphabet) {
        if (input == null) {
            return null;
        }
        int n = alphabet.length();
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            int idx = alphabet.indexOf(c);
            if (idx >= 0) {
                sb.append(alphabet.charAt(((idx + k) % n + n) % n));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private int parseKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        String trimmed = key.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty");
        }
        return Integer.parseInt(trimmed);
    }
}
