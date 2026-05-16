import controller.CryptoController;
import view.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::start);
    }

    private static void start() {
        MainFrame frame = new MainFrame();
        new CryptoController(frame);
        frame.setVisible(true);
    }
}
