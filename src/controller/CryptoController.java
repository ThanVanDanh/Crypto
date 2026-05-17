package controller;

import view.MainFrame;

import javax.swing.JComponent;
import java.util.LinkedHashMap;
import java.util.Map;

public class CryptoController {
    private final MainFrame frame;

    public CryptoController(MainFrame frame) {
        this.frame = frame;
        wireTabs();
    }

    private void wireTabs() {
        frame.setTabs(buildTabs());
    }

    private Map<String, JComponent> buildTabs() {
        Map<String, JComponent> tabs = new LinkedHashMap<>();
        tabs.put("Classic", new ClassicController(frame).getPanel());
        tabs.put("Symmetric", new SymmetricController(frame).getPanel());
        tabs.put("Asymmetric", new AsymmetricController(frame).getPanel());
        tabs.put("Hash", new HashController(frame).getPanel());
        return tabs;
    }
}
