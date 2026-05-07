package controller;

import common.CryptoUtils;
import model.AlgorithmItem;
import model.symmetric.AesCbcAlgorithm;
import model.symmetric.BlowfishAlgorithm;
import model.symmetric.SymmetricAlgorithm;
import model.symmetric.TripleDesAlgorithm;
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
    private final Map<String, SymmetricAlgorithm> algorithms = new LinkedHashMap<>();
    private AlgorithmItem selected;

    public SymmetricController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        register(items, "aes", "AES (CBC)", new AesCbcAlgorithm());
        register(items, "3des", "3DES (CBC)", new TripleDesAlgorithm());
        register(items, "blowfish", "Blowfish (CBC)", new BlowfishAlgorithm());
        panel = new SymmetricPanel(items);
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
        SymmetricAlgorithm algorithm = currentAlgorithm();
        KeyMaterial material = readKeyMaterial(algorithm);
        if (material == null) {
            return;
        }
        runWorker(() -> algorithm.encrypt(input, material.key, material.iv));
    }

    private void onSecondary() {
        String input = panel.getInputArea().getText();
        if (!requireInput(input, "Vui lòng nhập ciphertext (Base64)")) {
            return;
        }
        SymmetricAlgorithm algorithm = currentAlgorithm();
        KeyMaterial material = readKeyMaterial(algorithm);
        if (material == null) {
            return;
        }
        runWorker(() -> algorithm.decrypt(input, material.key, material.iv));
    }

    private void onGenerate() {
        SymmetricAlgorithm algorithm = currentAlgorithm();
        int keySizeBits = panel.getKeySizeBits(selected.getKey());
        if (!isSupportedKeySize(algorithm, keySizeBits)) {
            frame.showMessage("Khóa không hợp lệ", "Kích thước key không được hỗ trợ.");
            return;
        }
        int keySizeBytes = algorithm.keySizeBytes(keySizeBits);
        byte[] key = CryptoUtils.randomBytes(keySizeBytes);
        byte[] iv = CryptoUtils.randomBytes(algorithm.ivSizeBytes());
        panel.setKeyBase64(selected.getKey(), CryptoUtils.toBase64(key));
        panel.setIvBase64(selected.getKey(), CryptoUtils.toBase64(iv));
    }

    private void onClear() {
        panel.getInputArea().setText("");
        panel.getOutputArea().setText("");
    }

    private void selectAlgorithm(AlgorithmItem item) {
        selected = item;
        CardLayout cl = (CardLayout) panel.getOptionCards().getLayout();
        cl.show(panel.getOptionCards(), item.getKey());
        updateButtonLabels();
    }

    private void updateButtonLabels() {
        panel.getPrimaryButton().setText("Encrypt");
        panel.getSecondaryButton().setText("Decrypt");
        panel.getGenerateButton().setText("Generate key");
        panel.getGenerateButton().setEnabled(true);
        panel.getSecondaryButton().setEnabled(true);
    }

    private SymmetricAlgorithm currentAlgorithm() {
        SymmetricAlgorithm algorithm = algorithms.get(selected.getKey());
        if (algorithm == null) {
            throw new IllegalStateException("Chưa đăng ký thuật toán: " + selected.getKey());
        }
        return algorithm;
    }

    private KeyMaterial readKeyMaterial(SymmetricAlgorithm algorithm) {
        String keyBase64 = panel.getKeyBase64(selected.getKey());
        String ivBase64 = panel.getIvBase64(selected.getKey());
        if (keyBase64.isBlank()) {
            frame.showMessage("Thiếu dữ liệu", "Vui lòng nhập key (Base64) hoặc Generate.");
            return null;
        }
        if (ivBase64.isBlank()) {
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

    private boolean isSupportedKeySize(SymmetricAlgorithm algorithm, int keySizeBits) {
        return Arrays.stream(algorithm.supportedKeySizes()).anyMatch(size -> size == keySizeBits);
    }

    private boolean isSupportedKeyLength(SymmetricAlgorithm algorithm, int keyLengthBytes) {
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

    private void register(List<AlgorithmItem> items, String key, String displayName, SymmetricAlgorithm algorithm) {
        algorithms.put(key, algorithm);
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

