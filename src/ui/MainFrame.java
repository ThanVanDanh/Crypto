package ui;

import javax.swing.*;

public class MainFrame extends JFrame {
    private ControlPanel controlPanel;
    private InputPanel inputPanel;
    private OutputPanel outputPanel;

    public MainFrame() {
        super("CryptoJavaSwing");
        setTitle("Crypto Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

    }

}
