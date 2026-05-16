package controller;

import common.ControllerUtils;
import common.CryptoUtils;
import model.AlgorithmItem;
import model.symmetric.JceSymmetricAlgorithm;
import view.MainFrame;
import view.panel.SymmetricPanel;

import javax.swing.*;
import java.awt.*;
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
    private final Map<String, Integer> ivSizesByAlgorithm = new LinkedHashMap<>();
    private AlgorithmItem selected;

    public SymmetricController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        addJceSymmetricAlgorithm(items, "aes", "AES (CBC)", "AES/CBC/PKCS5Padding", "AES",
                new int[]{128, 192, 256}, new int[]{16, 24, 32}, 16, JceSymmetricAlgorithm.PARAM_IV);
        addJceSymmetricAlgorithm(items, "aes-gcm", "AES (GCM)", "AES/GCM/NoPadding", "AES",
                new int[]{128, 192, 256}, new int[]{16, 24, 32}, 12, JceSymmetricAlgorithm.PARAM_GCM);
        addJceSymmetricAlgorithm(items, "des", "DES (CBC)", "DES/CBC/PKCS5Padding", "DES",
                new int[]{56}, new int[]{8}, 8, JceSymmetricAlgorithm.PARAM_IV);
        addJceSymmetricAlgorithm(items, "3des", "3DES (CBC)", "DESede/CBC/PKCS5Padding", "DESede",
                new int[]{168}, new int[]{24}, 8, JceSymmetricAlgorithm.PARAM_IV);
        addJceSymmetricAlgorithm(items, "blowfish", "Blowfish (CBC)", "Blowfish/CBC/PKCS5Padding", "Blowfish",
                new int[]{32, 64, 128, 192, 256, 448}, new int[]{4, 8, 16, 24, 32, 56}, 8, JceSymmetricAlgorithm.PARAM_IV);
        addJceSymmetricAlgorithm(items, "rc2", "RC2 (CBC)", "RC2/CBC/PKCS5Padding", "RC2",
                new int[]{40, 64, 128, 256}, new int[]{5, 8, 16, 32}, 8, JceSymmetricAlgorithm.PARAM_IV);
        addJceSymmetricAlgorithm(items, "arcfour", "ARCFOUR / RC4", "ARCFOUR", "ARCFOUR",
                new int[]{40, 128, 256}, new int[]{5, 16, 32}, 0, JceSymmetricAlgorithm.PARAM_NONE);
        addJceSymmetricAlgorithm(items, "chacha20", "ChaCha20", "ChaCha20", "ChaCha20",
                new int[]{256}, new int[]{32}, 12, JceSymmetricAlgorithm.PARAM_CHACHA20);
        addJceSymmetricAlgorithm(items, "chacha20-poly1305", "ChaCha20-Poly1305", "ChaCha20-Poly1305", "ChaCha20",
                new int[]{256}, new int[]{32}, 12, JceSymmetricAlgorithm.PARAM_IV);
        panel = new SymmetricPanel(items, keySizesByAlgorithm, ivSizesByAlgorithm);
        bind();
    }

    public SymmetricPanel getPanel() {
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
        panel.getCopyKeyButton().addActionListener(e -> onCopyKey());
        panel.getSaveKeyButton().addActionListener(e -> onSaveKey());
        panel.getImportKeyButton().addActionListener(e -> onImportKey());
        panel.getClearButton().addActionListener(e -> onClear());
        panel.getBrowseInputFileButton().addActionListener(e -> chooseInputFile());
        panel.getBrowseOutputFileButton().addActionListener(e -> chooseOutputFile());
        panel.getEncryptFileButton().addActionListener(e -> onEncryptFile());
        panel.getDecryptFileButton().addActionListener(e -> onDecryptFile());
        panel.getSaveInputTextButton().addActionListener(e -> onSaveText("input.txt", panel.getInputArea().getText()));
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
        if (!requireInput(input, "Vui lòng nhập plaintext")) {
            return;
        }
        JceSymmetricAlgorithm algorithm = currentAlgorithm();
        KeyMaterial material = readKeyMaterial(algorithm);
        if (material == null) {
            return;
        }
        algorithm.loadKey(material.key, material.iv);
        runWorker(() -> algorithm.encryptText(input));
    }

    private void onSecondary() {
        String input = panel.getInputArea().getText();
        if (!requireInput(input, "Vui lòng nhập ciphertext (Base64)")) {
            return;
        }
        JceSymmetricAlgorithm algorithm = currentAlgorithm();
        KeyMaterial material = readKeyMaterial(algorithm);
        if (material == null) {
            return;
        }
        algorithm.loadKey(material.key, material.iv);
        runWorker(() -> algorithm.decryptText(input));
    }

    private void onGenerate() {
        JceSymmetricAlgorithm algorithm = currentAlgorithm();
        int keySizeBits = panel.getKeySizeBits();
        if (!isSupportedKeySize(algorithm, keySizeBits)) {
            frame.showMessage("Khóa không hợp lệ", "Kích thước key không được hỗ trợ.");
            return;
        }
        int keySizeBytes = algorithm.keySizeBytes(keySizeBits);
        byte[] key = CryptoUtils.randomBytes(keySizeBytes);
        byte[] iv = algorithm.ivSizeBytes() == 0 ? new byte[0] : CryptoUtils.randomBytes(algorithm.ivSizeBytes());
        panel.setKeyBase64(CryptoUtils.toBase64(key));
        panel.setIvBase64(CryptoUtils.toBase64(iv));
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
        panel.setIvBase64(lines.size() > 1 ? lines.get(1) : "");
    }

    private void onSaveText(String defaultName, String content) {
        ControllerUtils.saveText(panel, frame, defaultName, content, "Khong co noi dung de luu.");
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
        KeyMaterial material = readKeyMaterial(algorithm);
        if (material == null) {
            return;
        }
        algorithm.loadKey(material.key, material.iv);
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
            throw new IllegalStateException("Chưa đăng ký thuật toán: " + selected.getKey());
        }
        return algorithm;
    }

    private KeyMaterial readKeyMaterial(JceSymmetricAlgorithm algorithm) {
        String keyBase64 = panel.getKeyBase64();
        String ivBase64 = panel.getIvBase64();
        if (keyBase64.isBlank()) {
            frame.showMessage("Thiếu dữ liệu", "Vui lòng nhập key (Base64) hoặc Generate.");
            return null;
        }
        if (algorithm.ivSizeBytes() > 0 && ivBase64.isBlank()) {
            frame.showMessage("Thiếu dữ liệu", "Vui lòng nhập IV (Base64) hoặc Generate.");
            return null;
        }
        byte[] key;
        byte[] iv;
        try {
            key = CryptoUtils.fromBase64(keyBase64);
            iv = CryptoUtils.fromBase64(ivBase64);
        } catch (IllegalArgumentException ex) {
            frame.showMessage("Khóa không hợp lệ", "Key/IV phải là Base64 hợp lệ.");
            return null;
        }
        if (!isSupportedKeyLength(algorithm, key.length)) {
            frame.showMessage("Khóa không hợp lệ", "Kích thước key không được hỗ trợ.");
            return null;
        }
        if (iv.length != algorithm.ivSizeBytes()) {
            frame.showMessage("IV không hợp lệ", "IV phải có " + algorithm.ivSizeBytes() + " bytes.");
            return null;
        }
        return new KeyMaterial(key, iv);
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
        frame.showMessage("Thiếu dữ liệu", message);
        return false;
    }

    private String currentKeyText() {
        String key = panel.getKeyBase64();
        String iv = panel.getIvBase64();
        if (key.isBlank() && iv.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(key);
        if (!iv.isBlank()) {
            sb.append(System.lineSeparator()).append(iv);
        }
        return sb.toString();
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

    private void addJceSymmetricAlgorithm(List<AlgorithmItem> items,
                                          String key,
                                          String displayName,
                                          String transformation,
                                          String keyAlgorithm,
                                          int[] keySizes,
                                          int[] keyBytes,
                                          int ivSizeBytes,
                                          String parameterType) {
        if (!JceSymmetricAlgorithm.isAvailable(transformation)) {
            return;
        }
        addSymmetricAlgorithm(items, key, displayName,
                new JceSymmetricAlgorithm(transformation, keyAlgorithm, keySizes, keyBytes, ivSizeBytes, parameterType));
    }

    private void addSymmetricAlgorithm(List<AlgorithmItem> items, String key, String displayName, JceSymmetricAlgorithm algorithm) {
        algorithms.put(key, algorithm);
        keySizesByAlgorithm.put(key, algorithm.supportedKeySizes());
        ivSizesByAlgorithm.put(key, algorithm.ivSizeBytes());
        items.add(new AlgorithmItem(key, displayName));
    }

    private static final class KeyMaterial {
        private final byte[] key;
        private final byte[] iv;

        private KeyMaterial(byte[] key, byte[] iv) {
            this.key = key;
            this.iv = iv;
        }
    }
}
