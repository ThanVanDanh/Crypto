package controller;

import common.ControllerUtils;
import common.CryptoUtils;
import model.AlgorithmItem;
import model.asymmetric.RsaAlgorithm;
import view.MainFrame;
import view.panel.AsymmetricPanel;

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

public class AsymmetricController {
    private final MainFrame frame;
    private final AsymmetricPanel panel;
    private final Map<String, RsaAlgorithm> algorithms = new LinkedHashMap<>();
    private AlgorithmItem selected;

    public AsymmetricController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        algorithms.put("rsa", new RsaAlgorithm());
        items.add(new AlgorithmItem("rsa", "RSA"));
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
        panel.getBrowseInputFileButton().addActionListener(e ->
                ControllerUtils.selectOpenFileTo(panel, panel.getInputFileField()));
        panel.getEncryptFileButton().addActionListener(e -> onEncryptFile());
        panel.getDecryptFileButton().addActionListener(e -> onDecryptFile());
        panel.getSaveInputTextButton().addActionListener(e -> ControllerUtils.importTextTo(panel, frame, panel.getInputArea()));
        panel.getSaveOutputTextButton().addActionListener(e ->
                ControllerUtils.saveText(panel, frame, "output.txt", panel.getOutputArea().getText(), "Khong co noi dung de luu."));
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
        ControllerUtils.runWorker(panel, panel.getOutputArea(), () -> algorithm.encrypt(input, publicKey));
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
        ControllerUtils.runWorker(panel, panel.getOutputArea(), () -> algorithm.decrypt(input, privateKey));
    }

    private void onGenerate() {
        RsaAlgorithm algorithm = currentAlgorithm();
        int keySizeBits = panel.getKeySize();
        if (!isSupportedKeySize(algorithm, keySizeBits)) {
            frame.showMessage("Khoa khong hop le", "Kich thuoc key khong duoc ho tro.");
            return;
        }
        try {
            algorithm.genKeyPair(keySizeBits);
            panel.setPublicKey(algorithm.getPublicKey());
            panel.setPrivateKey(algorithm.getPrivateKey());
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
            panel.setPublicKey(lines.get(0));
            panel.setPrivateKey(lines.get(1));
            return;
        }

        String key = lines.get(0);
        if (isPublicKeyText(key)) {
            panel.setPublicKey(key);
            panel.setPrivateKey("");
        } else if (isPrivateKeyText(key)) {
            panel.setPublicKey("");
            panel.setPrivateKey(key);
        } else {
            frame.showMessage("Khoa khong hop le", "File key khong dung dinh dang RSA.");
        }
    }

    private void onClear() {
        panel.getInputArea().setText("");
        panel.getOutputArea().setText("");
        panel.getInputFileField().setText("");
        panel.getOutputFileField().setText("");
    }

    private void onEncryptFile() {
        String inputPath = panel.getInputFileField().getText().trim();
        if (inputPath.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long chon input file.");
            return;
        }
        String outputPath = inputPath + ".enc";
        RsaAlgorithm algorithm = currentAlgorithm();
        PublicKey publicKey = readPublicKey();
        if (publicKey == null) return;
        ControllerUtils.runFileWorker(panel, panel.getOutputFileField(), () -> {
            algorithm.encryptFile(inputPath, outputPath, publicKey);
            return outputPath;
        });
    }

    private void onDecryptFile() {
        String inputPath = panel.getInputFileField().getText().trim();
        if (inputPath.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long chon input file.");
            return;
        }
        String outputPath = decryptedPath(inputPath);
        RsaAlgorithm algorithm = currentAlgorithm();
        PrivateKey privateKey = readPrivateKey();
        if (privateKey == null) return;
        ControllerUtils.runFileWorker(panel, panel.getOutputFileField(), () -> {
            algorithm.decryptFile(inputPath, outputPath, privateKey);
            return outputPath;
        });
    }

    private static String decryptedPath(String path) {
        if (path != null && path.toLowerCase().endsWith(".enc"))
            return path.substring(0, path.length() - 4);
        return path == null ? "" : path + ".dec";
    }

    private void selectAlgorithm(AlgorithmItem item) {
        selected = item;
        panel.showOptions(item.getKey());
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
        String raw = panel.getPublicKey();
        if (raw.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long nhap public key hoac Generate.");
            return null;
        }
        try {
            byte[] decoded = CryptoUtils.fromBase64(raw);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception ex) {
            frame.showMessage("Khoa khong hop le", "Public key khong hop le.");
            return null;
        }
    }

    private PrivateKey readPrivateKey() {
        String raw = panel.getPrivateKey();
        if (raw.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long nhap private key hoac Generate.");
            return null;
        }
        try {
            byte[] decoded = CryptoUtils.fromBase64(raw);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
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
        String publicKey  = panel.getPublicKey();
        String privateKey = panel.getPrivateKey();
        if (publicKey.isBlank() && privateKey.isBlank()) return "";
        if (publicKey.isBlank())  return privateKey;
        if (privateKey.isBlank()) return publicKey;
        return publicKey + System.lineSeparator() + privateKey;
    }

    private boolean isPublicKeyText(String key) {
        try {
            KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(CryptoUtils.fromBase64(key)));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isPrivateKeyText(String key) {
        try {
            KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(CryptoUtils.fromBase64(key)));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}