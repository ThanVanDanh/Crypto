package view.panel;

import model.AlgorithmItem;
import model.classic.ClassicAlgorithmRegistry;

import javax.swing.*;
import java.util.List;

public class ClassicPanel extends CategoryPanel {
    public final JTextField caesarKey = new JTextField("3");

    public ClassicPanel(ClassicAlgorithmRegistry registry) {
        List<AlgorithmItem> items = registry.toItems();
        init(items, "Classic Studio", "Khung tối giản: hiện tại chỉ Caesar, bạn có thể tự thêm thuật toán mới vào registry");
        optionCards.add(buildCaesar(), "caesar");
    }

    private JComponent buildCaesar() {
        JPanel p = formGrid(1, 2);
        p.add(field("Key (shift)", caesarKey));
        p.add(field("Guide", new JLabel("Nhập số nguyên, ví dụ: 3 hoặc -2")));
        return p;
    }

    @Override
    protected String sidebarHint() {
        return "Bản khung hiện chỉ có Caesar. Khi thêm thuật toán mới, hãy tạo class thuật toán và register vào ClassicAlgorithmRegistry.";
    }
}
