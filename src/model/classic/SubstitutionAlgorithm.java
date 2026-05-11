package model.classic;

import common.AlphabetConstants;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubstitutionAlgorithm implements ClassicAlgorithm {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String encryptENG(String plaintext, String key) {
        return handleEncrypt(plaintext, key, AlphabetConstants.ALPHABET_ENG);
    }

    @Override
    public String decryptENG(String ciphertext, String key) {
        return handleDecrypt(ciphertext, key, AlphabetConstants.ALPHABET_ENG);
    }

    @Override
    public String encryptVIE(String plaintext, String key) {
        return handleEncrypt(plaintext, key, AlphabetConstants.ALPHABET_VIE);
    }

    @Override
    public String decryptVIE(String ciphertext, String key) {
        return handleDecrypt(ciphertext, key, AlphabetConstants.ALPHABET_VIE);
    }

    public String genKey(String language) {
        String alphabet = alphabetFor(language);
        List<Character> chars = new ArrayList<>();
        for (char c : alphabet.toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars, RANDOM);
        StringBuilder sb = new StringBuilder(chars.size());
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    public boolean isValidKey(String key, String language) {
        if (key == null) {
            return false;
        }
        String alphabet = alphabetFor(language);
        String trimmed = key.trim();
        if (trimmed.length() != alphabet.length()) {
            return false;
        }
        Set<Character> used = new HashSet<>();
        for (char c : trimmed.toCharArray()) {
            if (alphabet.indexOf(c) < 0 || !used.add(c)) {
                return false;
            }
        }
        return true;
    }

    public String keyHint(String language) {
        int n = alphabetFor(language).length();
        return "Khoa Substitution phai la hoan vi du " + n + " ky tu cua bang chu cai dang chon.";
    }

    private String handleEncrypt(String input, String key, String alphabet) {
        if (input == null) {
            return "";
        }
        String fixedKey = key.trim();
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            int idx = alphabet.indexOf(c);
            sb.append(idx >= 0 ? fixedKey.charAt(idx) : c);
        }
        return sb.toString();
    }

    private String handleDecrypt(String input, String key, String alphabet) {
        if (input == null) {
            return "";
        }
        String fixedKey = key.trim();
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            int idx = fixedKey.indexOf(c);
            sb.append(idx >= 0 ? alphabet.charAt(idx) : c);
        }
        return sb.toString();
    }

    private String alphabetFor(String language) {
        return "VIE".equalsIgnoreCase(language)
                ? AlphabetConstants.ALPHABET_VIE
                : AlphabetConstants.ALPHABET_ENG;
    }
}
