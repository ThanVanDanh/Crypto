package view.panel;

import model.AlgorithmItem;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public abstract class CategoryPanel extends JPanel {
    protected final JList<AlgorithmItem> algorithmList = new JList<>();
    protected final JPanel optionCards = new JPanel(new CardLayout());
    protected final JTextArea inputArea = new JTextArea();
    protected final JTextArea outputArea = new JTextArea();
    protected final JButton primaryButton = new JButton("Primary");
    protected final JButton secondaryButton = new JButton("Secondary");
    protected final JButton generateButton = new JButton("Generate");
    protected final JButton clearButton = new JButton("Clear");
    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();

    protected CategoryPanel() {
        super(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(4, 0, 0, 0));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        algorithmList.setCellRenderer(createAlgorithmRenderer());
    }

    private ListCellRenderer<? super AlgorithmItem> createAlgorithmRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel base = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                base.setBorder(new EmptyBorder(8, 10, 8, 10));
                if (value instanceof AlgorithmItem item) {
                    base.setText(item.isWeak() ? item.getName() + "  • weak" : item.getName());
                }
                return base;
            }
        };
    }

    protected void init(List<AlgorithmItem> algorithms, String title, String subtitle) {
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle);
        algorithmList.setListData(new Vector<>(algorithms));
        if (!algorithms.isEmpty()) {
            algorithmList.setSelectedIndex(0);
        }
        add(buildSidebar(), BorderLayout.WEST);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JComponent buildSidebar() {
        JPanel side = card(new BorderLayout(0, 10));
        side.setPreferredSize(new Dimension(240, 0));
        JLabel label = new JLabel("Algorithms");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        side.add(label, BorderLayout.NORTH);
        side.add(new JScrollPane(algorithmList), BorderLayout.CENTER);
        JTextArea hint = new JTextArea(sidebarHint());
        hint.setEditable(false);
        hint.setOpaque(false);
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        side.add(hint, BorderLayout.SOUTH);
        return side;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.add(buildHeaderCard(), BorderLayout.NORTH);
        body.add(buildWorkspace(), BorderLayout.CENTER);
        return body;
    }

    private JComponent buildHeaderCard() {
        JPanel card = card(new BorderLayout(0, 8));
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        left.add(titleLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitleLabel);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(primaryButton);
        right.add(secondaryButton);
        right.add(generateButton);
        right.add(clearButton);
        card.add(left, BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    protected JPanel buildWorkspace() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.add(optionDeck(), BorderLayout.NORTH);
        body.add(ioDeck(), BorderLayout.CENTER);
        return body;
    }


    protected JComponent optionDeck() {
        JPanel card = card(new BorderLayout(0, 10));
        JLabel label = new JLabel("Option Deck");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        card.add(label, BorderLayout.NORTH);
        card.add(optionCards, BorderLayout.CENTER);
        return card;
    }

    protected JComponent ioDeck() {
        JPanel split = new JPanel(new GridLayout(1, 2, 12, 0));
        split.add(editorCard("Input", inputArea));
        split.add(editorCard("Output", outputArea));
        return split;
    }


    protected JPanel editorCard(String title, JTextArea area) {
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

    protected JPanel formGrid(int rows, int cols) {
        JPanel panel = new JPanel(new GridLayout(rows, cols, 10, 10));
        panel.setOpaque(false);
        return panel;
    }

    protected JPanel field(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel l = new JLabel(label);
        panel.add(l, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    protected JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(new CompoundBorder(new LineBorder(new Color(220, 224, 230)), new EmptyBorder(12, 12, 12, 12)));
        return panel;
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

    protected abstract String sidebarHint();
}
