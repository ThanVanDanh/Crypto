package controller;

import common.CryptoUtils;
import model.AlgorithmItem;
import view.MainFrame;
import view.panel.HashPanel;

import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class HashController {
    private final MainFrame frame;
    private final HashPanel panel;
    private final Map<String, String> digestNames = new LinkedHashMap<>();
    private AlgorithmItem selected;

    public HashController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        register(items, "md5", "MD5", "MD5");
        register(items, "sha1", "SHA-1", "SHA-1");
        register(items, "sha256", "SHA-256", "SHA-256");
        register(items, "sha512", "SHA-512", "SHA-512");
        panel = new HashPanel(items);
        bind();
    }

    public HashPanel getPanel() {
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
        if (!requireInput(input, "Vui lòng nhập dữ liệu để hash")) {
            return;
        }
        runWorker(() -> hash(input));
    }

    private void onSecondary() {
        onPrimary();
    }

    private void onGenerate() {
        frame.showMessage("Không hỗ trợ", "Hash không hỗ trợ sinh key.");
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
        panel.getPrimaryButton().setText("Hash");
        panel.getSecondaryButton().setText("Hash");
        panel.getGenerateButton().setText("Generate key");
        panel.getSecondaryButton().setEnabled(false);
        panel.getGenerateButton().setEnabled(false);
    }

    private String hash(String input) throws Exception {
        String digestName = digestNames.get(selected.getKey());
        if (digestName == null) {
            throw new IllegalStateException("Chưa đăng ký thuật toán: " + selected.getKey());
        }
        MessageDigest digest = MessageDigest.getInstance(digestName);
        byte[] output = digest.digest(CryptoUtils.utf8(input));
        String format = panel.getOutputFormat(selected.getKey());
        if ("Base64".equalsIgnoreCase(format)) {
            return CryptoUtils.toBase64(output);
        }
        return CryptoUtils.toHex(output);
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

    private void register(List<AlgorithmItem> items, String key, String displayName, String digestName) {
        digestNames.put(key, digestName);
        items.add(new AlgorithmItem(key, displayName));
    }
}

