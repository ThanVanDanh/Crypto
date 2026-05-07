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

public class AsymmetricPanel extends JPanel {
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
    private final JLabel optionTitleLabel = new JLabel("Key Options");
    private final Map<String, RsaKeyView> keyPanels = new LinkedHashMap<>();

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
        header.add(languageBox, BorderLayout.EAST);
        languageBox.setVisible(false);
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

        registerKeyPanel("rsa", new RsaKeyView(new int[]{2048, 3072, 4096}));
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

    public int getKeySizeBits(String algorithmKey) {
        RsaKeyView panel = keyPanels.get(algorithmKey);
        return panel == null ? 0 : panel.getSelectedKeySize();
    }

    public String getPublicKeyBase64(String algorithmKey) {
        RsaKeyView panel = keyPanels.get(algorithmKey);
        return panel == null ? "" : panel.getPublicKeyBase64();
    }

    public String getPrivateKeyBase64(String algorithmKey) {
        RsaKeyView panel = keyPanels.get(algorithmKey);
        return panel == null ? "" : panel.getPrivateKeyBase64();
    }

    public void setPublicKeyBase64(String algorithmKey, String key) {
        RsaKeyView panel = keyPanels.get(algorithmKey);
        if (panel != null) {
            panel.setPublicKeyBase64(key);
        }
    }

    public void setPrivateKeyBase64(String algorithmKey, String key) {
        RsaKeyView panel = keyPanels.get(algorithmKey);
        if (panel != null) {
            panel.setPrivateKeyBase64(key);
        }
    }

    private void registerKeyPanel(String algorithmKey, RsaKeyView panel) {
        keyPanels.put(algorithmKey, panel);
        optionCards.add(panel.getPanel(), algorithmKey);
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

            JLabel hint = new JLabel("RSA - Public key dùng để mã hóa, private key để giải mã.");
            hint.setForeground(new Color(90, 90, 90));

            panel.add(top, BorderLayout.NORTH);
            panel.add(bottom, BorderLayout.CENTER);
            panel.add(hint, BorderLayout.SOUTH);
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
            JPanel panel = new JPanel(new BorderLayout(0, 6));
            panel.setOpaque(false);
            JLabel l = new JLabel(label);
            panel.add(l, BorderLayout.NORTH);
            panel.add(component, BorderLayout.CENTER);
            return panel;
        }
    }
}
