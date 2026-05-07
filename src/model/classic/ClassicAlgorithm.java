package model.classic;

public interface ClassicAlgorithm {
    String encryptENG(String plaintext, String key);

    String decryptENG(String ciphertext, String key);

    String encryptVIE(String plaintext, String key);

    String decryptVIE(String ciphertext, String key);


}
