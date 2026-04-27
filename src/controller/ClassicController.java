package controller;


import model.classic.ClassicAlgorithm;
import model.classic.ClassicAlgorithmRegistry;
import view.MainFrame;
import view.panel.ClassicPanel;

public class ClassicController extends CategoryController {
    private final ClassicPanel classicPanel;
    private final ClassicAlgorithmRegistry registry;

    public ClassicController(MainFrame frame, ClassicPanel panel, ClassicAlgorithmRegistry registry) {
        super(frame, panel);
        this.classicPanel = panel;
        this.registry = registry;
        initialize();
    }

    @Override
    protected void onPrimary() {
        String input = classicPanel.getInputArea().getText();
        if (!requireInput(input, "Vui lòng nhập plaintext")) {
            return;
        }
        ClassicAlgorithm algorithm = currentAlgorithm();
        if (!validateKey(algorithm)) {
            return;
        }
        String key = currentKey();
        runWorker(() -> "Algorithm=" + algorithm.displayName() + "\nKey=" + key + "\n\n" + algorithm.encrypt(input, key));
    }

    @Override
    protected void onSecondary() {
        String input = classicPanel.getInputArea().getText();
        if (!requireInput(input, "Vui lòng nhập ciphertext")) {
            return;
        }
        ClassicAlgorithm algorithm = currentAlgorithm();
        if (!validateKey(algorithm)) {
            return;
        }
        String key = currentKey();
        runWorker(() -> "Algorithm=" + algorithm.displayName() + "\nKey=" + key + "\n\n" + algorithm.decrypt(input, key));
    }

    @Override
    protected void onGenerate() {
        ClassicAlgorithm algorithm = currentAlgorithm();
        classicPanel.caesarKey.setText(algorithm.generateKey());
    }

    @Override
    protected void updateButtonLabels(String key) {
        setMainButtonLabels("Encrypt", "Decrypt", "Generate key");
    }

    private ClassicAlgorithm currentAlgorithm() {
        ClassicAlgorithm algorithm = registry.find(selected.getKey());
        if (algorithm == null) {
            throw new IllegalStateException("Chưa đăng ký thuật toán: " + selected.getKey());
        }
        return algorithm;
    }

    private String currentKey() {
        return classicPanel.caesarKey.getText().trim();
    }

    private boolean validateKey(ClassicAlgorithm algorithm) {
        String key = currentKey();
        if (algorithm.isValidKey(key)) {
            return true;
        }
        frame.showMessage("Khóa không hợp lệ", algorithm.keyHint());
        return false;
    }
}
