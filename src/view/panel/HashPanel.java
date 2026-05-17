package view.panel;

import common.PanelUtils;
import model.AlgorithmItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class HashPanel extends JPanel {
    private static final Font EDITOR_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private final JList<AlgorithmItem> algorithmList = new JList<>();
    private final JTextArea inputArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JButton primaryButton = new JButton("Primary");
    private final JButton clearButton = new JButton("Clear");
    private final JButton saveInputTextButton = new JButton("Import input");
    private final JButton saveOutputTextButton = new JButton("Save output");
    private final JTextField inputFileField = new JTextField();
    private final JButton browseInputFileButton = new JButton("Browse");
    private final JButton hashFileButton = new JButton("Hash file");

    public HashPanel(List<AlgorithmItem> items) {
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
        PanelUtils.configureButton(clearButton, buttonSize);
        splitOption.add(primaryButton);
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
        workspace.add(filePanel(), BorderLayout.SOUTH);
        body.add(workspace, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

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

    public JButton getBrowseInputFileButton() {
        return browseInputFileButton;
    }

    public JButton getHashFileButton() {
        return hashFileButton;
    }

    private JPanel filePanel() {
        JPanel panel = PanelUtils.card(new BorderLayout(0, 8));
        JLabel title = new JLabel("File");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));

        inputFileField.setEditable(false);

        JPanel grid = new JPanel(new GridLayout(1, 1, 8, 8));
        grid.setOpaque(false);
        grid.add(PanelUtils.field("Input file", PanelUtils.fileRow(inputFileField, browseInputFileButton)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(hashFileButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }
}
