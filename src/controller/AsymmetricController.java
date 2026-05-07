package controller;

import common.CryptoUtils;
import model.AlgorithmItem;
import model.asymmetric.AsymmetricAlgorithm;
import model.asymmetric.RsaAlgorithm;
import view.MainFrame;
import view.panel.AsymmetricPanel;

import javax.swing.*;
import java.awt.*;
import java.security.KeyFactory;
import java.security.KeyPair;
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
    private final Map<String, AsymmetricAlgorithm> algorithms = new LinkedHashMap<>();
    private AlgorithmItem selected;

    public AsymmetricController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        register(items, "rsa", "RSA", new RsaAlgorithm());
        panel = new AsymmetricPanel(items);
        bind();
    }

    public AsymmetricPanel getPanel() {
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
        AsymmetricAlgorithm algorithm = currentAlgorithm();
        PublicKey publicKey = readPublicKey();
        if (publicKey == null) {
            return;
        }
        runWorker(() -> algorithm.encrypt(input, publicKey));
    }

    private void onSecondary() {
        String input = panel.getInputArea().getText();
        if (!requireInput(input, "Vui lòng nhập ciphertext (Base64)")) {
            return;
        }
        AsymmetricAlgorithm algorithm = currentAlgorithm();
        PrivateKey privateKey = readPrivateKey();
        if (privateKey == null) {
            return;
        }
        runWorker(() -> algorithm.decrypt(input, privateKey));
    }

    private void onGenerate() {
        AsymmetricAlgorithm algorithm = currentAlgorithm();
        int keySizeBits = panel.getKeySizeBits(selected.getKey());
        if (!isSupportedKeySize(algorithm, keySizeBits)) {
            frame.showMessage("Khóa không hợp lệ", "Kích thước key không được hỗ trợ.");
            return;
        }
        try {
            KeyPair keyPair = algorithm.generateKeyPair(keySizeBits);
            panel.setPublicKeyBase64(selected.getKey(), CryptoUtils.toBase64(keyPair.getPublic().getEncoded()));
            panel.setPrivateKeyBase64(selected.getKey(), CryptoUtils.toBase64(keyPair.getPrivate().getEncoded()));
        } catch (Exception ex) {
            frame.showMessage("Lỗi", ex.getMessage());
        }
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

    private AsymmetricAlgorithm currentAlgorithm() {
        AsymmetricAlgorithm algorithm = algorithms.get(selected.getKey());
        if (algorithm == null) {
            throw new IllegalStateException("Chưa đăng ký thuật toán: " + selected.getKey());
        }
        return algorithm;
    }

    private PublicKey readPublicKey() {
        String publicKeyBase64 = panel.getPublicKeyBase64(selected.getKey());
        if (publicKeyBase64.isBlank()) {
            frame.showMessage("Thiếu dữ liệu", "Vui lòng nhập public key (Base64) hoặc Generate.");
            return null;
        }
        try {
            byte[] decoded = CryptoUtils.fromBase64(publicKeyBase64);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception ex) {
            frame.showMessage("Khóa không hợp lệ", "Public key không hợp lệ.");
            return null;
        }
    }

    private PrivateKey readPrivateKey() {
        String privateKeyBase64 = panel.getPrivateKeyBase64(selected.getKey());
        if (privateKeyBase64.isBlank()) {
            frame.showMessage("Thiếu dữ liệu", "Vui lòng nhập private key (Base64) hoặc Generate.");
            return null;
        }
        try {
            byte[] decoded = CryptoUtils.fromBase64(privateKeyBase64);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception ex) {
            frame.showMessage("Khóa không hợp lệ", "Private key không hợp lệ.");
            return null;
        }
    }

    private boolean isSupportedKeySize(AsymmetricAlgorithm algorithm, int keySizeBits) {
        return Arrays.stream(algorithm.supportedKeySizes()).anyMatch(size -> size == keySizeBits);
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

    private void register(List<AlgorithmItem> items, String key, String displayName, AsymmetricAlgorithm algorithm) {
        algorithms.put(key, algorithm);
        items.add(new AlgorithmItem(key, displayName));
    }
}

