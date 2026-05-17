package model.classic;

import common.AlphabetConstants;

import java.security.SecureRandom;

public class CaesarAlgorithm implements ClassicAlgorithm {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String genKey(boolean isVN) {
        return String.valueOf(RANDOM.nextInt(alphabetFor(isVN).length()));
    }

    @Override
    public String encrypt(String text, String key, boolean isVN) {
        return handle(text, parseKey(key), alphabetFor(isVN));
    }

    @Override
    public String decrypt(String text, String key, boolean isVN) {
        return handle(text, -parseKey(key), alphabetFor(isVN));
    }

    @Override
    public boolean isValidKey(String key, boolean isVN) {
        try {
            parseKey(key);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String alphabetFor(boolean isVN) {
        return isVN ? AlphabetConstants.ALPHABET_VIE : AlphabetConstants.ALPHABET_ENG;
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
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key khong duoc de trong");
        }
        return Integer.parseInt(key.trim());
    }
}
