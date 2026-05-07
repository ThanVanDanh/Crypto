package model.classic;

import common.AlphabetConstants;

import java.util.Random;

public class AffineAlgorithm implements ClassicAlgorithm {
    public int[] genKey(int n) {
        Random rand = new Random();
        int a;
        do {
            a = rand.nextInt(n - 1) + 1;
        } while (gcd(a, n) != 1);

        int b = rand.nextInt(n);

        return new int[] { a, b };
    }

    public String genKey(String language) {
        int[] key = genKey(alphabetFor(language).length());
        return key[0] + "," + key[1];
    }

    public boolean isValidKey(String key, String language) {
        try {
            int[] ab = parseKey(key);
            int n = alphabetFor(language).length();
            int a = ab[0];
            if (a <= 0 || a >= n) {
                return false;
            }
            return gcd(a, n) == 1;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public String keyHint(String language) {
        int n = alphabetFor(language).length();
        return "Khóa Affine có dạng a,b với gcd(a," + n + ") = 1 (ví dụ: 5,8).";
    }

    public String encryptENG(String plaintext, String key) {
        return handle(plaintext, parseKey(key), true, AlphabetConstants.ALPHABET_ENG);
    }

    public String encryptVIE(String plaintext, String key) {
        return handle(plaintext, parseKey(key), true, AlphabetConstants.ALPHABET_VIE);
    }

    public String decryptENG(String ciphertext, String key) {
        return handle(ciphertext, parseKey(key), false, AlphabetConstants.ALPHABET_ENG);
    }


    public String decryptVIE(String ciphertext, String key) {
        return handle(ciphertext, parseKey(key), false, AlphabetConstants.ALPHABET_VIE);
    }

    private String alphabetFor(String language) {
        return "VIE".equalsIgnoreCase(language)
                ? AlphabetConstants.ALPHABET_VIE
                : AlphabetConstants.ALPHABET_ENG;
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
        String[] parts = key.split(",");
        String part1 = parts[0].trim();
        String part2 = parts[1].trim();

        int a = Integer.parseInt(part1);
        int b = Integer.parseInt(part2);

        int[] result = new int[2];
        result[0] = a;
        result[1] = b;

        return result;
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
        throw new IllegalArgumentException("Không có nghịch đảo mod " + m + " của " + a);
    }

    private int gcd(int a, int b) {
        if (b == 0) {
            return a;
        } else {
            return gcd(b, a % b);
        }
    }
}
