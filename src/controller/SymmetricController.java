package controller;

import common.ControllerUtils;
import common.CryptoUtils;
import model.AlgorithmItem;
import model.symmetric.SymmetricAlgorithm;
import view.MainFrame;
import view.panel.SymmetricPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SymmetricController {

    private static final String[] MODES    = {"CBC", "ECB", "CFB", "OFB", "CTR", "PCBC", "CTS"};
    private static final String[] PADDINGS = {"PKCS5Padding", "NoPadding", "ISO10126Padding"};

    private final MainFrame      frame;
    private final SymmetricPanel panel;
    private final Map<String, SymmetricAlgorithm> algorithms = new LinkedHashMap<>();
    private AlgorithmItem selected;

    public SymmetricController(MainFrame frame) {
        this.frame = frame;
        List<AlgorithmItem> items = new ArrayList<>();

        addBlock(items, "aes",      "AES",           "AES",
                new int[]{128, 192, 256},              new int[]{16, 24, 32});
        addBlock(items, "des",      "DES",           "DES",
                new int[]{56},                         new int[]{8});
        addBlock(items, "3des",     "DESede (3DES)", "DESede",
                new int[]{112, 168},                   new int[]{16, 24});
        addBlock(items, "blowfish", "Blowfish",      "Blowfish",
                new int[]{32, 64, 128, 192, 256, 448}, new int[]{4, 8, 16, 24, 32, 56});
        addBlock(items, "rc2",      "RC2",           "RC2",
                new int[]{40, 64, 128},                new int[]{5, 8, 16});
        addStream(items, "arcfour", "ARCFOUR (RC4)", "ARCFOUR",
                new int[]{40, 64, 128},                new int[]{5, 8, 16});

        panel = new SymmetricPanel(items, algorithms);
        bind();
    }

    public SymmetricPanel getPanel() { return panel; }

    private void bind() {
        panel.getPrimaryButton().addActionListener(e -> onPrimary());
        panel.getSecondaryButton().addActionListener(e -> onSecondary());
        panel.getGenerateButton().addActionListener(e -> onGenerate());
        panel.getCopyKeyButton().addActionListener(e -> onCopyKey());
        panel.getSaveKeyButton().addActionListener(e -> onSaveKey());
        panel.getImportKeyButton().addActionListener(e -> onImportKey());
        panel.getClearButton().addActionListener(e -> onClear());
        panel.getBrowseInputFileButton().addActionListener(e ->
                ControllerUtils.selectOpenFileTo(panel, panel.getInputFileField()));
        panel.getEncryptFileButton().addActionListener(e -> onEncryptFile());
        panel.getDecryptFileButton().addActionListener(e -> onDecryptFile());
        panel.getSaveInputTextButton().addActionListener(e ->
                ControllerUtils.importTextTo(panel, frame, panel.getInputArea()));
        panel.getSaveOutputTextButton().addActionListener(e ->
                ControllerUtils.saveText(panel, frame, "symmetric-output.txt",
                        panel.getOutputArea().getText(), "Khong co noi dung de luu."));
        panel.getAlgorithmList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                AlgorithmItem item = panel.getAlgorithmList().getSelectedValue();
                if (item != null) selectAlgorithm(item);
            }
        });
        if (panel.getAlgorithmList().getModel().getSize() > 0) {
            panel.getAlgorithmList().setSelectedIndex(0);
            selectAlgorithm(panel.getAlgorithmList().getSelectedValue());
        }
    }

    private void onPrimary() {
        String input = panel.getInputArea().getText();
        if (!requireInput(input, "Vui long nhap plaintext.")) return;
        SymmetricAlgorithm algorithm = currentAlgorithm();
        if (!loadKeyInto(algorithm)) return;
        algorithm.updateConfig(panel.getMode(), panel.getPadding());
        ControllerUtils.runWorker(panel, panel.getOutputArea(), () -> algorithm.encryptText(input));
    }

    private void onSecondary() {
        String input = panel.getInputArea().getText();
        if (!requireInput(input, "Vui long nhap ciphertext.")) return;
        SymmetricAlgorithm algorithm = currentAlgorithm();
        if (!loadKeyInto(algorithm)) return;
        algorithm.updateConfig(panel.getMode(), panel.getPadding());
        ControllerUtils.runWorker(panel, panel.getOutputArea(), () -> algorithm.decryptText(input));
    }

    private void onGenerate() {
        SymmetricAlgorithm algorithm = currentAlgorithm();
        int keySizeBits = panel.getKeySize();
        if (!isSupportedKeySize(algorithm, keySizeBits)) {
            frame.showMessage("Khoa khong hop le", "Kich thuoc key khong duoc ho tro.");
            return;
        }
        try {
            algorithm.genKey(keySizeBits);
            panel.setKeyBase64(algorithm.exportKeyToBase64());
        } catch (Exception ex) {
            frame.showMessage("Loi", ex.getMessage());
        }
    }

    private void onCopyKey() {
        ControllerUtils.copyText(frame, panel.getKeyBase64().trim(), "Vui long tao hoac nhap key truoc.");
    }

    private void onSaveKey() {
        ControllerUtils.saveText(panel, frame, selected.getKey() + "-key.txt",
                panel.getKeyBase64().trim(), "Vui long tao hoac nhap key truoc.");
    }

    private void onImportKey() {
        String content = ControllerUtils.openText(panel, frame);
        if (content == null) return;
        List<String> lines = ControllerUtils.textLines(content);
        if (lines.isEmpty()) {
            frame.showMessage("Thieu du lieu", "File key dang rong.");
            return;
        }
        panel.setKeyBase64(lines.get(0));
    }

    private void onClear() {
        panel.getInputArea().setText("");
        panel.getOutputArea().setText("");
        panel.getInputFileField().setText("");
        panel.getOutputFileField().setText("");
    }

    private void onEncryptFile() { runFileCipher(true);  }
    private void onDecryptFile() { runFileCipher(false); }

    private void runFileCipher(boolean encrypt) {
        String inputPath = panel.getInputFileField().getText().trim();
        if (inputPath.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long chon input file.");
            return;
        }
        String outputPath = encrypt ? inputPath + ".enc" : decryptedPath(inputPath);
        SymmetricAlgorithm algorithm = currentAlgorithm();
        if (!loadKeyInto(algorithm)) return;
        algorithm.updateConfig(panel.getMode(), panel.getPadding());
        ControllerUtils.runFileWorker(panel, panel.getOutputFileField(), () -> {
            if (encrypt) algorithm.encryptFile(inputPath, outputPath);
            else         algorithm.decryptFile(inputPath, outputPath);
            return outputPath;
        });
    }

    private static String decryptedPath(String path) {
        if (path != null && path.toLowerCase().endsWith(".enc"))
            return path.substring(0, path.length() - 4);
        return path == null ? "" : path + ".dec";
    }

    private void selectAlgorithm(AlgorithmItem item) {
        selected = item;
        panel.showOptions(item.getKey());
        panel.getPrimaryButton().setText("Encrypt");
        panel.getSecondaryButton().setText("Decrypt");
        panel.getGenerateButton().setText("Generate key");
        panel.getCopyKeyButton().setEnabled(true);
        panel.getSaveKeyButton().setEnabled(true);
        panel.getImportKeyButton().setEnabled(true);
        panel.getGenerateButton().setEnabled(true);
        panel.getSecondaryButton().setEnabled(true);
    }

    private SymmetricAlgorithm currentAlgorithm() {
        SymmetricAlgorithm alg = algorithms.get(selected.getKey());
        if (alg == null) throw new IllegalStateException("Chua dang ky thuat toan: " + selected.getKey());
        return alg;
    }

    private boolean loadKeyInto(SymmetricAlgorithm algorithm) {
        String keyBase64 = panel.getKeyBase64();
        if (keyBase64.isBlank()) {
            frame.showMessage("Thieu du lieu", "Vui long nhap key (Base64) hoac Generate.");
            return false;
        }
        byte[] keyBytes;
        try {
            keyBytes = CryptoUtils.fromBase64(keyBase64);
        } catch (Exception ex) {
            frame.showMessage("Khoa khong hop le", "Key phai la Base64 hop le.");
            return false;
        }
        if (!isSupportedKeyLength(algorithm, keyBytes.length)) {
            frame.showMessage("Khoa khong hop le", "Kich thuoc key khong duoc ho tro.");
            return false;
        }
        algorithm.importKeyFromBase64(keyBase64);
        return true;
    }

    private boolean isSupportedKeySize(SymmetricAlgorithm alg, int bits) {
        return Arrays.stream(alg.supportedKeySizes()).anyMatch(s -> s == bits);
    }

    private boolean isSupportedKeyLength(SymmetricAlgorithm alg, int lengthBytes) {
        return Arrays.stream(alg.supportedKeySizes())
                .map(alg::keySizeBytes)
                .anyMatch(b -> b == lengthBytes);
    }

    private boolean requireInput(String input, String msg) {
        if (!input.isBlank()) return true;
        frame.showMessage("Thieu du lieu", msg);
        return false;
    }

    private void addBlock(List<AlgorithmItem> items, String key, String displayName,
                          String jceName, int[] keySizes, int[] keyBytes) {
        algorithms.put(key, new SymmetricAlgorithm(jceName, keySizes, keyBytes, MODES, PADDINGS));
        items.add(new AlgorithmItem(key, displayName));
    }

    private void addStream(List<AlgorithmItem> items, String key, String displayName,
                           String jceName, int[] keySizes, int[] keyBytes) {
        algorithms.put(key, new SymmetricAlgorithm(jceName, keySizes, keyBytes));
        items.add(new AlgorithmItem(key, displayName));
    }
}
