import controller.ClassicController;
import controller.MainController;
import model.classic.ClassicAlgorithmRegistry;
import view.MainFrame;
import view.panel.ClassicPanel;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::start);
    }

    private static void start() {
        MainFrame frame = new MainFrame();
        ClassicAlgorithmRegistry classicRegistry = ClassicAlgorithmRegistry.caesarOnly();
        ClassicPanel classicPanel = new ClassicPanel(classicRegistry);
        ClassicController classicController = new ClassicController(frame, classicPanel, classicRegistry);
        frame.setTabs(buildTabs(classicPanel));
        bindMainActions(frame, classicController);
        frame.setVisible(true);
    }

    private static Map<String, JComponent> buildTabs(ClassicPanel classicPanel) {
        Map<String, JComponent> tabs = new LinkedHashMap<>();
        tabs.put("Classic", classicPanel);
        return tabs;
    }

    private static void bindMainActions(MainFrame frame, ClassicController classicController) {
        MainController mainController = new MainController(frame);
        mainController.registerTabAction("Classic", classicController::triggerPrimary, classicController::triggerSecondary, classicController::triggerGenerate, classicController::triggerClear);
        mainController.bind();
    }

}

