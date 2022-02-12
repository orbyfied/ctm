package com.github.orbyfied.ctm.gui;

import com.github.orbyfied.ctm.Main;
import com.github.orbyfied.logging.Logger;
import com.github.orbyfied.util.IExecutor;
import com.github.orbyfied.util.TickingExecutor;

import javax.swing.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class CtmGui {

    // constants
    public static final int WIDTH  = 1000;
    public static final int HEIGHT = 600;

    // gui components
    private GridLayout frameLayout;
    private volatile JFrame frame;

    private JPanel exportOptions;

    private JScrollPane consoleScrollPane;
    private JTextPane   consoleTextPane;
    private JPanel      consoleTextPanel;

    private MyScrollbarUI scrollbarUI = new MyScrollbarUI();

    // asynchronous
    public final TickingExecutor mainThreadSyncExecutor = new TickingExecutor();
    public final IExecutor awtThreadSyncExecutor = SwingUtilities::invokeLater;

    // console instance
    private volatile GuiConsole console;

    public static GuiConsole console() { return Main.gui.console; }
    public GuiConsole getConsole() { return console; }

    public JFrame getFrame() {
        return frame;
    }

    public void open() {

        Logger logger = Main.maker.logger;

        // prepare other shit
        UIManager.put("ScrollbarUI", scrollbarUI);

        // prepare console
        consoleTextPanel = new JPanel();
        consoleTextPane = new JTextPane();
        consoleTextPanel.setLayout(new BorderLayout());
        consoleTextPanel.add(consoleTextPane);
        consoleScrollPane = new JScrollPane(consoleTextPanel);
        console = new GuiConsole(this, consoleTextPane);
        Main.maker.logger.addOutput((level, str) -> {
            Color col = Logger.getLevelColor(level);
            String prefix = str.substring(0, 8);
            String body = str.substring(8);

            queueAwtSync(() -> {
                SimpleAttributeSet set = new SimpleAttributeSet();
                StyleConstants.setForeground(set, col);
                StyleConstants.setBold(set, true);
                console.append(prefix, set);
                StyleConstants.setForeground(set, Color.WHITE);
                StyleConstants.setBold(set, false);
                console.append(body, set);
                console.println();
            });
        });

        consoleTextPane.setEditable(false);
        consoleTextPane.setBackground(Color.DARK_GRAY.brighter());
        consoleTextPane.setFont(new Font("Courier New", Font.PLAIN, 20));
        consoleTextPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        consoleTextPane.setSelectionColor(Color.LIGHT_GRAY);
        JScrollBar bar = consoleScrollPane.getVerticalScrollBar();
        bar.setUI(scrollbarUI);
        bar.setUnitIncrement(86);
        bar = consoleScrollPane.getHorizontalScrollBar();
        bar.setUI(scrollbarUI);
        bar.setUnitIncrement(86);
        consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // prepare options
        exportOptions = new JPanel();
        exportOptions.setSize(WIDTH, 700);
        exportOptions.setBackground(Color.DARK_GRAY);

        // construct frame
        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) { e.printStackTrace(); }

            frame = new JFrame("ctm " + Main.VERSION);
            frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setBackground(Color.DARK_GRAY);
            frame.setVisible(true);

            frameLayout = new GridLayout();
            frame.setLayout(frameLayout);

            frame.add(exportOptions);
            frame.add(consoleScrollPane);

            frame.pack();

        });

        logger.log(-5, "-> ctm " + Main.VERSION + " by orbyfied (https://github.com/orbyfied/ctm)");

        // start main sync executor
        mainThreadSyncExecutor.loop();

    }

    public void queueMainSync(Runnable r) {
        mainThreadSyncExecutor.queue(r);
    }

    public void queueAwtSync(Runnable r) {
        awtThreadSyncExecutor.queue(r);
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private static class MyScrollbarUI extends MetalScrollBarUI {

        public MyScrollbarUI() {

            trackColor = Color.LIGHT_GRAY;
            thumbDarkShadowColor = Color.LIGHT_GRAY;
            thumbLightShadowColor = Color.LIGHT_GRAY;
            trackHighlightColor = Color.DARK_GRAY;

        }

        @Override
        public void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(0x3B3E40));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        @Override
        public void paintThumb(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(0x2F2F2F));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width / 3, trackBounds.height);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton jbutton = new JButton();
            jbutton.setPreferredSize(new Dimension(0, 0));
            jbutton.setMinimumSize(new Dimension(0, 0));
            jbutton.setMaximumSize(new Dimension(0, 0));
            return jbutton;
        }

    }

}
