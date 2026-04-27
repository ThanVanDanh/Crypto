package model.classic;


import model.AlgorithmItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClassicAlgorithmRegistry {
    private final Map<String, ClassicAlgorithm> algorithms = new LinkedHashMap<>();

    public ClassicAlgorithmRegistry() {
    }

    public static ClassicAlgorithmRegistry caesarOnly() {
        ClassicAlgorithmRegistry registry = new ClassicAlgorithmRegistry();
        registry.register(new CaesarAlgorithm());
        return registry;
    }

    public void register(ClassicAlgorithm algorithm) {
        algorithms.put(algorithm.key(), algorithm);
    }

    public ClassicAlgorithm find(String key) {
        return algorithms.get(key);
    }

    public List<AlgorithmItem> toItems() {
        List<AlgorithmItem> items = new ArrayList<>();
        for (ClassicAlgorithm algorithm : algorithms.values()) {
            items.add(new AlgorithmItem(algorithm.key(), algorithm.displayName(), false));
        }
        return items;
    }
}

