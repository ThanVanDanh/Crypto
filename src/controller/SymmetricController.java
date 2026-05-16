package controller;

import common.ControllerUtils;
import common.CryptoUtils;
import model.AlgorithmItem;
import model.symmetric.JceSymmetricAlgorithm;
import view.MainFrame;
import view.panel.SymmetricPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class SymmetricController {
    private final MainFrame frame;
    private final SymmetricPanel panel;
    private final Map<String, JceSymmetricAlgorithm> algorithms = new LinkedHashMap<>();
    private final Map<String, int[]> keySizesByAlgorithm = new LinkedHashMap<>();
    private AlgorithmItem selected;

    public SymmetricController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        addJceSymmetricAlgorithm(items, "aes", "AES", "AES",
                new int[]{128, 192, 256}, new int[]{16, 24, 32});
        addJceSymmetricAlgorithm(items, "aria", "ARIA", "ARIA",
                new int[]{128, 192, 256}, new int[]{16, 24, 32});
        addJceSymmetricAlgorithm(items, "camellia", "Camellia", "Camellia",
                new int[]{128, 192, 256}, new int[]{16, 24, 32});
        addJceSymmetricAlgorithm(items, "cast5", "CAST5", "CAST5",
                new int[]{40, 80, 128}, new int[]{5, 10, 16});
        addJceSymmetricAlgorithm(items, "cast6", "CAST6", "CAST6",
                new int[]{128, 192, 256}, new int[]{16, 24, 32});
        addJceSymmetricAlgorithm(items, "serpent", "Serpent", "Serpent",
                new int[]{128, 192, 256}, new int[]{16, 24, 32});
        addJceSymmetricAlgorithm(items, "twofish", "Twofish", "Twofish",
                new int[]{128, 192, 256}, new int[]{16, 24, 32});
        addJceSymmetricAlgorithm(items, "blowfish", "Blowfish", "Blowfish",
                new int[]{128, 256, 448}, new int[]{16, 32, 56});
        addJceSymmetricAlgorithm(items, "des", "DES", "DES",
                new int[]{56}, new int[]{8});
        addJceSymmetricAlgorithm(items, "3des", "DESede", "DESede",
                new int[]{112, 168}, new int[]{16, 24});
        panel = new SymmetricPanel(items, keySizesByAlgorithm);
        bind();
    }

    public SymmetricPanel getPanel() {
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
        panel.getBrowseInputFileButton().addActionListener(e -> chooseInputFile());
        panel.getBrowseOutputFileButton().addActionListener(e -> chooseOutputFile());
        panel.getEncryptFileButton().addActionListener(e -> onEncryptFile());
        panel.getDecryptFileButton().addActionListener(e -> onDecryptFile());
        panel.getSaveInputTextButton().addActionListener(e -> onImportInput());
        panel.getSaveOutputTextButton().addActionListener(e -> onSaveText("output.txt", panel.getOutputArea().getText()));
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
        JceSymmetricAlgorithm algorithm = currentAlgorithm();
        if (!isTransformationSupported(algorithm)) {
            return;
        }
        KeyMaterial material = readKeyMaterial(algorithm);
        if (material == null) {
            return;
        }
        algorithm.updateConfig(panel.getMode(), panel.getPadding());
        algorithm.loadKey(material.key);
        runWorker(() -> algorithm.encryptText(input));
    }

    private void onSecondary() {
        String input = panel.getInputArea().getText();
        if (!requireInput(input, "Vui long nhap ciphertext (Base64).")) {
            return;
        }
        JceSymmetricAlgorithm algorithm = currentAlgorithm();
        if (!isTransformationSupported(algorithm)) {
            return;
        }
        KeyMaterial material = readKeyMaterial(algorithm);
        if (material == null) {
            return;
        }
        algorithm.updateConfig(panel.getMode(), panel.getPadding());
        algorithm.loadKey(material.key);
        runWorker(() -> algorithm.decryptText(input));
    }

    private void onGenerate() {
        JceSymmetricAlgorithm algorithm = currentAlgorithm();
        int keySizeBits = panel.getKeySizeBits();
        if (!isSupportedKeySize(algorithm, keySizeBits)) {
            frame.showMessage("Khoa khong hop le", "Kich thuoc key khong duoc ho tro.");
            return;
        }
        int keySizeBytes = algorithm.keySizeBytes(keySizeBits);
        byte[] key = CryptoUtils.randomBytes(keySizeBytes);
        panel.setKeyBase64(CryptoUtils.toBase64(key));
    }

    private void onCopyKey() {
        ControllerUtils.copyText(frame, currentKeyText(), "Vui long tao hoac nhap key truoc.");
    }

    private void onSaveKey() {
        ControllerUtils.saveText(panel, frame, selected.getKey() + "-key.txt",
                currentKeyText(), "Vui long tao hoac nhap key truoc.");
    }

    private void onImportKey() {
        String content = ControllerUtils.openText(panel, frame);
        if (content == null) {
            return;
        }
        List<String> lines = ControllerUtils.textLines(content);
        if (lines.isEmpty()) {
            frame.showMessage("Thieu du lieu", "File key dang rong.");
            return;
        }
        panel.setKeyBase64(lines.get(0));
    }

    private void onSaveText(String defaultName, String content) {
        ControllerUtils.saveText(panel, frame, defaultName, content, "Khong co noi dung de luu.");
    }

    private void onImportInput() {
        String content = ControllerUtils.openText(panel, frame);
        if (content != null) {
            panel.getInputArea().setText(content);
        }
    }

    private void onClear() {
        panel.getInputArea().setText("");
        panel.getOutputArea().setText("");
        panel.getInputFileField().setText("");
        panel.getOutputFileField().setText("");
    }

    private void chooseInputFile() {
        String path = ControllerUtils.chooseOpenFile(panel);
        if (path != null) {
            panel.getInputFileField().setText(path);
        }
    }

    private void chooseOutputFile() {
        String path = ControllerUtils.chooseSaveFile(panel, null);
        if (path != null) {
            panel.getOutputFileField().setText(path);
        }
    }

    private void onEncryptFile() {
        runFileCipher(true);
    }

    private void onDecryptFile() {
        runFileCipher(false);
    }

    private void runFileCipher(boolean encrypt) {
        String inputPath = panel.getInputFileField().getText();
        String outputPath = panel.getOutputFileField().getText();
        if (!ControllerUtils.requireFilePaths(frame, inputPath, outputPath)) {
            return;
        }
        JceSymmetricAlgorithm algorithm = currentAlgorithm();
        if (!isTransformationSupported(algorithm)) {
            return;
        }
        KeyMaterial material = readKeyMaterial(algorithm);
        if (material == null) {
            return;
        }
        algorithm.updateConfig(panel.getMode(), panel.getPadding());
        algorithm.loadKey(material.key);
        runWorker(() -> {
            if (encrypt) {
                algorithm.encryptFile(inputPath, outputPath);
            } else {
                algorithm.decryptFile(inputPath, outputPath);
            }
            return outputPath;
        });
    }

    private void selectAlgorithm(AlgorithmItem item) {
        selected = item;
        panel.showOptions(item.getKey());
        updateButtonLabels();
    }

    private void updateButtonLabels() {
        panel.getPrimaryButton().setText("Encrypt");
        panel.getSecondaryButton().setText("Decrypt");
        panel.getGenerateButton().setText("Generate key");
        panel.getCopyKeyButton().setEnabled(true);
        panel.getSaveKeyButton().setEnabled(true);
        panel.getImportKeyButton().setEnabled(true);
        panel.getGenerateButton().setEnabled(true);
        panel.getSecondaryButton().setEnabled(true);
    }

    private JceSymmetricAlgorithm currentAlgorithm() {
        JceSymmetricAlgorithm algorithm = algorithms.get(selected.getKey());
        if (algorithm == null) {
            throw new IllegalStateException("Chua dang ky thuat toan: " + selected.getKey());
        }
        return algorithm;
    }

    private boolean isTransformationSupported(JceSymmetricAlgorithm algorithm) {
        String mode = panel.getMode();
        String padding = panel.getPadding();
        if (algorithm.isTransformationSupported(mode, padding)) {
            return true;
        }
        frame.showMessage("Khong ho tro", "Mode/padding khong duoc ho tro voi thuat toan da chon.");
        return false;
    }

    private KeyMaterial readKeyMaterial(JceSymmetricAlgorithm algorithm) {
        String keyBase64 = panel.getKeyBase64();
        if (keyBase64.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long nhap key (Base64) hoac Generate.");
            return null;
        }
        byte[] key;
        try {
            key = CryptoUtils.fromBase64(keyBase64);
        } catch (IllegalArgumentException ex) {
            frame.showMessage("Khoa khong hop le", "Key phai la Base64 hop le.");
            return null;
        }
        if (!isSupportedKeyLength(algorithm, key.length)) {
            frame.showMessage("Khoa khong hop le", "Kich thuoc key khong duoc ho tro.");
            return null;
        }
        return new KeyMaterial(key);
    }

    private boolean isSupportedKeySize(JceSymmetricAlgorithm algorithm, int keySizeBits) {
        return Arrays.stream(algorithm.supportedKeySizes()).anyMatch(size -> size == keySizeBits);
    }

    private boolean isSupportedKeyLength(JceSymmetricAlgorithm algorithm, int keyLengthBytes) {
        return Arrays.stream(algorithm.supportedKeySizes())
                .map(algorithm::keySizeBytes)
                .anyMatch(size -> size == keyLengthBytes);
    }

    private boolean requireInput(String input, String message) {
        if (!input.isBlank()) {
            return true;
        }
        frame.showMessage("Thieu du lieu", message);
        return false;
    }

    private String currentKeyText() {
        String key = panel.getKeyBase64();
        return key.isBlank() ? "" : key;
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
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        swingWorker.execute();
    }

    private void addJceSymmetricAlgorithm(List<AlgorithmItem> items,
                                          String key,
                                          String displayName,
                                          String keyAlgorithm,
                                          int[] keySizes,
                                          int[] keyBytes) {
        addSymmetricAlgorithm(items, key, displayName,
                new JceSymmetricAlgorithm(keyAlgorithm, keySizes, keyBytes));
    }

    private void addSymmetricAlgorithm(List<AlgorithmItem> items, String key, String displayName, JceSymmetricAlgorithm algorithm) {
        algorithms.put(key, algorithm);
        keySizesByAlgorithm.put(key, algorithm.supportedKeySizes());
        items.add(new AlgorithmItem(key, displayName));
    }

    private static final class KeyMaterial {
        private final byte[] key;

        private KeyMaterial(byte[] key) {
            this.key = key;
        }
    }
}
