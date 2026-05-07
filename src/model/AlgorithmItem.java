package model;

public class AlgorithmItem {
    private final String key;
    private final String name;

    /**
     * Tạo item hiển thị cho một thuật toán.
     */
    public AlgorithmItem(String key, String name) {
        this.key = key;
        this.name = name;
    }

    /**
     * Trả về key định danh nội bộ của thuật toán.
     */
    public String getKey() {
        return key;
    }

    /**
     * Trả về tên hiển thị của thuật toán.
     */
    public String getName() {
        return name;
    }



    @Override
    public String toString() {
        return name;
    }
}