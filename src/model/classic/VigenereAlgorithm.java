package model.classic;

import common.AlphabetConstants;

import java.security.SecureRandom;

public class VigenereAlgorithm implements ClassicAlgorithm {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String genKey(boolean isVN) {
        String alphabet = alphabetFor(isVN);
        int length = 6 + RANDOM.nextInt(5);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    @Override
    public String encrypt(String text, String key, boolean isVN) {
        return handle(text, key, alphabetFor(isVN), true);
    }

    @Override
    public String decrypt(String text, String key, boolean isVN) {
        return handle(text, key, alphabetFor(isVN), false);
    }

    @Override
    public boolean isValidKey(String key, boolean isVN) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        String alphabet = alphabetFor(isVN);
        for (char c : key.toCharArray()) {
            if (alphabet.indexOf(c) < 0) {
                return false;
            }
        }
        return true;
    }

    private String alphabetFor(boolean isVN) {
        return isVN ? AlphabetConstants.ALPHABET_VIE : AlphabetConstants.ALPHABET_ENG;
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

