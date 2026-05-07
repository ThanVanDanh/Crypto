package model.classic;

import common.AlphabetConstants;

import java.security.SecureRandom;

public class VigenereAlgorithm implements ClassicAlgorithm {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String encryptENG(String plaintext, String key) {
        return handle(plaintext, key, AlphabetConstants.ALPHABET_ENG, true);
    }

    @Override
    public String decryptENG(String ciphertext, String key) {
        return handle(ciphertext, key, AlphabetConstants.ALPHABET_ENG, false);
    }

    @Override
    public String encryptVIE(String plaintext, String key) {
        return handle(plaintext, key, AlphabetConstants.ALPHABET_VIE, true);
    }

    @Override
    public String decryptVIE(String ciphertext, String key) {
        return handle(ciphertext, key, AlphabetConstants.ALPHABET_VIE, false);
    }

    public boolean isValidKey(String key, String language) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        String alphabet = alphabetFor(language);
        for (char c : key.toCharArray()) {
            if (alphabet.indexOf(c) < 0) {
                return false;
            }
        }
        return true;
    }

    public String keyHint(String language) {
        return "Khóa Vigenere là chuỗi ký tự thuộc bảng chữ cái (ví dụ: KEY, hello).";
    }

    public String genKey(String language) {
        String alphabet = alphabetFor(language);
        int length = 6 + RANDOM.nextInt(5);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private String alphabetFor(String language) {
        return "VIE".equalsIgnoreCase(language)
                ? AlphabetConstants.ALPHABET_VIE
                : AlphabetConstants.ALPHABET_ENG;
    }

    private String handle(String input, String key, String alphabet, boolean encrypt) {
        if (input == null) {
            return "";
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty");
        }
        int n = alphabet.length();
        StringBuilder sb = new StringBuilder(input.length());
        int keyIndex = 0;
        for (char c : input.toCharArray()) {
            int idx = alphabet.indexOf(c);
            if (idx >= 0) {
                int shift = alphabet.indexOf(key.charAt(keyIndex % key.length()));
                int result = encrypt ? (idx + shift) % n : (idx - shift + n) % n;
                sb.append(alphabet.charAt(result));
                keyIndex++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

