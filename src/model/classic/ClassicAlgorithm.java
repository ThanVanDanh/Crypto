package model.classic;

public interface ClassicAlgorithm {
    String genKey(boolean isVN);

    String encrypt(String text, String key, boolean isVN);

    String decrypt(String text, String key, boolean isVN);

    boolean isValidKey(String key, boolean isVN);
}
