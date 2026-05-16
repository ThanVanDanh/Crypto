package controller;

import common.ControllerUtils;
import common.CryptoUtils;
import model.AlgorithmItem;
import model.asymmetric.RsaAlgorithm;
import view.MainFrame;
import view.panel.AsymmetricPanel;

import javax.swing.*;
import java.awt.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class AsymmetricController {
    private final MainFrame frame;
    private final AsymmetricPanel panel;
    private final Map<String, RsaAlgorithm> algorithms = new LinkedHashMap<>();
    private AlgorithmItem selected;

    public AsymmetricController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        addAsymmetricAlgorithm(items, "rsa", "RSA", new RsaAlgorithm());
        panel = new AsymmetricPanel(items);
        bind();
    }

    public AsymmetricPanel getPanel() {
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
        RsaAlgorithm algorithm = currentAlgorithm();
        PublicKey publicKey = readPublicKey();
        if (publicKey == null) {
            return;
        }
        algorithm.setPublicKey(publicKey);
        runWorker(() -> algorithm.encryptBase64(input));
    }

    private void onSecondary() {
        String input = panel.getInputArea().getText();
        if (!requireInput(input, "Vui long nhap ciphertext (Base64).")) {
            return;
        }
        RsaAlgorithm algorithm = currentAlgorithm();
        PrivateKey privateKey = readPrivateKey();
        if (privateKey == null) {
            return;
        }
        algorithm.setPrivateKey(privateKey);
        runWorker(() -> algorithm.decrypt(input));
    }

    private void onGenerate() {
        RsaAlgorithm algorithm = currentAlgorithm();
        int keySizeBits = panel.getKeySizeBits();
        if (!isSupportedKeySize(algorithm, keySizeBits)) {
            frame.showMessage("Khoa khong hop le", "Kich thuoc key khong duoc ho tro.");
            return;
        }
        try {
            algorithm.genKeyPair(keySizeBits);
            panel.setPublicKeyBase64(algorithm.getPublicKeyBase64());
            panel.setPrivateKeyBase64(algorithm.getPrivateKeyBase64());
        } catch (Exception ex) {
            frame.showMessage("Loi", ex.getMessage());
        }
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
        if (lines.size() > 1) {
            panel.setPublicKeyBase64(lines.get(0));
            panel.setPrivateKeyBase64(lines.get(1));
            return;
        }

        String key = lines.get(0);
        if (isPublicKeyText(key)) {
            panel.setPublicKeyBase64(key);
            panel.setPrivateKeyBase64("");
        } else if (isPrivateKeyText(key)) {
            panel.setPublicKeyBase64("");
            panel.setPrivateKeyBase64(key);
        } else {
            frame.showMessage("Khoa khong hop le", "File key khong dung dinh dang RSA.");
        }
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
        String inputPath = panel.getInputFileField().getText();
        String outputPath = panel.getOutputFileField().getText();
        if (!ControllerUtils.requireFilePaths(frame, inputPath, outputPath)) {
            return;
        }
        RsaAlgorithm algorithm = currentAlgorithm();
        PublicKey publicKey = readPublicKey();
        if (publicKey == null) {
            return;
        }
        algorithm.setPublicKey(publicKey);
        runWorker(() -> {
            algorithm.encryptFile(inputPath, outputPath);
            return outputPath;
        });
    }

    private void onDecryptFile() {
        String inputPath = panel.getInputFileField().getText();
        String outputPath = panel.getOutputFileField().getText();
        if (!ControllerUtils.requireFilePaths(frame, inputPath, outputPath)) {
            return;
        }
        RsaAlgorithm algorithm = currentAlgorithm();
        PrivateKey privateKey = readPrivateKey();
        if (privateKey == null) {
            return;
        }
        algorithm.setPrivateKey(privateKey);
        runWorker(() -> {
            algorithm.decryptFile(inputPath, outputPath);
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

    private RsaAlgorithm currentAlgorithm() {
        RsaAlgorithm algorithm = algorithms.get(selected.getKey());
        if (algorithm == null) {
            throw new IllegalStateException("Chua dang ky thuat toan: " + selected.getKey());
        }
        return algorithm;
    }

    private PublicKey readPublicKey() {
        String publicKeyBase64 = panel.getPublicKeyBase64();
        if (publicKeyBase64.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long nhap public key (Base64) hoac Generate.");
            return null;
        }
        try {
            byte[] decoded = CryptoUtils.fromBase64(publicKeyBase64);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception ex) {
            frame.showMessage("Khoa khong hop le", "Public key khong hop le.");
            return null;
        }
    }

    private PrivateKey readPrivateKey() {
        String privateKeyBase64 = panel.getPrivateKeyBase64();
        if (privateKeyBase64.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long nhap private key (Base64) hoac Generate.");
            return null;
        }
        try {
            byte[] decoded = CryptoUtils.fromBase64(privateKeyBase64);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception ex) {
            frame.showMessage("Khoa khong hop le", "Private key khong hop le.");
            return null;
        }
    }

    private boolean isSupportedKeySize(RsaAlgorithm algorithm, int keySizeBits) {
        return Arrays.stream(algorithm.supportedKeySizes()).anyMatch(size -> size == keySizeBits);
    }

    private boolean requireInput(String input, String message) {
        if (!input.isBlank()) {
            return true;
        }
        frame.showMessage("Thieu du lieu", message);
        return false;
    }

    private String currentKeyText() {
        String publicKey = panel.getPublicKeyBase64();
        String privateKey = panel.getPrivateKeyBase64();
        if (publicKey.isBlank() && privateKey.isBlank()) {
            return "";
        }
        if (publicKey.isBlank()) {
            return privateKey;
        }
        if (privateKey.isBlank()) {
            return publicKey;
        }
        return publicKey + System.lineSeparator() + privateKey;
    }

    private boolean isPublicKeyText(String key) {
        try {
            byte[] decoded = CryptoUtils.fromBase64(key);
            KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isPrivateKeyText(String key) {
        try {
            byte[] decoded = CryptoUtils.fromBase64(key);
            KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
            return true;
        } catch (Exception ex) {
            return false;
        }
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

    private void addAsymmetricAlgorithm(List<AlgorithmItem> items, String key, String displayName, RsaAlgorithm algorithm) {
        algorithms.put(key, algorithm);
        items.add(new AlgorithmItem(key, displayName));
    }
}
