package common;

import view.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ControllerUtils {
    private ControllerUtils() {
    }

    public static void copyText(MainFrame frame, String content, String emptyMessage) {
        if (content == null || content.isBlank()) {
            frame.showMessage("Thieu du lieu", emptyMessage);
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content), null);
        frame.showMessage("Hoan tat", "Da sao chep.");
    }

    public static void saveText(Component parent, MainFrame frame, String defaultName, String content, String emptyMessage) {
        if (content == null || content.isBlank()) {
            frame.showMessage("Thieu du lieu", emptyMessage);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = CryptoUtils.withTxtExtension(chooser.getSelectedFile());
        try {
            CryptoUtils.writeTextFile(file.getAbsolutePath(), content);
            frame.showMessage("Hoan tat", "Da luu file: " + file.getAbsolutePath());
        } catch (Exception ex) {
            frame.showMessage("Loi", ex.getMessage());
        }
    }

    public static String openText(Component parent, MainFrame frame) {
        String path = chooseOpenFile(parent);
        if (path == null) {
            return null;
        }
        try {
            return CryptoUtils.readTextFile(path);
        } catch (Exception ex) {
            frame.showMessage("Loi", ex.getMessage());
            return null;
        }
    }

    public static List<String> textLines(String content) {
        List<String> lines = new ArrayList<>();
        if (content == null) {
            return lines;
        }
        for (String line : content.split("\\R")) {
            String value = line.trim();
            if (!value.isBlank()) {
                lines.add(value);
            }
        }
        return lines;
    }

    public static String chooseOpenFile(Component parent) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    public static String chooseSaveFile(Component parent, String defaultName) {
        JFileChooser chooser = new JFileChooser();
        if (defaultName != null && !defaultName.isBlank()) {
            chooser.setSelectedFile(new File(defaultName));
        }
        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    public static boolean requireFilePaths(MainFrame frame, String inputPath, String outputPath) {
        if (inputPath == null || inputPath.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long chon input file.");
            return false;
        }
        if (outputPath == null || outputPath.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long chon output file.");
            return false;
        }
        return true;
    }
}
