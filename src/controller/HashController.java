package controller;

import common.ControllerUtils;
import common.CryptoUtils;
import model.AlgorithmItem;
import model.hash.Hash;
import view.MainFrame;
import view.panel.HashPanel;

import javax.swing.*;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

public class HashController {
    private final MainFrame frame;
    private final HashPanel panel;
    private final Hash model = new Hash();
    private AlgorithmItem selected;

    public HashController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        names.addAll(Security.getAlgorithms("MessageDigest"));
        for (String name : names) {
            items.add(new AlgorithmItem(name, name));
        }
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
        onPrimary();
    }

    public void triggerGenerate() {
        frame.showMessage("Khong ho tro", "Hash khong su dung key.");
    }

    public void triggerClear() {
        onClear();
    }

    private void bind() {
        panel.getPrimaryButton().addActionListener(e -> onPrimary());
        panel.getClearButton().addActionListener(e -> onClear());
        panel.getBrowseInputFileButton().addActionListener(e -> chooseInputFile());
        panel.getBrowseOutputFileButton().addActionListener(e -> chooseOutputFile());
        panel.getHashFileButton().addActionListener(e -> onHashFile());
        panel.getSaveInputTextButton().addActionListener(e -> onSaveText("input.txt", panel.getInputArea().getText()));
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
        panel.getOutputFileField().setText("");
    }

    private void chooseInputFile() {
        String path = ControllerUtils.chooseOpenFile(panel);
        if (path != null) {
            panel.getInputFileField().setText(path);
        }
    }

    private void chooseOutputFile() {
        String path = ControllerUtils.chooseSaveFile(panel, "hash-output.txt");
        if (path != null) {
            panel.getOutputFileField().setText(path);
        }
    }

    private void onHashFile() {
        String inputPath = panel.getInputFileField().getText();
        String outputPath = panel.getOutputFileField().getText();
        if (!ControllerUtils.requireFilePaths(frame, inputPath, outputPath)) {
            return;
        }
        runWorker(() -> {
            String output = model.hashFile(inputPath, currentAlgorithm());
            CryptoUtils.writeTextFile(outputPath, output);
            return output;
        });
    }

    private void onSaveText(String defaultName, String content) {
        ControllerUtils.saveText(panel, frame, defaultName, content, "Khong co noi dung de luu.");
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
