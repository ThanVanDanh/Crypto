package view.panel;

import common.PanelUtils;
import model.AlgorithmItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AsymmetricPanel extends JPanel {
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
    private final JButton browseOutputFileButton = new JButton("Browse");
    private final JButton encryptFileButton = new JButton("Encrypt file");
    private final JButton decryptFileButton = new JButton("Decrypt file");
    private final JLabel optionTitleLabel = new JLabel("Key Options");
    private final RsaKeyView keyPanel = new RsaKeyView(new int[]{2048, 3072, 4096});

    public AsymmetricPanel(List<AlgorithmItem> items) {
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

        JPanel side = card(new BorderLayout(0, 10));
        side.setPreferredSize(new Dimension(240, 0));
        JLabel label = new JLabel("Algorithms");
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label, BorderLayout.WEST);
        side.add(header, BorderLayout.NORTH);
        side.add(new JScrollPane(algorithmList), BorderLayout.CENTER);
        add(side, BorderLayout.WEST);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        JPanel workspace = new JPanel(new BorderLayout(0, 12));
        JPanel optionDeck = card(new BorderLayout(0, 10));
        optionTitleLabel.setFont(optionTitleLabel.getFont().deriveFont(Font.BOLD, 14f));
        optionDeck.add(optionTitleLabel, BorderLayout.NORTH);
        optionDeck.add(optionCards, BorderLayout.CENTER);
        optionDeck.add(keyActionPanel(), BorderLayout.SOUTH);
        workspace.add(optionDeck, BorderLayout.NORTH);

        JPanel split = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 12);

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        split.add(editorCard("Input", inputArea, saveInputTextButton), gbc);

        Dimension buttonSize = new Dimension(120, 28);
        JPanel splitOption = new JPanel();
        splitOption.setOpaque(false);
        splitOption.setLayout(new BoxLayout(splitOption, BoxLayout.Y_AXIS));
        configureButton(primaryButton, buttonSize);
        configureButton(secondaryButton, buttonSize);
        configureButton(generateButton, buttonSize);
        configureButton(clearButton, buttonSize);
        splitOption.add(primaryButton);
        splitOption.add(Box.createVerticalStrut(8));
        splitOption.add(secondaryButton);
        splitOption.add(Box.createVerticalStrut(8));
        splitOption.add(generateButton);
        splitOption.add(Box.createVerticalStrut(8));
        splitOption.add(clearButton);

        gbc.gridx = 1;
        gbc.weightx = 0.2;
        split.add(splitOption, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 12, 0, 0);
        split.add(editorCard("Output", outputArea, saveOutputTextButton), gbc);

        workspace.add(split, BorderLayout.CENTER);
        workspace.add(filePanel(), BorderLayout.SOUTH);
        body.add(workspace, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        optionCards.add(keyPanel.getPanel(), "rsa");
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

    public JButton getBrowseOutputFileButton() {
        return browseOutputFileButton;
    }

    public JButton getEncryptFileButton() {
        return encryptFileButton;
    }

    public JButton getDecryptFileButton() {
        return decryptFileButton;
    }

    public int getKeySizeBits() {
        return keyPanel.getSelectedKeySize();
    }

    public void showOptions(String algorithmKey) {
        CardLayout cl = (CardLayout) optionCards.getLayout();
        cl.show(optionCards, algorithmKey);
    }

    public String getPublicKeyBase64() {
        return keyPanel.getPublicKeyBase64();
    }

    public String getPrivateKeyBase64() {
        return keyPanel.getPrivateKeyBase64();
    }

    public void setPublicKeyBase64(String key) {
        keyPanel.setPublicKeyBase64(key);
    }

    public void setPrivateKeyBase64(String key) {
        keyPanel.setPrivateKeyBase64(key);
    }

    private JPanel editorCard(String title, JTextArea area, JButton saveButton) {
        return PanelUtils.editorCard(title, area, saveButton);
    }

    private JPanel keyActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        panel.add(copyKeyButton);
        panel.add(saveKeyButton);
        panel.add(importKeyButton);
        return panel;
    }

    private JPanel filePanel() {
        JPanel panel = card(new BorderLayout(0, 8));
        JLabel title = new JLabel("File");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));

        inputFileField.setEditable(false);
        outputFileField.setEditable(false);

        JPanel grid = new JPanel(new GridLayout(2, 1, 8, 8));
        grid.setOpaque(false);
        grid.add(field("Input file", PanelUtils.fileRow(inputFileField, browseInputFileButton)));
        grid.add(field("Output file", PanelUtils.fileRow(outputFileField, browseOutputFileButton)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(encryptFileButton);
        actions.add(decryptFileButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel field(String label, JComponent component) {
        return PanelUtils.field(label, component);
    }

    private void configureButton(JButton button, Dimension size) {
        PanelUtils.configureButton(button, size);
    }

    private JPanel card(LayoutManager layout) {
        return PanelUtils.card(layout);
    }

    private static final class RsaKeyView {
        private final JPanel panel = new JPanel(new BorderLayout(0, 8));
        private final JComboBox<Integer> keySizeBox;
        private final JTextArea publicKeyArea = new JTextArea();
        private final JTextArea privateKeyArea = new JTextArea();

        private RsaKeyView(int[] keySizes) {
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(2, 2, 2, 2));

            Integer[] sizes = new Integer[keySizes.length];
            for (int i = 0; i < keySizes.length; i++) {
                sizes[i] = keySizes[i];
            }
            keySizeBox = new JComboBox<>(sizes);
            keySizeBox.setSelectedIndex(0);

            publicKeyArea.setLineWrap(true);
            publicKeyArea.setWrapStyleWord(true);
            privateKeyArea.setLineWrap(true);
            privateKeyArea.setWrapStyleWord(true);

            JPanel top = new JPanel(new GridLayout(1, 2, 10, 10));
            top.setOpaque(false);
            top.add(field("Key size (bits)", keySizeBox));
            top.add(field("Public key (Base64)", new JScrollPane(publicKeyArea)));

            JPanel bottom = new JPanel(new GridLayout(1, 1, 10, 10));
            bottom.setOpaque(false);
            bottom.add(field("Private key (Base64)", new JScrollPane(privateKeyArea)));

            panel.add(top, BorderLayout.NORTH);
            panel.add(bottom, BorderLayout.CENTER);
        }

        public JPanel getPanel() {
            return panel;
        }

        public int getSelectedKeySize() {
            Integer value = (Integer) keySizeBox.getSelectedItem();
            return value == null ? 0 : value;
        }

        public String getPublicKeyBase64() {
            return publicKeyArea.getText().trim();
        }

        public void setPublicKeyBase64(String key) {
            publicKeyArea.setText(key == null ? "" : key.trim());
        }

        public String getPrivateKeyBase64() {
            return privateKeyArea.getText().trim();
        }

        public void setPrivateKeyBase64(String key) {
            privateKeyArea.setText(key == null ? "" : key.trim());
        }

        private JPanel field(String label, JComponent component) {
            return PanelUtils.field(label, component);
        }
    }
}
