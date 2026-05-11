package view.panel;

import model.AlgorithmItem;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
    private final JButton clearButton = new JButton("Clear");
    private final JComboBox<String> languageBox = new JComboBox<>(new String[]{"ENG", "VIE"});
    private final JLabel optionTitleLabel = new JLabel("Option Deck");
    private final Map<String, KeyAccessor> keyPanels = new LinkedHashMap<>();

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

        JPanel side = card(new BorderLayout(0, 10));
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
        JPanel optionDeck = card(new BorderLayout(0, 10));
        optionTitleLabel.setFont(optionTitleLabel.getFont().deriveFont(Font.BOLD, 14f));
        optionDeck.add(optionTitleLabel, BorderLayout.NORTH);
        optionDeck.add(optionCards, BorderLayout.CENTER);
        workspace.add(optionDeck, BorderLayout.NORTH);

        JPanel split = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 12);

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        split.add(editorCard("Input", inputArea), gbc);

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
        split.add(editorCard("Output", outputArea), gbc);

        workspace.add(split, BorderLayout.CENTER);
        body.add(workspace, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        buildKeyPanels();
    }

    public JList<AlgorithmItem> getAlgorithmList() {
        return algorithmList;
    }

    public JPanel getOptionCards() {
        return optionCards;
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

    public JButton getClearButton() {
        return clearButton;
    }

    public String getSelectedLanguage() {
        Object value = languageBox.getSelectedItem();
        return value == null ? "ENG" : value.toString();
    }

    public String getKeyFor(String algorithmKey) {
        KeyAccessor panel = keyPanels.get(algorithmKey);
        return panel == null ? "" : panel.getKey();
    }

    public void setKeyFor(String algorithmKey, String key) {
        KeyAccessor panel = keyPanels.get(algorithmKey);
        if (panel != null) {
            panel.setKey(key);
        }
    }

    private void buildKeyPanels() {
        JTextField caesarField = new JTextField("");
        JPanel caesarPanel = new JPanel(new BorderLayout(0, 8));
        caesarPanel.setOpaque(false);
        caesarPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JPanel caesarGrid = new JPanel(new GridLayout(1, 1, 10, 10));
        caesarGrid.setOpaque(false);
        caesarGrid.add(field("Key", caesarField));
        JLabel caesarHint = new JLabel("Nhập số nguyên, ví dụ: 3 hoặc -2");
        caesarHint.setForeground(new Color(90, 90, 90));
        caesarPanel.add(caesarGrid, BorderLayout.CENTER);
        caesarPanel.add(caesarHint, BorderLayout.SOUTH);
        addKeyPanel("caesar", caesarPanel, () -> caesarField.getText().trim(), key -> caesarField.setText(key == null ? "" : key.trim()));

        JTextField affineA = new JTextField("");
        JTextField affineB = new JTextField("");
        JPanel affinePanel = new JPanel(new BorderLayout(0, 8));
        affinePanel.setOpaque(false);
        affinePanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JPanel affineGrid = new JPanel(new GridLayout(1, 2, 10, 10));
        affineGrid.setOpaque(false);
        affineGrid.add(field("a", affineA));
        affineGrid.add(field("b", affineB));
        JLabel affineHint = new JLabel("Nhập a,b (ví dụ: 5,8)");
        affineHint.setForeground(new Color(90, 90, 90));
        affinePanel.add(affineGrid, BorderLayout.CENTER);
        affinePanel.add(affineHint, BorderLayout.SOUTH);
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
            String[] parts = key.split(",");
            affineA.setText(parts.length > 0 ? parts[0].trim() : "");
            affineB.setText(parts.length > 1 ? parts[1].trim() : "");
        });

        JTextField hillField = new JTextField("");
        JPanel hillPanel = new JPanel(new BorderLayout(0, 8));
        hillPanel.setOpaque(false);
        hillPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JPanel hillGrid = new JPanel(new GridLayout(1, 1, 10, 10));
        hillGrid.setOpaque(false);
        hillGrid.add(field("Matrix 2x2", hillField));
        JLabel hillHint = new JLabel("Nhap a,b,c,d (vi du: 3,3,2,5)");
        hillHint.setForeground(new Color(90, 90, 90));
        hillPanel.add(hillGrid, BorderLayout.CENTER);
        hillPanel.add(hillHint, BorderLayout.SOUTH);
        addKeyPanel("hill", hillPanel, () -> hillField.getText().trim(), key -> hillField.setText(key == null ? "" : key.trim()));

        JTextArea substitutionArea = new JTextArea(3, 20);
        substitutionArea.setLineWrap(true);
        substitutionArea.setWrapStyleWord(true);
        substitutionArea.setFont(EDITOR_FONT);
        JPanel substitutionPanel = new JPanel(new BorderLayout(0, 8));
        substitutionPanel.setOpaque(false);
        substitutionPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        substitutionPanel.add(field("Alphabet permutation", new JScrollPane(substitutionArea)), BorderLayout.CENTER);
        JLabel substitutionHint = new JLabel("Dung Generate de tao khoa hoan vi nhanh hon.");
        substitutionHint.setForeground(new Color(90, 90, 90));
        substitutionPanel.add(substitutionHint, BorderLayout.SOUTH);
        addKeyPanel("substitution", substitutionPanel,
                () -> substitutionArea.getText().trim(),
                key -> substitutionArea.setText(key == null ? "" : key.trim()));

        JTextField vigenereField = new JTextField("");
        JPanel vigenerePanel = new JPanel(new BorderLayout(0, 8));
        vigenerePanel.setOpaque(false);
        vigenerePanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        JPanel vigenereGrid = new JPanel(new GridLayout(1, 1, 10, 10));
        vigenereGrid.setOpaque(false);
        vigenereGrid.add(field("Key", vigenereField));
        JLabel vigenereHint = new JLabel("Nhập chuỗi ký tự (ví dụ: KEY, hello)");
        vigenereHint.setForeground(new Color(90, 90, 90));
        vigenerePanel.add(vigenereGrid, BorderLayout.CENTER);
        vigenerePanel.add(vigenereHint, BorderLayout.SOUTH);
        addKeyPanel("vigenere", vigenerePanel, () -> vigenereField.getText().trim(), key -> vigenereField.setText(key == null ? "" : key.trim()));
    }

    private void addKeyPanel(String algorithmKey, JComponent panel, Supplier<String> getter, java.util.function.Consumer<String> setter) {
        keyPanels.put(algorithmKey, new KeyAccessor(getter, setter));
        optionCards.add(panel, algorithmKey);
    }

    private JPanel editorCard(String title, JTextArea area) {
        JPanel card = card(new BorderLayout(0, 8));
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label, BorderLayout.WEST);
        card.add(top, BorderLayout.NORTH);
        card.add(new JScrollPane(area), BorderLayout.CENTER);
        return card;
    }

    private JPanel field(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel l = new JLabel(label);
        panel.add(l, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void configureButton(JButton button, Dimension size) {
        button.setMargin(new Insets(4, 10, 4, 10));
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(new CompoundBorder(new LineBorder(new Color(220, 224, 230)), new EmptyBorder(12, 12, 12, 12)));
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
