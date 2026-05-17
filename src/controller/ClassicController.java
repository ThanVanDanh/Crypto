package controller;

import common.ControllerUtils;
import model.AlgorithmItem;
import model.classic.AffineAlgorithm;
import model.classic.CaesarAlgorithm;
import model.classic.ClassicAlgorithm;
import model.classic.HillAlgorithm;
import model.classic.SubstitutionAlgorithm;
import model.classic.VigenereAlgorithm;
import view.MainFrame;
import view.panel.ClassicPanel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClassicController {
    private final MainFrame frame;
    private final ClassicPanel panel;
    private final Map<String, ClassicAlgorithm> algorithms = new LinkedHashMap<>();
    private AlgorithmItem selected;

    public ClassicController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        addClassicAlgorithm(items, "caesar", "Caesar Cipher", new CaesarAlgorithm());
        addClassicAlgorithm(items, "affine", "Affine Cipher", new AffineAlgorithm());
        addClassicAlgorithm(items, "hill", "Hill Cipher", new HillAlgorithm());
        addClassicAlgorithm(items, "substitution", "Substitution Cipher", new SubstitutionAlgorithm());
        addClassicAlgorithm(items, "vigenere", "Vigenere Cipher", new VigenereAlgorithm());
        panel = new ClassicPanel(items);
        bind();
    }

    public ClassicPanel getPanel() {
        return panel;
    }

    private void bind() {
        panel.getPrimaryButton().addActionListener(e -> onPrimary());
        panel.getSecondaryButton().addActionListener(e -> onSecondary());
        panel.getGenerateButton().addActionListener(e -> onGenerate());
        panel.getCopyKeyButton().addActionListener(e -> onCopyKey());
        panel.getSaveKeyButton().addActionListener(e -> onSaveKey());
        panel.getImportKeyButton().addActionListener(e -> onImportKey());
        panel.getClearButton().addActionListener(e -> onClear());
        panel.getSaveInputTextButton().addActionListener(e -> ControllerUtils.importTextTo(panel, frame, panel.getInputArea()));
        panel.getSaveOutputTextButton().addActionListener(e ->
                ControllerUtils.saveText(panel, frame, "classic-output.txt", panel.getOutputArea().getText(), "Khong co noi dung de luu."));
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
        if (!requireInput(input, "Vui long nhap plaintext.")) {
            return;
        }
        ClassicAlgorithm algorithm = currentAlgorithm();
        if (!validateKey()) {
            return;
        }
        String key = panel.getKey();
        ControllerUtils.runWorker(panel, panel.getOutputArea(), () -> algorithm.encrypt(input, key, isVietnamese()));
    }

    private void onSecondary() {
        String input = panel.getInputArea().getText();
        if (!requireInput(input, "Vui long nhap ciphertext.")) {
            return;
        }
        ClassicAlgorithm algorithm = currentAlgorithm();
        if (!validateKey()) {
            return;
        }
        String key = panel.getKey();
        ControllerUtils.runWorker(panel, panel.getOutputArea(), () -> algorithm.decrypt(input, key, isVietnamese()));
    }

    private void onGenerate() {
        panel.setKey(currentAlgorithm().genKey(isVietnamese()));
    }

    private void onCopyKey() {
        ControllerUtils.copyText(frame, panel.getKey(), "Vui long nhap hoac tao key truoc.");
    }

    private void onSaveKey() {
        ControllerUtils.saveText(panel, frame, selected.getKey() + "-key.txt",
                panel.getKey(), "Vui long nhap hoac tao key truoc.");
    }

    private void onImportKey() {
        String content = ControllerUtils.openText(panel, frame);
        if (content == null) {
            return;
        }
        if (content.isBlank()) {
            frame.showMessage("Thieu du lieu", "File key dang rong.");
            return;
        }
        panel.setKey(content.trim());
    }

    private void onClear() {
        panel.getInputArea().setText("");
        panel.getOutputArea().setText("");
    }

    private void selectAlgorithm(AlgorithmItem item) {
        selected = item;
        panel.showOptions(item.getKey());
        panel.getPrimaryButton().setText("Encrypt");
        panel.getSecondaryButton().setText("Decrypt");
        panel.getGenerateButton().setText("Generate key");
        panel.getGenerateButton().setEnabled(true);
        panel.getCopyKeyButton().setEnabled(true);
        panel.getSaveKeyButton().setEnabled(true);
        panel.getImportKeyButton().setEnabled(true);
        panel.getSecondaryButton().setEnabled(true);
    }

    private ClassicAlgorithm currentAlgorithm() {
        ClassicAlgorithm algorithm = algorithms.get(selected.getKey());
        if (algorithm == null) {
            throw new IllegalStateException("Chua dang ky thuat toan: " + selected.getKey());
        }
        return algorithm;
    }

    private boolean validateKey() {
        ClassicAlgorithm algorithm = currentAlgorithm();
        String key = panel.getKey();
        if (algorithm.isValidKey(key, isVietnamese())) {
            return true;
        }
        frame.showMessage("Khoa khong hop le", "Vui long kiem tra key cua thuat toan dang chon.");
        return false;
    }

    private boolean isVietnamese() {
        return "VIE".equalsIgnoreCase(panel.getSelectedLanguage());
    }

    private boolean requireInput(String input, String message) {
        if (!input.isBlank()) {
            return true;
        }
        frame.showMessage("Thieu du lieu", message);
        return false;
    }

    private void addClassicAlgorithm(List<AlgorithmItem> items,
                                     String key,
                                     String displayName,
                                     ClassicAlgorithm algorithm) {
        algorithms.put(key, algorithm);
        items.add(new AlgorithmItem(key, displayName));
    }
}
