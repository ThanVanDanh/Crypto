package controller;

import view.MainFrame;
import view.panel.AsymmetricPanel;
import view.panel.ClassicPanel;
import view.panel.HashPanel;
import view.panel.SymmetricPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class CryptoController {
    private final MainFrame frame;
    private final ClassicController classicController;
    private final SymmetricController symmetricController;
    private final AsymmetricController asymmetricController;
    private final HashController hashController;

    public CryptoController(MainFrame frame) {
        this.frame = frame;
        classicController = new ClassicController(frame);
        symmetricController = new SymmetricController(frame);
        asymmetricController = new AsymmetricController(frame);
        hashController = new HashController(frame);
        wireTabs();
    }

    public void bind() {
        bindMainActions();
    }

    private void wireTabs() {
        ClassicPanel classicPanel = classicController.getPanel();
        SymmetricPanel symmetricPanel = symmetricController.getPanel();
        AsymmetricPanel asymmetricPanel = asymmetricController.getPanel();
        HashPanel hashPanel = hashController.getPanel();
        frame.setTabs(buildTabs(classicPanel, symmetricPanel, asymmetricPanel, hashPanel));
    }

    private Map<String, JComponent> buildTabs(ClassicPanel classicPanel,
                                              SymmetricPanel symmetricPanel,
                                              AsymmetricPanel asymmetricPanel,
                                              HashPanel hashPanel) {
        Map<String, JComponent> tabs = new LinkedHashMap<>();
        tabs.put("Classic", classicPanel);
        tabs.put("Symmetric", symmetricPanel);
        tabs.put("Asymmetric", asymmetricPanel);
        tabs.put("Hash", hashPanel);
        return tabs;
    }

    private void bindMainActions() {
        Map<String, TabActions> tabTargets = new LinkedHashMap<>();
        tabTargets.put("Classic", new TabActions(classicController::triggerPrimary, classicController::triggerSecondary, classicController::triggerGenerate, classicController::triggerClear));
        tabTargets.put("Symmetric", new TabActions(symmetricController::triggerPrimary, symmetricController::triggerSecondary, symmetricController::triggerGenerate, symmetricController::triggerClear));
        tabTargets.put("Asymmetric", new TabActions(asymmetricController::triggerPrimary, asymmetricController::triggerSecondary, asymmetricController::triggerGenerate, asymmetricController::triggerClear));
        tabTargets.put("Hash", new TabActions(hashController::triggerPrimary, this::doNothing, this::doNothing, hashController::triggerClear));

        JRootPane root = frame.getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "primary", () -> dispatch(tabTargets, TabActions::primary));
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "secondary", () -> dispatch(tabTargets, TabActions::secondary));
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK), "generate", () -> dispatch(tabTargets, TabActions::generate));
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK), "clear", () -> dispatch(tabTargets, TabActions::clear));
        for (int i = 0; i < frame.getTabbedPane().getTabCount(); i++) {
            final int index = i;
            bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, InputEvent.CTRL_DOWN_MASK), "tab" + i, () -> {
                if (index < frame.getTabbedPane().getTabCount()) {
                    frame.getTabbedPane().setSelectedIndex(index);
                }
            });
        }
    }

    private void bind(InputMap inputMap, ActionMap actionMap, KeyStroke keyStroke, String key, Runnable runnable) {
        inputMap.put(keyStroke, key);
        actionMap.put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        });
    }

    private void doNothing() {
    }

    private void dispatch(Map<String, TabActions> tabTargets, ActionSelector selector) {
        String title = frame.getTabbedPane().getTitleAt(frame.getTabbedPane().getSelectedIndex());
        TabActions actions = tabTargets.get(title);
        if (actions != null) {
            selector.select(actions).run();
        }
    }

    @FunctionalInterface
    private interface ActionSelector {
        Runnable select(TabActions actions);
    }

    private static final class TabActions {
        private final Runnable primary;
        private final Runnable secondary;
        private final Runnable generate;
        private final Runnable clear;

        private TabActions(Runnable primary, Runnable secondary, Runnable generate, Runnable clear) {
            this.primary = primary;
            this.secondary = secondary;
            this.generate = generate;
            this.clear = clear;
        }

        private Runnable primary() {
            return primary;
        }

        private Runnable secondary() {
            return secondary;
        }

        private Runnable generate() {
            return generate;
        }

        private Runnable clear() {
            return clear;
        }
    }
}

