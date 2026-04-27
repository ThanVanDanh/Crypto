package model.classic;

public interface ClassicAlgorithm {
    String key();

    String displayName();

    String encrypt(String plaintext, String key);

    String decrypt(String ciphertext, String key);

    String generateKey();

    boolean isValidKey(String key);

    String keyHint();
}

