package model.classic;

import common.AlphabetConstants;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class HillAlgorithm implements ClassicAlgorithm {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String genKey(boolean isVN) {
        int n = alphabetFor(isVN).length();
        int[] key;
        do {
            key = new int[]{
                    RANDOM.nextInt(n),
                    RANDOM.nextInt(n),
                    RANDOM.nextInt(n),
                    RANDOM.nextInt(n)
            };
        } while (!isInvertible(key, n));
        return key[0] + " " + key[1] + " " + key[2] + " " + key[3];
    }

    @Override
    public String encrypt(String text, String key, boolean isVN) {
        return handle(text, parseKey(key), true, alphabetFor(isVN));
    }

    @Override
    public String decrypt(String text, String key, boolean isVN) {
        return handle(text, parseKey(key), false, alphabetFor(isVN));
    }

    @Override
    public boolean isValidKey(String key, boolean isVN) {
        try {
            return isInvertible(parseKey(key), alphabetFor(isVN).length());
        } catch (RuntimeException ex) {
            return false;
        }
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
            if (!encrypt) {
                throw new IllegalArgumentException("Ciphertext Hill phai co so ky tu hop le la so chan.");
            }
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
            throw new IllegalArgumentException("Key khong duoc de trong");
        }
        String[] parts = key.trim().split("[,\\s]+");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Key phai co 4 so nguyen");
        }
        int[] result = new int[4];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    private String alphabetFor(boolean isVN) {
        return isVN ? AlphabetConstants.ALPHABET_VIE : AlphabetConstants.ALPHABET_ENG;
    }

    private int modInverse(int a, int m) {
        a = mod(a, m);
        for (int x = 1; x < m; x++) {
            if ((a * x) % m == 1) {
                return x;
            }
        }
        throw new IllegalArgumentException("Khong ton tai nghich dao modular");
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
