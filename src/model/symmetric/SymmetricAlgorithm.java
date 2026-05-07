package model.symmetric;

public interface SymmetricAlgorithm {
    String encrypt(String plaintext, byte[] key, byte[] iv) throws Exception;

    String decrypt(String ciphertextBase64, byte[] key, byte[] iv) throws Exception;

    int[] supportedKeySizes();

    int ivSizeBytes();

    int keySizeBytes(int keySizeBits);
}
