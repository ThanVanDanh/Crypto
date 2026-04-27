package controller;

import model.AlgorithmItem;
import view.MainFrame;
import view.panel.CategoryPanel;

import javax.swing.*;
import java.awt.*;

public abstract class CategoryController {
    private static final Font EDITOR_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 13);
    protected final MainFrame frame;
    protected final CategoryPanel panel;
    protected AlgorithmItem selected;

    protected CategoryController(MainFrame frame, CategoryPanel panel) {
        this.frame = frame;
        this.panel = panel;
    }

    protected final void initialize() {
        bindCommon();
    }

    private void bindCommon() {
        panel.getPrimaryButton().addActionListener(e -> triggerPrimary());
        panel.getSecondaryButton().addActionListener(e -> triggerSecondary());
        panel.getGenerateButton().addActionListener(e -> triggerGenerate());
        panel.getClearButton().addActionListener(e -> triggerClear());
        panel.getAlgorithmList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                AlgorithmItem item = panel.getAlgorithmList().getSelectedValue();
                if (item != null) {
                    selectAlgorithm(item, true);
                }
            }
        });
        applyEditorFont();
        selectFirstAlgorithm();
    }

    private void selectFirstAlgorithm() {
        if (panel.getAlgorithmList().getModel().getSize() == 0) {
            return;
        }
        selectAlgorithm(panel.getAlgorithmList().getModel().getElementAt(0), false);
    }

    protected void selectAlgorithm(AlgorithmItem item, boolean warn) {
        if (warn && item.isWeak()) {
            int result = JOptionPane.showConfirmDialog(panel,
                    item.getName() + " là thuật toán yếu/legacy. Bạn vẫn muốn tiếp tục?",
                    "Cảnh báo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                panel.getAlgorithmList().setSelectedValue(selected, true);
                return;
            }
        }
        selected = item;
        CardLayout cl = (CardLayout) panel.getOptionCards().getLayout();
        cl.show(panel.getOptionCards(), item.getKey());
        updateButtonLabels(item.getKey());
    }

    protected void applyEditorFont() {
        panel.getInputArea().setFont(EDITOR_FONT);
        panel.getOutputArea().setFont(EDITOR_FONT);
    }

    protected void runWorker(Worker worker) {
        SwingWorker<String, Void> swingWorker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return worker.run();
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

    protected void onClear() {
        panel.getInputArea().setText("");
        panel.getOutputArea().setText("");
    }

    public final void triggerPrimary() {
        onPrimary();
    }

    public final void triggerSecondary() {
        onSecondary();
    }

    public final void triggerGenerate() {
        onGenerate();
    }

    public final void triggerClear() {
        onClear();
    }

    protected final boolean requireInput(String input, String message) {
        if (!input.isBlank()) {
            return true;
        }
        frame.showMessage("Thiếu dữ liệu", message);
        return false;
    }

    protected final void setMainButtonLabels(String primary, String secondary, String generate) {
        panel.getPrimaryButton().setText(primary);
        panel.getSecondaryButton().setText(secondary);
        panel.getGenerateButton().setText(generate);
    }

    protected abstract void onPrimary();

    protected abstract void onSecondary();

    protected abstract void onGenerate();

    protected abstract void updateButtonLabels(String key);

    @FunctionalInterface
    protected interface Worker {
        String run() throws Exception;
    }
}
