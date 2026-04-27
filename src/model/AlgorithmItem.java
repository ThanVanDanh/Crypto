package model;

public class AlgorithmItem {
    private final String key;
    private final String name;
    private final boolean weak;

    public AlgorithmItem(String key, String name, boolean weak) {
        this.key = key;
        this.name = name;
        this.weak = weak;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public boolean isWeak() {
        return weak;
    }

    @Override
    public String toString() {
        return name;
    }
}
