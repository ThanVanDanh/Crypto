package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class MainFrame extends JFrame {
    private final JTabbedPane tabbedPane = new JTabbedPane();

    public MainFrame() {
        super("Crypto Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        setMinimumSize(new Dimension(900, 450));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JComponent buildTabs() {
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        return tabbedPane;
    }


    public void setTabs(Map<String, JComponent> tabs) {
        tabbedPane.removeAll();
        for (Map.Entry<String, JComponent> entry : tabs.entrySet()) {
            tabbedPane.addTab(entry.getKey(), entry.getValue());
        }
    }

    public void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

}
