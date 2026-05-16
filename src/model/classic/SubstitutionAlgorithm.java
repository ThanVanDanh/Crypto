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
    public String genKey(boolean isVN) {
        String alphabet = alphabetFor(isVN);
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

    @Override
    public String encrypt(String text, String key, boolean isVN) {
        return handleEncrypt(text, key, alphabetFor(isVN));
    }

    @Override
    public String decrypt(String text, String key, boolean isVN) {
        return handleDecrypt(text, key, alphabetFor(isVN));
    }

    @Override
    public boolean isValidKey(String key, boolean isVN) {
        if (key == null) {
            return false;
        }
        String alphabet = alphabetFor(isVN);
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

    private String alphabetFor(boolean isVN) {
        return isVN ? AlphabetConstants.ALPHABET_VIE : AlphabetConstants.ALPHABET_ENG;
    }
}
