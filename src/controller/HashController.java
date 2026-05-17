package controller;

import common.ControllerUtils;
import model.AlgorithmItem;
import model.hash.HashAlgorithm;
import view.MainFrame;
import view.panel.HashPanel;

import java.util.ArrayList;
import java.util.List;

public class HashController {
    private static final String[] SUPPORTED_ALGORITHMS = {
            "MD2", "MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512",
            "SHA-512/224", "SHA-512/256", "SHAKE128", "SHAKE256",
            "BLAKE2B-512", "RIPEMD160", "Whirlpool"
    };

    private final MainFrame frame;
    private final HashPanel panel;
    private final HashAlgorithm model = new HashAlgorithm();
    private AlgorithmItem selected;

    public HashController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>(SUPPORTED_ALGORITHMS.length);
        for (String name : SUPPORTED_ALGORITHMS) {
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
        panel.getBrowseInputFileButton().addActionListener(e ->
                ControllerUtils.selectOpenFileTo(panel, panel.getInputFileField()));
        panel.getHashFileButton().addActionListener(e -> onHashFile());
        panel.getSaveInputTextButton().addActionListener(e -> ControllerUtils.importTextTo(panel, frame, panel.getInputArea()));
        panel.getSaveOutputTextButton().addActionListener(e ->
                ControllerUtils.saveText(panel, frame, "hash-output.txt", panel.getOutputArea().getText(), "Khong co noi dung de luu."));
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
        ControllerUtils.runWorker(panel, panel.getOutputArea(), () -> model.checkSum(input, currentAlgorithm()));
    }

    private void onClear() {
        panel.getInputArea().setText("");
        panel.getOutputArea().setText("");
        panel.getInputFileField().setText("");
    }

    private void onHashFile() {
        String inputPath = panel.getInputFileField().getText();
        if (inputPath == null || inputPath.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long chon input file.");
            return;
        }
        ControllerUtils.runWorker(panel, panel.getOutputArea(), () -> model.hashFile(inputPath, currentAlgorithm()));
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
}
