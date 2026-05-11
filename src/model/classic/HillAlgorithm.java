package model.classic;

import common.AlphabetConstants;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class HillAlgorithm implements ClassicAlgorithm {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String encryptENG(String plaintext, String key) {
        return handle(plaintext, parseKey(key), true, AlphabetConstants.ALPHABET_ENG);
    }

    @Override
    public String decryptENG(String ciphertext, String key) {
        return handle(ciphertext, parseKey(key), false, AlphabetConstants.ALPHABET_ENG);
    }

    @Override
    public String encryptVIE(String plaintext, String key) {
        return handle(plaintext, parseKey(key), true, AlphabetConstants.ALPHABET_VIE);
    }

    @Override
    public String decryptVIE(String ciphertext, String key) {
        return handle(ciphertext, parseKey(key), false, AlphabetConstants.ALPHABET_VIE);
    }

    public String genKey(String language) {
        int n = alphabetFor(language).length();
        int[] key;
        do {
            key = new int[]{
                    RANDOM.nextInt(n),
                    RANDOM.nextInt(n),
                    RANDOM.nextInt(n),
                    RANDOM.nextInt(n)
            };
        } while (!isInvertible(key, n));
        return key[0] + "," + key[1] + "," + key[2] + "," + key[3];
    }

    public boolean isValidKey(String key, String language) {
        try {
            return isInvertible(parseKey(key), alphabetFor(language).length());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public String keyHint(String language) {
        int n = alphabetFor(language).length();
        return "Khoa Hill 2x2 co dang a,b,c,d va det phai co nghich dao modulo " + n + ".";
    }

    private String handle(String input, int[] key, boolean encrypt, String alphabet) {
        if (input == null) {
            return "";
        }
        int n = alphabet.length();
        int[] matrix = encrypt ? key : inverseKey(key, n);
        StringBuilder result = new StringBuilder(input);
        List<Integer> positions = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int idx = alphabet.indexOf(c);
            if (idx >= 0) {
                positions.add(i);
                values.add(idx);
            }
        }

        if (values.size() % 2 != 0) {
            // them ky tu dem de du cap 2 ky tu cho ma tran hill 2x2
            positions.add(-1);
            values.add(0);
        }

        StringBuilder transformed = new StringBuilder(values.size());
        for (int i = 0; i < values.size(); i += 2) {
            appendPair(transformed, values.get(i), values.get(i + 1), matrix, alphabet);
        }

        for (int i = 0; i < positions.size(); i++) {
            int position = positions.get(i);
            char c = transformed.charAt(i);
            if (position >= 0) {
                result.setCharAt(position, c);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private void appendPair(StringBuilder sb, int x, int y, int[] matrix, String alphabet) {
        int n = alphabet.length();
        int first = mod(matrix[0] * x + matrix[1] * y, n);
        int second = mod(matrix[2] * x + matrix[3] * y, n);
        sb.append(alphabet.charAt(first));
        sb.append(alphabet.charAt(second));
    }

    private int[] inverseKey(int[] key, int n) {
        int a = key[0], b = key[1], c = key[2], d = key[3];
        int det = mod(a * d - b * c, n);
        int detInv = modInverse(det, n);
        return new int[]{
                mod(detInv * d, n),
                mod(-detInv * b, n),
                mod(-detInv * c, n),
                mod(detInv * a, n)
        };
    }

    private boolean isInvertible(int[] key, int n) {
        int det = mod(key[0] * key[3] - key[1] * key[2], n);
        return gcd(det, n) == 1;
    }

    private int[] parseKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        String[] parts = key.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Hill key must have 4 numbers");
        }
        int[] result = new int[4];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    private String alphabetFor(String language) {
        return "VIE".equalsIgnoreCase(language)
                ? AlphabetConstants.ALPHABET_VIE
                : AlphabetConstants.ALPHABET_ENG;
    }

    private int modInverse(int a, int m) {
        a = mod(a, m);
        for (int x = 1; x < m; x++) {
            if ((a * x) % m == 1) {
                return x;
            }
        }
        throw new IllegalArgumentException("No modular inverse");
    }

    private int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int temp = a % b;
            a = b;
            b = temp;
        }
        return a;
    }

    private int mod(int value, int n) {
        return ((value % n) + n) % n;
    }
}
