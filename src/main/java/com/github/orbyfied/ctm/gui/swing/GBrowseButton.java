package com.github.orbyfied.ctm.gui.swing;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.List;

public class GBrowseButton extends JButton implements ActionListener {

    public static final int OPEN = 0;
    public static final int SAVE = 1;

    public static final int DIRS  = JFileChooser.DIRECTORIES_ONLY;
    public static final int FILES = JFileChooser.FILES_ONLY;
    public static final int ALL   = JFileChooser.FILES_AND_DIRECTORIES;

    private static HashMap<String, Path> lastDirectories = new HashMap<>();

    private String group;
    private Component parent;
    private Consumer<Path> consumer;
    private int type;
    private int mode;
    private List<String> extensions;

    public GBrowseButton(
            String group,
            Component parent,
            Consumer<Path> consumer,
            int type, int mode) {
        super();
        this.group = group;
        this.parent = parent;
        this.consumer = consumer;
        this.setText("...");
        this.addActionListener(this);
        this.type = type;
        this.mode = mode;
    }

    public void browse() {
        JFileChooser fileChooser = new JFileChooser();
        String desc;
        switch (mode) {
            case DIRS  -> desc = "Directories";
            case FILES -> desc = makeExtensionString(extensions);
            case ALL   -> desc = "Directories & " + makeExtensionString(extensions);
            default -> throw new IllegalArgumentException();
        }
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (mode == DIRS) {
                    return f.isDirectory();
                } else {
                    if (f.isDirectory())
                        return true;
                    String[] split = f.getName().split("\\.");
                    if (split.length < 2)
                        return true;
                    return extensions.contains(split[1]);
                }
            }

            @Override
            public String getDescription() {
                return desc;
            }
        });
        fileChooser.setDialogTitle("Browse...");
        fileChooser.setFileSelectionMode(mode);

        Path ls = lastDirectories.get(group);
        if (ls != null)
            fileChooser.setCurrentDirectory(ls.toFile());

        int res;
        if (type == SAVE) {
            res = fileChooser.showSaveDialog(parent);
        } else {
            res = fileChooser.showOpenDialog(parent);
        }
        if (res == JFileChooser.APPROVE_OPTION) {
            lastDirectories.put(group, fileChooser.getSelectedFile().toPath().getParent());
            consumer.accept(fileChooser.getSelectedFile().toPath());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this) {
            browse();
        }
    }

    public GBrowseButton setExtensions(String... ext) {
        extensions = Arrays.asList(ext);
        return this;
    }

    private static String makeExtensionString(List<String> extensions) {
        if (extensions == null)
            return "*.*";
        StringBuilder b = new StringBuilder();
        for (String ext : extensions)
            b.append("*.").append(ext).append(", ");
        b.delete(b.length() - 2, b.length());
        return b.toString();
    }

}
