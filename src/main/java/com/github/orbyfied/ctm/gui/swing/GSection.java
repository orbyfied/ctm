package com.github.orbyfied.ctm.gui.swing;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GSection extends JPanel {

    public static final int LABEL_TYPE_ICON = 0;
    public static final int LABEL_TYPE_TEXT = 1;
    public static final int LABEL_TYPE_BOTH = 2;

    private final String name;
    private JLabel label;

    public GSection(String name, Container container) {
        this(name);
        container.add(this);
    }

    public GSection(String name) {
        this.name = name;
        initialize();
    }

    public void initialize() {
        label = new JLabel(name);
        label.setBackground(new Color(0, 0, 0, 0));
        label.setForeground(Color.WHITE);
        add(label);

        setToolTipText(name);
        setBackground(new Color(0, 0, 0, 0));
        setBorder(new LineBorder(Color.LIGHT_GRAY));
    }

    public GSection withLabel(int type, Object... dataf) {
        if (type == LABEL_TYPE_ICON) {
            Object data = dataf[0];
            setLabelIcon(data);
        } else if (type == LABEL_TYPE_TEXT) {
            String str = (String) dataf[0];
            label.setText(str);
            label.setPreferredSize(new Dimension(label.getFontMetrics(label.getFont()).stringWidth(str), 20));
        } else {
            String str = (String) dataf[0];
            label.setText(str);
            label.setPreferredSize(new Dimension(label.getFontMetrics(label.getFont()).stringWidth(str), 20));
            setLabelIcon(dataf[1]);
        }

        return this;
    }

    public JLabel label() {
        return label;
    }

    public GSection label(Consumer<JLabel> consumer) {
        consumer.accept(label);
        return this;
    }

    protected void setLabelIcon(Object data) {
        label.setText("");
        Icon icon;
        if (data instanceof Icon ic) {
            icon = ic;
        } else if (data instanceof String str) {
            try {
                icon = new ImageIcon(ImageIO.read(getCallerClass(0).getResourceAsStream(str)));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else return;
        label.setIcon(icon);
    }

    public <T extends Component> GSection withComponent(T c, Consumer<T> consumer) {
        add(c);
        consumer.accept(c);
        return this;
    }

    protected Class<?> getCallerClass(int off) {
        try {
            StackTraceElement[] stack;
            try { throw new Exception(); } catch (Exception e) {
                stack = e.getStackTrace();
            }
            StackTraceElement callerElement = stack[2 + off];
            String n = callerElement.getClassName();
            Class<?> c = classCache.computeIfAbsent(n, _ign -> {
                try {
                    return Class.forName(n);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final Map<String, Class<?>> classCache = new HashMap<>();

}
