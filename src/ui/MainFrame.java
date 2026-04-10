package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private ControlPanel controlPanel;
    private InputPanel inputPanel;
    private OutputPanel outputPanel;

    private JLabel lbStatus;
    private JButton btnDarkMode;

    public MainFrame() {
        super("CryptoJavaSwing");
        setTitle("Crypto Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        controlPanel = new ControlPanel();
        inputPanel = new InputPanel();
        outputPanel = new OutputPanel();

        lbStatus = new JLabel("Tình trạng: Sẵn sàng!");
        lbStatus.setFont(new Font("SansSerif", Font.PLAIN, 13));

        btnDarkMode = new JButton("Sáng/Tối");
        btnDarkMode.setFont(new Font("SansSerif", Font.PLAIN, 13));

        //main UI
        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(Color.DARK_GRAY);

        Border cardBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        );

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.white);
        headerPanel.setBorder(cardBorder);

        JPanel leftHeaderPanel = new JPanel();
        leftHeaderPanel.setOpaque(false);
        leftHeaderPanel.setLayout(new BoxLayout(leftHeaderPanel, BoxLayout.Y_AXIS));

        JLabel lbTitle = new JLabel("Crypto Java Swing");
        lbTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        lbTitle.setForeground(new Color(30, 41, 59));

        JLabel lbSubtitle = new JLabel("Encryption • Decryption • Hashing");
        lbSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbSubtitle.setForeground(new Color(100, 116, 139));

        leftHeaderPanel.add(lbTitle);
        leftHeaderPanel.add(Box.createVerticalStrut(4));
        leftHeaderPanel.add(lbSubtitle);

        JPanel rightHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightHeaderPanel.setOpaque(false);
        rightHeaderPanel.add(btnDarkMode);

        headerPanel.add(leftHeaderPanel, BorderLayout.WEST);
        headerPanel.add(rightHeaderPanel, BorderLayout.EAST);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(cardBorder);
        statusPanel.add(lbStatus, BorderLayout.WEST);

        JSplitPane inputOutputSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputPanel);

        inputOutputSplit.setResizeWeight(0.58);
        inputOutputSplit.setContinuousLayout(true);
        inputOutputSplit.setBorder(null);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, inputOutputSplit);
        mainSplit.setResizeWeight(0.25);
        mainSplit.setContinuousLayout(true);
        mainSplit.setBorder(null);

        mainSplit.setResizeWeight(0.25);
        mainSplit.setContinuousLayout(true);
        mainSplit.setBorder(null);

        controlPanel.setPreferredSize(new Dimension(300, 600));
        inputPanel.setPreferredSize(new Dimension(480, 600));
        outputPanel.setPreferredSize(new Dimension(380, 600));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(mainSplit, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

}
