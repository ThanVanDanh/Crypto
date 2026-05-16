package model.classic;

public interface ClassicAlgorithm {

    String encrypt(String text, String key, boolean isVN);

    String decrypt(String text, String key, boolean isVN);

    String genKey(boolean isVN);

    boolean isValidKey(String key, boolean isVN);
}
