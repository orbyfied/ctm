package com.github.orbyfied.ctm.gui;

import com.github.orbyfied.ctm.Main;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;

public class GuiConsole {

    public static final Object NO_ATTRIBUTES = new Object() {
        @Override
        public String toString() {
            return "NO_ATTRIBUTES";
        }
    };

    private final CtmGui gui;

    public GuiConsole(CtmGui gui, JTextComponent component) {
        this.gui = gui;

        this.component = component;
        this.document  = component.getDocument();
    }

    private Document document;
    private JTextComponent component;
    private Queue<Object> buffer = new ArrayDeque<>();

    public void append(String str, Color color) {
        if (color != null) {
            SimpleAttributeSet set = new SimpleAttributeSet();
            StyleConstants.setForeground(set, color);
            StyleConstants.setAlignment(set, StyleConstants.ALIGN_RIGHT);
            buffer.add(set);
        } else {
            buffer.add(null);
        }
        buffer.add(str);
    }

    public void append(String str, AttributeSet set) {
        buffer.add(new SimpleAttributeSet(set));
        buffer.add(str);
    }

    public void appendln() {
        buffer.add(NO_ATTRIBUTES);
        buffer.add("\n");
    }

    public void appendln(String str, Color col) {
        append(str, col);
        appendln();
    }

    public void print(String str, Color col) {
        append(str, col);
        flush();
    }

    public void print(String str) {
        print(str, null);
    }

    public void println(String str, Color col) {
        appendln(str, col);
        flush();
    }

    public void println(String str) {
        println(str, null);
    }

    public void println() {
        appendln();
        flush();
    }

    public void printAsync(String str, Color col) {
        gui.queueAwtSync(() -> print(str, col));
    }

    public void printAsync(String str) {
        printAsync(str, null);
    }

    public void printlnAsync(String str, Color col) {
        gui.queueAwtSync(() -> println(str, col));
    }

    public void printlnAsync(String str) {
        printlnAsync(str, null);
    }

    public void printAllAwt(final Object... objs) {
        if (objs.length % 2 != 0) return;
        gui.queueAwtSync(() -> {
            for (int i = 0; i < objs.length; i += 2) {
                String str = (String)objs[i];
                Color col = (Color)objs[i + 1];
                append(str, col);
            }
            flush();
        });
    }

    public void printlnAllAwt(final Object... objs) {
        if (objs.length % 2 != 0) return;
        gui.queueAwtSync(() -> {
            for (int i = 0; i < objs.length; i += 2) {
                String str = (String)objs[i];
                Color col = (Color)objs[i + 1];
                append(str, col);
            }
            appendln();
            flush();
        });
    }

    public void flush() {
        int i = 0;
        Object obj;

        AttributeSet set = null;
        String str;

        while ((obj = buffer.poll()) != null) {
            if (i % 2 != 0)  {
                str = (String)obj;
                try {
                    document.insertString(document.getLength(), str, set);
                } catch (BadLocationException e) { e.printStackTrace(); }
            } else if (obj != NO_ATTRIBUTES) set = (AttributeSet)obj;
            i++;
        }
    }

}
