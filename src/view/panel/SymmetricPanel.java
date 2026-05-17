package view.panel;

import common.PanelUtils;
import model.AlgorithmItem;
import model.symmetric.SymmetricAlgorithm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SymmetricPanel extends JPanel {
    private static final Font EDITOR_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private final JList<AlgorithmItem> algorithmList = new JList<>();
    private final JPanel optionCards = new JPanel(new CardLayout());
    private final JTextArea inputArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JButton primaryButton = new JButton("Primary");
    private final JButton secondaryButton = new JButton("Secondary");
    private final JButton generateButton = new JButton("Generate");
    private final JButton copyKeyButton = new JButton("Copy key");
    private final JButton saveKeyButton = new JButton("Save key");
    private final JButton importKeyButton = new JButton("Import key");
    private final JButton clearButton = new JButton("Clear");
    private final JButton saveInputTextButton = new JButton("Import input");
    private final JButton saveOutputTextButton = new JButton("Save output");
    private final JTextField inputFileField = new JTextField();
    private final JTextField outputFileField = new JTextField();
    private final JButton browseInputFileButton = new JButton("Browse");
    private final JButton encryptFileButton = new JButton("Encrypt file");
    private final JButton decryptFileButton = new JButton("Decrypt file");

    private final Map<String, KeyView> keyViews = new LinkedHashMap<>();
    private KeyView currentView;

    public SymmetricPanel(List<AlgorithmItem> items, Map<String, SymmetricAlgorithm> algorithms) {
        super(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(4, 0, 0, 0));

        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        inputArea.setFont(EDITOR_FONT);
        outputArea.setFont(EDITOR_FONT);

        algorithmList.setListData(items.toArray(new AlgorithmItem[0]));
        algorithmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel side = PanelUtils.card(new BorderLayout(0, 10));
        side.setPreferredSize(new Dimension(240, 0));
        JLabel sideLabel = new JLabel("Algorithms");
        sideLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        sideLabel.setFont(sideLabel.getFont().deriveFont(Font.BOLD, 14f));
        JPanel sideHeader = new JPanel(new BorderLayout());
        sideHeader.setOpaque(false);
        sideHeader.add(sideLabel, BorderLayout.WEST);
        side.add(sideHeader, BorderLayout.NORTH);
        side.add(new JScrollPane(algorithmList), BorderLayout.CENTER);
        add(side, BorderLayout.WEST);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        JPanel workspace = new JPanel(new BorderLayout(0, 12));

        JPanel optionDeck = PanelUtils.card(new BorderLayout(0, 10));
        optionDeck.add(optionCards, BorderLayout.CENTER);
        optionDeck.add(keyActionPanel(), BorderLayout.SOUTH);
        workspace.add(optionDeck, BorderLayout.NORTH);

        JPanel split = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 12);
        split.add(PanelUtils.editorCard("Input", inputArea, saveInputTextButton), gbc);

        Dimension btnSize = new Dimension(120, 28);
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        PanelUtils.configureButton(primaryButton, btnSize);
        PanelUtils.configureButton(secondaryButton, btnSize);
        PanelUtils.configureButton(generateButton, btnSize);
        PanelUtils.configureButton(clearButton, btnSize);
        btnPanel.add(primaryButton);
        btnPanel.add(Box.createVerticalStrut(8));
        btnPanel.add(secondaryButton);
        btnPanel.add(Box.createVerticalStrut(8));
        btnPanel.add(generateButton);
        btnPanel.add(Box.createVerticalStrut(8));
        btnPanel.add(clearButton);

        gbc.gridx = 1;
        gbc.weightx = 0.2;
        gbc.insets = new Insets(0, 0, 0, 0);
        split.add(btnPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 12, 0, 0);
        split.add(PanelUtils.editorCard("Output", outputArea, saveOutputTextButton), gbc);

        workspace.add(split, BorderLayout.CENTER);
        workspace.add(filePanel(), BorderLayout.SOUTH);
        body.add(workspace, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        for (AlgorithmItem item : items) {
            SymmetricAlgorithm alg = algorithms.get(item.getKey());
            KeyView view = alg.isStreamCipher()
                    ? new StreamKeyView(alg.supportedKeySizes())
                    : new BlockKeyView(alg.supportedKeySizes(),
                    alg.getSupportedModes(),
                    alg.getSupportedPaddings());
            keyViews.put(item.getKey(), view);
            optionCards.add(view.getPanel(), item.getKey());
            if (currentView == null) currentView = view;
        }
    }

    public JList<AlgorithmItem> getAlgorithmList() {
        return algorithmList;
    }

    public JTextArea getInputArea() {
        return inputArea;
    }

    public JTextArea getOutputArea() {
        return outputArea;
    }

    public JButton getPrimaryButton() {
        return primaryButton;
    }

    public JButton getSecondaryButton() {
        return secondaryButton;
    }

    public JButton getGenerateButton() {
        return generateButton;
    }

    public JButton getCopyKeyButton() {
        return copyKeyButton;
    }

    public JButton getSaveKeyButton() {
        return saveKeyButton;
    }

    public JButton getImportKeyButton() {
        return importKeyButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JButton getSaveInputTextButton() {
        return saveInputTextButton;
    }

    public JButton getSaveOutputTextButton() {
        return saveOutputTextButton;
    }

    public JTextField getInputFileField() {
        return inputFileField;
    }

    public JTextField getOutputFileField() {
        return outputFileField;
    }

    public JButton getBrowseInputFileButton() {
        return browseInputFileButton;
    }

    public JButton getEncryptFileButton() {
        return encryptFileButton;
    }

    public JButton getDecryptFileButton() {
        return decryptFileButton;
    }

    public void showOptions(String key) {
        ((CardLayout) optionCards.getLayout()).show(optionCards, key);
        currentView = keyViews.get(key);
    }

    public String getKeyBase64() {
        return currentView == null ? "" : currentView.getKeyBase64();
    }

    public int getKeySize() {
        return currentView == null ? 0 : currentView.getSelectedKeySize();
    }

    public String getMode() {
        return currentView instanceof BlockKeyView b ? b.getMode() : "";
    }

    public String getPadding() {
        return currentView instanceof BlockKeyView b ? b.getPadding() : "";
    }

    public void setKeyBase64(String key) {
        if (currentView != null) currentView.setKeyBase64(key);
    }

    private JPanel keyActionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        p.add(copyKeyButton);
        p.add(saveKeyButton);
        p.add(importKeyButton);
        return p;
    }

    private JPanel filePanel() {
        JPanel p = PanelUtils.card(new BorderLayout(0, 8));
        JLabel title = new JLabel("File");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        inputFileField.setEditable(false);
        outputFileField.setEditable(false);

        JPanel grid = new JPanel(new GridLayout(2, 1, 8, 8));
        grid.setOpaque(false);
        grid.add(PanelUtils.field("Input file", PanelUtils.fileRow(inputFileField, browseInputFileButton)));
        grid.add(PanelUtils.field("Output file", outputFileField));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(encryptFileButton);
        actions.add(decryptFileButton);

        p.add(title, BorderLayout.NORTH);
        p.add(grid, BorderLayout.CENTER);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private interface KeyView {
        JPanel getPanel();

        String getKeyBase64();

        void setKeyBase64(String key);

        int getSelectedKeySize();
    }

    private static final class BlockKeyView implements KeyView {
        private final JPanel panel = new JPanel(new BorderLayout(0, 8));
        private final JComboBox<Integer> keySizeBox;
        private final JComboBox<String> modeBox;
        private final JComboBox<String> paddingBox;
        private final JTextField keyField = new JTextField();

        private BlockKeyView(int[] keySizes, String[] modes, String[] paddings) {
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(2, 2, 2, 2));

            Integer[] sizes = new Integer[keySizes.length];
            for (int i = 0; i < keySizes.length; i++) sizes[i] = keySizes[i];
            keySizeBox = new JComboBox<>(sizes);
            if (sizes.length > 0) keySizeBox.setSelectedIndex(0);

            modeBox = new JComboBox<>(modes);
            paddingBox = new JComboBox<>(paddings);
            modeBox.addActionListener(e -> syncPaddingVisibility());

            JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
            grid.setOpaque(false);
            grid.add(PanelUtils.field("Key size", keySizeBox));
            grid.add(PanelUtils.field("Mode", modeBox));
            grid.add(PanelUtils.field("Padding", paddingBox));
            grid.add(PanelUtils.field("Key", keyField));
            panel.add(grid, BorderLayout.CENTER);

            syncPaddingVisibility();
        }

        @Override
        public JPanel getPanel() {
            return panel;
        }

        @Override
        public String getKeyBase64() {
            return keyField.getText().trim();
        }

        @Override
        public void setKeyBase64(String k) {
            keyField.setText(k == null ? "" : k.trim());
        }

        @Override
        public int getSelectedKeySize() {
            Integer v = (Integer) keySizeBox.getSelectedItem();
            return v == null ? 0 : v;
        }

        public String getMode() {
            Object v = modeBox.getSelectedItem();
            return v == null ? "" : v.toString();
        }

        public String getPadding() {
            Object v = paddingBox.getSelectedItem();
            return v == null ? "" : v.toString();
        }

        private void syncPaddingVisibility() {
            boolean fixed = "CTR".equalsIgnoreCase(getMode());
            paddingBox.setEnabled(!fixed);
            if (fixed) paddingBox.setSelectedItem("NoPadding");
        }
    }

    private static final class StreamKeyView implements KeyView {
        private final JPanel panel = new JPanel(new BorderLayout(0, 8));
        private final JComboBox<Integer> keySizeBox;
        private final JTextField keyField = new JTextField();

        private StreamKeyView(int[] keySizes) {
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(2, 2, 2, 2));

            Integer[] sizes = new Integer[keySizes.length];
            for (int i = 0; i < keySizes.length; i++) sizes[i] = keySizes[i];
            keySizeBox = new JComboBox<>(sizes);
            if (sizes.length > 0) keySizeBox.setSelectedIndex(0);

            JPanel grid = new JPanel(new GridLayout(1, 2, 10, 10));
            grid.setOpaque(false);
            grid.add(PanelUtils.field("Key size", keySizeBox));
            grid.add(PanelUtils.field("Key", keyField));
            panel.add(grid, BorderLayout.NORTH);
        }

        @Override
        public JPanel getPanel() {
            return panel;
        }

        @Override
        public String getKeyBase64() {
            return keyField.getText().trim();
        }

        @Override
        public void setKeyBase64(String k) {
            keyField.setText(k == null ? "" : k.trim());
        }

        @Override
        public int getSelectedKeySize() {
            Integer v = (Integer) keySizeBox.getSelectedItem();
            return v == null ? 0 : v;
        }
    }
}
