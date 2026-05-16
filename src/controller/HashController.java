package controller;

import common.ControllerUtils;
import model.AlgorithmItem;
import model.hash.Hash;
import view.MainFrame;
import view.panel.HashPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class HashController {
    private final MainFrame frame;
    private final HashPanel panel;
    private final Hash model = new Hash();
    private AlgorithmItem selected;

    public HashController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        String[] supported = new String[]{
                "MD2", "MD5", "SHA-1", "SHA-224", "SHA-384", "SHA-256", "SHA-512",
                "SHA-512/224", "SHA-512/256", "SHAKE128", "SHAKE256",
                "BLAKE2B-512", "RIPEMD160", "Whirlpool"
        };
        for (String name : supported) {
            items.add(new AlgorithmItem(name, name));
        }
        panel = new HashPanel(items);
        bind();
    }

    public HashPanel getPanel() {
        return panel;
    }

    private void bind() {
        panel.getPrimaryButton().addActionListener(e -> onPrimary());
        panel.getClearButton().addActionListener(e -> onClear());
        panel.getBrowseInputFileButton().addActionListener(e -> chooseInputFile());
        panel.getHashFileButton().addActionListener(e -> onHashFile());
        panel.getSaveInputTextButton().addActionListener(e -> onImportInput());
        panel.getSaveOutputTextButton().addActionListener(e -> onSaveText("hash-output.txt", panel.getOutputArea().getText()));
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
        if (!requireInput(input, "Vui long nhap du lieu de hash.")) {
            return;
        }
        runWorker(() -> model.checkSum(input, currentAlgorithm()));
    }

    private void onClear() {
        panel.getInputArea().setText("");
        panel.getOutputArea().setText("");
        panel.getInputFileField().setText("");
    }

    private void chooseInputFile() {
        String path = ControllerUtils.chooseOpenFile(panel);
        if (path != null) {
            panel.getInputFileField().setText(path);
        }
    }

    private void onHashFile() {
        String inputPath = panel.getInputFileField().getText();
        if (inputPath == null || inputPath.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long chon input file.");
            return;
        }
        runWorker(() -> model.hashFile(inputPath, currentAlgorithm()));
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

    private void selectAlgorithm(AlgorithmItem item) {
        selected = item;
        panel.getPrimaryButton().setText("Hash");
    }

    private String currentAlgorithm() {
        if (selected == null) {
            throw new IllegalStateException("Chua chon thuat toan hash.");
        }
        return selected.getKey();
    }

    private boolean requireInput(String input, String message) {
        if (!input.isBlank()) {
            return true;
        }
        frame.showMessage("Thieu du lieu", message);
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
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        swingWorker.execute();
    }
}
