package controller;

import common.ActionTarget;
import common.ActionType;
import view.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainController {
    private MainFrame  mainFrame;
    private final Map<String, ActionTarget> tabTargets = new LinkedHashMap<>();

    public MainController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
    public void registerTabAction(String tabName, ActionTarget target) {
        tabTargets.put(tabName, target);
    }
    private void bindShortcuts() {
        JRootPane root = mainFrame.getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "primary", () -> dispatch(ActionType.PRIMARY));
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "secondary", () -> dispatch(ActionType.SECONDARY));
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK), "generate", () -> dispatch(ActionType.GENERATE));
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK), "clear", () -> dispatch(ActionType.CLEAR));
        for (int i = 0; i < 6; i++) {
            final int index = i;
            bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, InputEvent.CTRL_DOWN_MASK), "tab" + i, () -> mainFrame.getTabbedPane().setSelectedIndex(index));
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

    private void dispatch(ActionType type) {
        String title = mainFrame.getTabbedPane().getTitleAt(mainFrame.getTabbedPane().getSelectedIndex());
        ActionTarget target = tabTargets.get(title);
        if (target != null) {
            target.dispatch(type);
        }
    }
}
