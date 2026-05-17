package view.panel;

import common.PanelUtils;
import model.AlgorithmItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ClassicPanel extends JPanel {
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
    private final JComboBox<String> languageBox = new JComboBox<>(new String[]{"ENG", "VIE"});
    private final Map<String, KeyAccessor> keyPanels = new LinkedHashMap<>();
    private KeyAccessor currentKeyPanel;

    public ClassicPanel(List<AlgorithmItem> items) {
        super(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(4, 0, 0, 0));

        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        inputArea.setFont(EDITOR_FONT);
        outputArea.setFont(EDITOR_FONT);
        languageBox.setSelectedItem("ENG");

        algorithmList.setListData(items.toArray(new AlgorithmItem[0]));
        algorithmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel side = PanelUtils.card(new BorderLayout(0, 10));
        side.setPreferredSize(new Dimension(240, 0));
        JLabel label = new JLabel("Algorithms");
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label, BorderLayout.WEST);
        header.add(languageBox, BorderLayout.EAST);
        side.add(header, BorderLayout.NORTH);
        side.add(new JScrollPane(algorithmList), BorderLayout.CENTER);
        add(side, BorderLayout.WEST);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        JPanel workspace = new JPanel(new BorderLayout(0, 12));
        JPanel optionDeck = PanelUtils.card(new BorderLayout(0, 10));
        JLabel optionTitleLabel = new JLabel("Option Deck");
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
        split.add(PanelUtils.editorCard("Input", inputArea, saveInputTextButton), gbc);

        Dimension buttonSize = new Dimension(120, 28);
        JPanel splitOption = new JPanel();
        splitOption.setOpaque(false);
        splitOption.setLayout(new BoxLayout(splitOption, BoxLayout.Y_AXIS));
        PanelUtils.configureButton(primaryButton, buttonSize);
        PanelUtils.configureButton(secondaryButton, buttonSize);
        PanelUtils.configureButton(generateButton, buttonSize);
        PanelUtils.configureButton(clearButton, buttonSize);
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
        split.add(PanelUtils.editorCard("Output", outputArea, saveOutputTextButton), gbc);

        workspace.add(split, BorderLayout.CENTER);
        body.add(workspace, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        buildKeyPanels();
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

    public String getSelectedLanguage() {
        Object value = languageBox.getSelectedItem();
        return value == null ? "ENG" : value.toString();
    }

    public void showOptions(String algorithmKey) {
        CardLayout cl = (CardLayout) optionCards.getLayout();
        cl.show(optionCards, algorithmKey);
        currentKeyPanel = keyPanels.get(algorithmKey);
    }

    public String getKey() {
        return currentKeyPanel == null ? "" : currentKeyPanel.getKey();
    }

    public void setKey(String key) {
        if (currentKeyPanel != null) {
            currentKeyPanel.setKey(key);
        }
    }

    private void buildKeyPanels() {
        JTextField caesarField = new JTextField("");
        JPanel caesarPanel = new JPanel(new BorderLayout(0, 8));
        caesarPanel.setOpaque(false);
        caesarPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JPanel caesarGrid = new JPanel(new GridLayout(1, 1, 10, 10));
        caesarGrid.setOpaque(false);
        caesarGrid.add(PanelUtils.field("Key", caesarField));
        caesarPanel.add(caesarGrid, BorderLayout.CENTER);
        addKeyPanel("caesar", caesarPanel, () -> caesarField.getText().trim(), key -> caesarField.setText(key == null ? "" : key.trim()));

        JTextField affineA = new JTextField("");
        JTextField affineB = new JTextField("");
        JPanel affinePanel = new JPanel(new BorderLayout(0, 8));
        affinePanel.setOpaque(false);
        affinePanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JPanel affineGrid = new JPanel(new GridLayout(1, 2, 10, 10));
        affineGrid.setOpaque(false);
        affineGrid.add(PanelUtils.field("a", affineA));
        affineGrid.add(PanelUtils.field("b", affineB));
        affinePanel.add(affineGrid, BorderLayout.CENTER);
        addKeyPanel("affine", affinePanel, () -> {
            String a = affineA.getText().trim();
            String b = affineB.getText().trim();
            if (a.isEmpty() && b.isEmpty()) {
                return "";
            }
            return a + "," + b;
        }, key -> {
            if (key == null || key.isBlank()) {
                affineA.setText("");
                affineB.setText("");
                return;
            }
            String[] parts = key.trim().split("[,\\s]+");
            affineA.setText(parts.length > 0 ? parts[0].trim() : "");
            affineB.setText(parts.length > 1 ? parts[1].trim() : "");
        });

        JTextField hillField = new JTextField("");
        JPanel hillPanel = new JPanel(new BorderLayout(0, 8));
        hillPanel.setOpaque(false);
        hillPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JPanel hillGrid = new JPanel(new GridLayout(1, 1, 10, 10));
        hillGrid.setOpaque(false);
        hillGrid.add(PanelUtils.field("Matrix 2x2", hillField));
        hillPanel.add(hillGrid, BorderLayout.CENTER);
        addKeyPanel("hill", hillPanel, () -> hillField.getText().trim(), key -> hillField.setText(key == null ? "" : key.trim()));

        JTextArea substitutionArea = new JTextArea(3, 20);
        substitutionArea.setLineWrap(true);
        substitutionArea.setWrapStyleWord(true);
        substitutionArea.setFont(EDITOR_FONT);
        JPanel substitutionPanel = new JPanel(new BorderLayout(0, 8));
        substitutionPanel.setOpaque(false);
        substitutionPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        substitutionPanel.add(PanelUtils.field("Alphabet permutation", new JScrollPane(substitutionArea)), BorderLayout.CENTER);
        addKeyPanel("substitution", substitutionPanel,
                () -> substitutionArea.getText().trim(),
                key -> substitutionArea.setText(key == null ? "" : key.trim()));

        JTextField vigenereField = new JTextField("");
        JPanel vigenerePanel = new JPanel(new BorderLayout(0, 8));
        vigenerePanel.setOpaque(false);
        vigenerePanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JPanel vigenereGrid = new JPanel(new GridLayout(1, 1, 10, 10));
        vigenereGrid.setOpaque(false);
        vigenereGrid.add(PanelUtils.field("Key", vigenereField));
        vigenerePanel.add(vigenereGrid, BorderLayout.CENTER);
        addKeyPanel("vigenere", vigenerePanel, () -> vigenereField.getText().trim(), key -> vigenereField.setText(key == null ? "" : key.trim()));
    }

    private void addKeyPanel(String algorithmKey, JComponent panel, Supplier<String> getter, java.util.function.Consumer<String> setter) {
        KeyAccessor accessor = new KeyAccessor(getter, setter);
        keyPanels.put(algorithmKey, accessor);
        if (currentKeyPanel == null) {
            currentKeyPanel = accessor;
        }
        optionCards.add(panel, algorithmKey);
    }

    private JPanel keyActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        panel.add(copyKeyButton);
        panel.add(saveKeyButton);
        panel.add(importKeyButton);
        return panel;
    }

    private static final class KeyAccessor {
        private final Supplier<String> getter;
        private final java.util.function.Consumer<String> setter;

        private KeyAccessor(Supplier<String> getter, java.util.function.Consumer<String> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public String getKey() {
            return getter.get();
        }

        public void setKey(String key) {
            setter.accept(key);
        }
    }
}
