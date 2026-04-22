package view;

import view.panel.ControlPanel;
import view.panel.InputPanel;
import view.panel.OutputPanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private final Map<String, JComponent> tabMap = new LinkedHashMap<>();


    public MainFrame() {
        super("CryptoJavaSwing");
        tabbedPane = new JTabbedPane();
        setTitle("Crypto Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setJMenuBar(buildMenuBar());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);


    }

    public void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem save = new JMenuItem("Save config");
        JMenuItem open = new JMenuItem("Open config");
        JMenuItem exit = new JMenuItem("Thoát");
        exit.addActionListener(e -> dispose());
        fileMenu.add(save);
        fileMenu.add(open);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem shortcuts = new JMenuItem("Keyboard shortcuts");
        shortcuts.addActionListener(e -> showMessage("Keyboard shortcuts", "Ctrl+E: Primary\nCtrl+D: Secondary\nCtrl+G: Generate\nCtrl+K: Clear\nCtrl+1..6: Chuyển tab"));
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> showMessage("About", "CryptoTool refactor theo package MVC."));
        helpMenu.add(shortcuts);
        helpMenu.add(about);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JComponent buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 224, 230)), new EmptyBorder(14, 16, 14, 16)));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("CryptoTool Workbench");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        JLabel subtitle = new JLabel("Mỗi tab là một studio riêng, controller riêng và bộ option riêng");
        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(subtitle);

        wrap.add(left, BorderLayout.WEST);
        return wrap;
    }

    private JComponent buildTabs() {
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        return tabbedPane;
    }

    private JComponent buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 224, 230)), new EmptyBorder(8, 12, 8, 12)));
        return bar;
    }

    public void setTabs(Map<String, JComponent> tabs) {
        tabMap.clear();
        tabMap.putAll(tabs);
        tabbedPane.removeAll();
        for (Map.Entry<String, JComponent> entry : tabs.entrySet()) {
            tabbedPane.addTab(entry.getKey(), entry.getValue());
        }
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

}
