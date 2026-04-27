package controller;

import view.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainController {
    private final MainFrame frame;
    private final Map<String, TabActions> tabTargets = new LinkedHashMap<>();

    public MainController(MainFrame frame) {
        this.frame = frame;
    }

    public void registerTabAction(String tabName,
                                  Runnable primary,
                                  Runnable secondary,
                                  Runnable generate,
                                  Runnable clear) {
        tabTargets.put(tabName, new TabActions(primary, secondary, generate, clear));
    }

    public void bind() {
        bindShortcuts();
    }


    private void bindShortcuts() {
        JRootPane root = frame.getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "primary", this::dispatchPrimary);
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "secondary", this::dispatchSecondary);
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK), "generate", this::dispatchGenerate);
        bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK), "clear", this::dispatchClear);
        for (int i = 0; i < 6; i++) {
            final int index = i;
            bind(inputMap, actionMap, KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, InputEvent.CTRL_DOWN_MASK), "tab" + i, () -> frame.getTabbedPane().setSelectedIndex(index));
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

    private void dispatchPrimary() {
        dispatch(TabActions::primary);
    }

    private void dispatchSecondary() {
        dispatch(TabActions::secondary);
    }

    private void dispatchGenerate() {
        dispatch(TabActions::generate);
    }

    private void dispatchClear() {
        dispatch(TabActions::clear);
    }

    private void dispatch(ActionSelector selector) {
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

    private record TabActions(Runnable primary, Runnable secondary, Runnable generate, Runnable clear) {
    }
}
