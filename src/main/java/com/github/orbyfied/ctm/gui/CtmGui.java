package com.github.orbyfied.ctm.gui;

import com.github.orbyfied.ctm.Main;
import com.github.orbyfied.ctm.gui.swing.GList;
import com.github.orbyfied.ctm.process.Maker;
import com.github.orbyfied.ctm.process.Match;
import com.github.orbyfied.logging.Logger;
import com.github.orbyfied.util.IExecutor;
import com.github.orbyfied.util.TickingExecutor;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalScrollBarUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;

public class CtmGui {

    // constants
    public static final int WIDTH  = 1000;
    public static final int HEIGHT = 600;

    // gui components
    private GridLayout frameLayout;
    private volatile JFrame frame;

    private GList<Match> matchesList;

    private JPanel optionsPanel;

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
        consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        consoleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollBar bar = consoleScrollPane.getVerticalScrollBar();
        bar.setUI(scrollbarUI);
        bar.setUnitIncrement(86);
        JScrollBar bar1 = consoleScrollPane.getHorizontalScrollBar();
        bar1.setUI(scrollbarUI);
        bar1.setUnitIncrement(86);

        // prepare options
        optionsPanel = new JPanel();
        optionsPanel.setSize(WIDTH, 700);
        optionsPanel.setBackground(Color.DARK_GRAY);
//
//        matchesList = new GList<>(CtmGui::createMatchListComponent, (GList<Match> list, int i) -> new Match(Main.maker));
//        matchesList.setNewItemMessage("New Match");
//        matchesList.initialize();
//        matchesList.setBorder(new LineBorder(Color.WHITE, 1));

        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(400, 20));
        field.setEditable(true);
        optionsPanel.add(field);

        JButton button = new JButton();
        button.addActionListener(e -> {
            Maker maker = Main.maker;
            maker.sourceImagePath = Path.of("d");
            maker.export();
        });
        optionsPanel.add(button);

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

            frame.add(optionsPanel);
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

    private static Container createMatchListComponent(GList<Match> list,
                                                      int index,
                                                      Match value) {
        final Dimension dims = new Dimension(80, 20);

        JTextField fieldName    = new JTextField();
        JTextField fieldMatches = new JTextField();
        fieldName.setEditable(true);
        fieldMatches.setEditable(true);
        fieldName.setPreferredSize(dims);
        fieldMatches.setPreferredSize(dims);
        fieldName.setBackground(Color.LIGHT_GRAY);
        fieldMatches.setBackground(Color.LIGHT_GRAY);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());
        panel.add(fieldName);
        panel.add(fieldMatches);
        fieldName.addActionListener(e -> {
            value.tileName = fieldName.getText();
        });
        fieldMatches.addActionListener(e -> {
            value.matches = fieldMatches.getText();
        });
        return panel;

    }

}
