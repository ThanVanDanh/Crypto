package controller;

import model.AlgorithmItem;
import model.classic.AffineAlgorithm;
import model.classic.CaesarAlgorithm;
import model.classic.ClassicAlgorithm;
import model.classic.HillAlgorithm;
import model.classic.SubstitutionAlgorithm;
import model.classic.VigenereAlgorithm;
import view.MainFrame;
import view.panel.ClassicPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class ClassicController {
    private final MainFrame frame;
    private final ClassicPanel panel;
    private final Map<String, ClassicAlgorithm> algorithms = new LinkedHashMap<>();
    private final Map<String, Function<String, String>> keyGenerators = new LinkedHashMap<>();
    private final Map<String, BiPredicate<String, String>> keyValidators = new LinkedHashMap<>();
    private final Map<String, Function<String, String>> keyHints = new LinkedHashMap<>();
    private AlgorithmItem selected;

    public ClassicController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        CaesarAlgorithm caesar = new CaesarAlgorithm();
        AffineAlgorithm affine = new AffineAlgorithm();
        HillAlgorithm hill = new HillAlgorithm();
        SubstitutionAlgorithm substitution = new SubstitutionAlgorithm();
        VigenereAlgorithm vigenere = new VigenereAlgorithm();
        addClassicAlgorithm(items, "caesar", "Caesar Cipher", caesar, caesar::genKey, caesar::isValidKey, caesar::keyHint);
        addClassicAlgorithm(items, "affine", "Affine Cipher", affine, affine::genKey, affine::isValidKey, affine::keyHint);
        addClassicAlgorithm(items, "hill", "Hill Cipher", hill, hill::genKey, hill::isValidKey, hill::keyHint);
        addClassicAlgorithm(items, "substitution", "Substitution Cipher", substitution, substitution::genKey, substitution::isValidKey, substitution::keyHint);
        addClassicAlgorithm(items, "vigenere", "Vigenere Cipher", vigenere, vigenere::genKey, vigenere::isValidKey, vigenere::keyHint);
        panel = new ClassicPanel(items);
        bind();
    }

    public ClassicPanel getPanel() {
        return panel;
    }

    public void triggerPrimary() {
        onPrimary();
    }

    public void triggerSecondary() {
        onSecondary();
    }

    public void triggerGenerate() {
        onGenerate();
    }

    public void triggerClear() {
        onClear();
    }

    private void bind() {
        panel.getPrimaryButton().addActionListener(e -> onPrimary());
        panel.getSecondaryButton().addActionListener(e -> onSecondary());
        panel.getGenerateButton().addActionListener(e -> onGenerate());
        panel.getClearButton().addActionListener(e -> onClear());
        panel.getAlgorithmList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                AlgorithmItem item = panel.getAlgorithmList().getSelectedValue();
                if (item != null) {
                    selectAlgorithm(item);
                }
            }
        });
        if (panel.getAlgorithmList().getModel().getSize() > 0) {
            panel.getAlgorithmList().setSelectedIndex(0);
            selectAlgorithm(panel.getAlgorithmList().getSelectedValue());
        }
    }

    private void onPrimary() {
        String input = panel.getInputArea().getText();
        if (!requireInput(input, "Vui lòng nhập plaintext")) {
            return;
        }
        ClassicAlgorithm algorithm = currentAlgorithm();
        if (!validateKey()) {
            return;
        }
        String key = panel.getKeyFor(selected.getKey());
        runWorker(() -> encryptByLanguage(algorithm, input, key));
    }

    private void onSecondary() {
        String input = panel.getInputArea().getText();
        if (!requireInput(input, "Vui lòng nhập ciphertext")) {
            return;
        }
        ClassicAlgorithm algorithm = currentAlgorithm();
        if (!validateKey()) {
            return;
        }
        String key = panel.getKeyFor(selected.getKey());
        runWorker(() -> decryptByLanguage(algorithm, input, key));
    }

    private void onGenerate() {
        Function<String, String> generator = keyGenerators.get(selected.getKey());
        if (generator != null) {
            panel.setKeyFor(selected.getKey(), generator.apply(panel.getSelectedLanguage()));
            return;
        }
        frame.showMessage("Không hỗ trợ", "Thuật toán này không hỗ trợ sinh key.");
    }

    private void onClear() {
        panel.getInputArea().setText("");
        panel.getOutputArea().setText("");
    }

    private void selectAlgorithm(AlgorithmItem item) {
        selected = item;
        CardLayout cl = (CardLayout) panel.getOptionCards().getLayout();
        cl.show(panel.getOptionCards(), item.getKey());
        updateButtonLabels(item.getKey());
    }

    private void updateButtonLabels(String key) {
        panel.getPrimaryButton().setText("Encrypt");
        panel.getSecondaryButton().setText("Decrypt");
        panel.getGenerateButton().setText("Generate key");
        panel.getGenerateButton().setEnabled(keyGenerators.get(key) != null);
        panel.getSecondaryButton().setEnabled(true);
    }

    private ClassicAlgorithm currentAlgorithm() {
        ClassicAlgorithm algorithm = algorithms.get(selected.getKey());
        if (algorithm == null) {
            throw new IllegalStateException("Chưa đăng ký thuật toán: " + selected.getKey());
        }
        return algorithm;
    }

    private boolean validateKey() {
        BiPredicate<String, String> validator = keyValidators.get(selected.getKey());
        if (validator != null) {
            String key = panel.getKeyFor(selected.getKey());
            String language = panel.getSelectedLanguage();
            if (validator.test(key, language)) {
                return true;
            }
            Function<String, String> hintProvider = keyHints.get(selected.getKey());
            String hint = hintProvider == null ? "Khóa không hợp lệ" : hintProvider.apply(language);
            frame.showMessage("Khóa không hợp lệ", hint);
            return false;
        }
        return true;
    }

    private String encryptByLanguage(ClassicAlgorithm algorithm, String input, String key) {
        if ("VIE".equalsIgnoreCase(panel.getSelectedLanguage())) {
            return algorithm.encryptVIE(input, key);
        }
        return algorithm.encryptENG(input, key);
    }

    private String decryptByLanguage(ClassicAlgorithm algorithm, String input, String key) {
        if ("VIE".equalsIgnoreCase(panel.getSelectedLanguage())) {
            return algorithm.decryptVIE(input, key);
        }
        return algorithm.decryptENG(input, key);
    }

    private boolean requireInput(String input, String message) {
        if (!input.isBlank()) {
            return true;
        }
        frame.showMessage("Thiếu dữ liệu", message);
        return false;
    }

    private void runWorker(Callable<String> worker) {
        SwingWorker<String, Void> swingWorker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return worker.call();
            }

            @Override
            protected void done() {
                try {
                    panel.getOutputArea().setText(get());
                } catch (Exception ex) {
                    panel.getOutputArea().setText("");
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        swingWorker.execute();
    }

    private void addClassicAlgorithm(List<AlgorithmItem> items,
                                     String key,
                                     String displayName,
                                     ClassicAlgorithm algorithm,
                                     Function<String, String> keyGenerator,
                                     BiPredicate<String, String> keyValidator,
                                     Function<String, String> keyHint) {
        algorithms.put(key, algorithm);
        items.add(new AlgorithmItem(key, displayName));
        if (keyGenerator != null) {
            keyGenerators.put(key, keyGenerator);
        }
        if (keyValidator != null) {
            keyValidators.put(key, keyValidator);
        }
        if (keyHint != null) {
            keyHints.put(key, keyHint);
        }
    }
}

