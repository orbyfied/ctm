package com.github.orbyfied.ctm.gui.swing;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GList<T> extends JPanel {

    private Color ITEM_BACKGROUND_COLOR = Color.LIGHT_GRAY;

    private String newItemMessage;

    private int sizePerItem = 25;

    final List<T> list;
    final IComponentFactory<T> componentFactory;
    final IItemFactory<T> itemFactory;

    final Component exampleComponent;

    final List<Component> itemComponents = new ArrayList<>();
    final GridLayout layout = new GridLayout();
    private JButton addButton;

    public GList(IComponentFactory<T> componentFactory, IItemFactory<T> itemFactory) {
        this(componentFactory, itemFactory, null);
    }

    public GList(IComponentFactory<T> componentFactory,IItemFactory<T> itemFactory, List<T> list) {
        super();
        this.componentFactory = componentFactory;
        this.itemFactory = itemFactory;
        this.list = new ArrayList<>();
        exampleComponent = componentFactory.createComponent(this, 0, itemFactory.createItem(this, 0));
        if (list != null) {
            for (T t : list)
                insertItem(-1, t);
        }
    }

    public void setNewItemMessage(String s) {
        this.newItemMessage = s;
    }

    public List<T> getList() {
        return list;
    }

    public IComponentFactory<T> getComponentFactory() {
        return componentFactory;
    }

    // index = -1 -> add last
    public Container insertItem(int index, final T t) {
        if (index == -1)
            index = list.size();
        list.add(index, t);
        Container c = componentFactory.createComponent(this, index, t);
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        layout.columnWeights = new double[] { 0.2, 0.8 };
        panel.setLayout(layout);
        JButton removeButton = new JButton("-");
        removeButton.addActionListener(e -> {
            removeItem(t);
        });
        removeButton.setFont(MyButtonUI.BUTTON_FONT);
        panel.add(removeButton);
        panel.add(c);
        panel.setBackground(Color.LIGHT_GRAY);
        itemComponents.add(index, panel);
        this.add(panel);
        panel.repaint();
        updateListDisplay();
        return c;
    }

    public void removeItem(int index) {
        list.remove(index);
        Component c = itemComponents.remove(index);
        this.remove(c);
        updateListDisplay();
    }

    public void removeItem(T item) {
        removeItem(list.indexOf(item));
    }

    public void initialize() {
        layout.setColumns(1);
        layout.setRows(list.size() + 1);
        this.setLayout(layout);
        addButton = new JButton(newItemMessage);
        addButton.setFont(MyButtonUI.BUTTON_FONT);
        addButton.addActionListener(e -> {
            insertItem(-1, itemFactory.createItem(this, list.size()));
        });
        this.add(addButton);
        updateListDisplay();
        this.repaint();
    }

    private void updateListDisplay() {
        layout.setRows(list.size() + 1);
        this.setSize(new Dimension(201, list.size() * sizePerItem + 22));
        repaint();
    }

    public interface IComponentFactory<T> {

        Container createComponent(GList<T> list,
                                  int index,
                                  T value);

    }

    public interface IItemFactory<T> {

        T createItem(GList<T> list,
                     int index);

    }

    private static class MyButtonUI extends BasicButtonUI {

        public static final Font BUTTON_FONT = new Font("Courier New", Font.BOLD, 10);
        public static final MyButtonUI THE = new MyButtonUI();

        public MyButtonUI() { }

    }

}
