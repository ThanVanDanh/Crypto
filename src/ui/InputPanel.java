package ui;

import javax.swing.*;
import java.awt.*;

public class InputPanel extends JPanel {
    JTextField inputField;

    public InputPanel() {
        setBackground(Color.RED);
        setVisible(true);

        inputField = new JTextField();
        inputField.setBackground(Color.white);

        add(inputField);
    }
}
