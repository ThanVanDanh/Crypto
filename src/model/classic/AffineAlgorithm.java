package model.classic;

import common.AlphabetConstants;

import java.security.SecureRandom;

public class AffineAlgorithm implements ClassicAlgorithm {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String genKey(boolean isVN) {
        int n = alphabetFor(isVN).length();
        int a;
        do {
            a = RANDOM.nextInt(n - 1) + 1;
        } while (gcd(a, n) != 1);
        int b = RANDOM.nextInt(n);
        return a + " " + b;
    }

    @Override
    public boolean isValidKey(String key, boolean isVN) {
        try {
            int[] ab = parseKey(key);
            int n = alphabetFor(isVN).length();
            int a = ab[0];
            if (a <= 0 || a >= n) {
                return false;
            }
            return gcd(a, n) == 1;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    @Override
    public String encrypt(String text, String key, boolean isVN) {
        return handle(text, parseKey(key), true, alphabetFor(isVN));
    }

    @Override
    public String decrypt(String text, String key, boolean isVN) {
        return handle(text, parseKey(key), false, alphabetFor(isVN));
    }

    private String alphabetFor(boolean isVN) {
        return isVN ? AlphabetConstants.ALPHABET_VIE : AlphabetConstants.ALPHABET_ENG;
    }

    private String handle(String text, int[] ab, boolean encrypt, String alphabet) {
        int m = alphabet.length();
        int a = ab[0], b = ab[1];
        int aInv = modInverse(a, m);

        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            int idx = alphabet.indexOf(c);
            if (idx >= 0) {
                int result;
                if (encrypt) {
                    result = (a * idx + b) % m;
                } else {
                    result = (aInv * (idx - b)) % m;
                    if (result < 0) {
                        result = result + m;
                    }
                }
                sb.append(alphabet.charAt(result));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private int[] parseKey(String key) {
        String[] parts = key.trim().split("[,\\s]+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Key Affine phai co 2 so nguyen");
        }
        return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
    }

    private int modInverse(int a, int m) {
        a = a % m;
        if (a < 0) {
            a = a + m;
        }
        for (int x = 1; x < m; x++) {
            if ((a * x) % m == 1) {
                return x;
            }
        }
        throw new IllegalArgumentException("Khong ton tai nghich dao mod " + m + " cua " + a);
    }

    private int gcd(int a, int b) {
        if (b == 0) {
            return a;
        } else {
            return gcd(b, a % b);
        }
    }
}
