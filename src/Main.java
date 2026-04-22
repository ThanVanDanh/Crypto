import view.MainFrame;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();

            Map<String, JComponent> tabs = new LinkedHashMap<>();
            frame.setTabs(tabs);


            frame.setVisible(true);
        });
    }

}

