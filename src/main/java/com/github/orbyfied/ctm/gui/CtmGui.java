package com.github.orbyfied.ctm.gui;

import com.github.orbyfied.ctm.Main;
import com.github.orbyfied.ctm.gui.swing.GBrowseButton;
import com.github.orbyfied.ctm.gui.swing.GList;
import com.github.orbyfied.ctm.gui.swing.GSection;
import com.github.orbyfied.ctm.process.Maker;
import com.github.orbyfied.ctm.process.Match;
import com.github.orbyfied.logging.Logger;
import com.github.orbyfied.util.IExecutor;
import com.github.orbyfied.util.TickingExecutor;

import javax.swing.*;
import javax.swing.plaf.metal.MetalButtonUI;
import javax.swing.plaf.metal.MetalScrollBarUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.nio.file.Path;

public class CtmGui {

    // constants
    public static final int WIDTH  = 1000;
    public static final int HEIGHT = 600;

    // gui components
    private GridLayout frameLayout;
    private volatile JFrame frame;

    private GList<Match> matchesList;

    private JPanel contentPanel;
    private JPanel optionsPanel;
    private JPanel previewPanel;

    private JButton exportButton;
    private JTextField archiveNameField;
    private JTextField outputDirField;
    private GBrowseButton outputDirBrowse;

    private JTextField sourceImageField;
    private JTextField borderImageField;
    private JTextField cornerImageField;

    private JTextField matchNameField;
    private JTextField matchTargetField;

    private JTextField borderSizeField;
    private JCheckBox testBorderCheckbox;

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
        consoleTextPane.setSelectionColor(Color.LIGHT_GRAY);
        consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        consoleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollBar barh = consoleScrollPane.getHorizontalScrollBar();
        barh.setUI(new MyScrollbarUI());
        barh.setUnitIncrement(86);
        JScrollBar barv = consoleScrollPane.getVerticalScrollBar();
        barv.setUI(new MyScrollbarUI());
        barv.setVisible(false);
        barv.setUnitIncrement(86);
        consoleScrollPane.setVerticalScrollBar(barv);

        // prepare options
        optionsPanel = new JPanel();
        optionsPanel.setBackground(Color.DARK_GRAY);
//        optionsPanel.setLayout(new GridLayout());

        optionsPanel.add(exportButton = new JButton("Export"));
        exportButton.setPreferredSize(new Dimension(90, 30));
        exportButton.setUI(new MyButtonUI());
        exportButton.addActionListener(e -> {
            Maker maker = Main.maker;
            logger.stage("gui-export");

            String archivename = archiveNameField.getText();
            if (archivename.isBlank()) {
                logger.err("no archive name specified");
                return;
            }
            maker.archiveName = archivename;

            String outputdir = outputDirField.getText();
            if (outputdir.isBlank()) {
                outputdir = ".";
            }
            maker.outputDir = Path.of(outputdir);

            String sourceImage = sourceImageField.getText();
            if (sourceImage.isBlank()) {
                logger.err("no source image specified");
                return;
            }
            maker.sourceImagePath = Path.of(sourceImage);

            String borderImage = borderImageField.getText();
            if (borderImage.isBlank()) {
                logger.err("no border image specified");
                return;
            }
            maker.borderImagePath = Path.of(borderImage);

            String cornerImage = cornerImageField.getText();
            if (!cornerImage.isBlank()) {
                maker.cornerImagePath = Path.of(cornerImage);
            }

            String borderSize = borderSizeField.getText();
            if (borderSize.isBlank()) {
                borderSize = "0";
            }
            maker.borderSizePx = Integer.parseInt(borderSize);

            maker.testBorderSize = testBorderCheckbox.isSelected();

            String matchTarget = matchTargetField.getText();
            if (matchTarget.isBlank()) {
                logger.err("no match target specified");
                return;
            }

            String matchName = matchNameField.getText();
            if (matchName.isBlank()) {
                matchName = matchTarget;
            }

            Match match = new Match(maker).withProperties(matchTarget, matchName);
            maker.addMatch(match);

            Main.doExport();
        });
        exportButton.setForeground(Color.WHITE);

        GSection section0 = new GSection("Archive", optionsPanel);
        section0.withComponent(archiveNameField = new JTextField(), f -> {
            f.setPreferredSize(new Dimension(100, 20));
        });

        GSection section1 = new GSection("Output", optionsPanel);
        section1.withComponent(outputDirField = new JTextField(), f -> {
            f.setPreferredSize(new Dimension(100, 20));
        }).withComponent(outputDirBrowse = new GBrowseButton(
                "all",
                optionsPanel,
                path -> outputDirField.setText(path.toString()),
                GBrowseButton.SAVE, GBrowseButton.DIRS), b -> {
            b.setPreferredSize(new Dimension(50, 20));
        });


        JPanel sourcesPanel = new JPanel();
        sourcesPanel.setBackground(new Color(0, 0, 0, 0));
        GridLayout lsp = new GridLayout();
        lsp.setRows(3);
        lsp.setColumns(1);
        sourcesPanel.setLayout(lsp);
        optionsPanel.add(sourcesPanel);

        new GSection("Source", sourcesPanel)
                .withComponent(sourceImageField = new JTextField(), f -> {
                    f.setPreferredSize(new Dimension(100, 20));
                }).withComponent(new GBrowseButton(
                        "all",
                        optionsPanel,
                        path -> sourceImageField.setText(path.toString()),
                        GBrowseButton.OPEN, GBrowseButton.FILES), b -> {
            b.setExtensions("png", "jpg", "jpeg", "bmp");
            b.setPreferredSize(new Dimension(50, 20));
        });

        new GSection("Border", sourcesPanel)
                .withComponent(borderImageField = new JTextField(), f -> {
                    f.setPreferredSize(new Dimension(100, 20));
                }).withComponent(new GBrowseButton(
                "all",
                optionsPanel,
                path -> borderImageField.setText(path.toString()),
                GBrowseButton.OPEN, GBrowseButton.FILES), b -> {
            b.setExtensions("png", "jpg", "jpeg", "bmp");
            b.setPreferredSize(new Dimension(50, 20));
        });

        new GSection("Corner", sourcesPanel)
                .withComponent(cornerImageField = new JTextField(), f -> {
                    f.setPreferredSize(new Dimension(100, 20));
                }).withComponent(new GBrowseButton(
                "all",
                optionsPanel,
                path -> cornerImageField.setText(path.toString()),
                GBrowseButton.OPEN, GBrowseButton.FILES), b -> {
            b.setExtensions("png", "jpg", "jpeg", "bmp");
            b.setPreferredSize(new Dimension(50, 20));
        });

        new GSection("Thickness", optionsPanel)
                .withComponent(borderSizeField = new JTextField(), f -> {
                    f.setPreferredSize(new Dimension(50, 20));
                }).withComponent(testBorderCheckbox = new JCheckBox("Test"), c -> {
            c.setBackground(new Color(0, 0, 0, 0));
        });

        new GSection("Match", optionsPanel)
                .withComponent(new JPanel(), p -> {
                    GridLayout l = new GridLayout();
                    l.setRows(2);
                    l.setColumns(2);
                    p.setLayout(l);
                    p.setBackground(new Color(0, 0, 0, 0));
                    p.setPreferredSize(new Dimension(200, 40));
                    JLabel label;
                    p.add(label = new JLabel("Name"));
                    label.setForeground(Color.LIGHT_GRAY);
                    p.add(matchNameField = new JTextField());
                    matchNameField.setPreferredSize(new Dimension(100, 20));
                    p.add(label = new JLabel("Target"));
                    label.setForeground(Color.LIGHT_GRAY);
                    p.add(matchTargetField = new JTextField());
                    matchTargetField.setPreferredSize(new Dimension(150, 20));
                });

        // prepare preview
        previewPanel = new JPanel();
        previewPanel.setBackground(new Color(0x343434));

        contentPanel = new JPanel();
        GridLayout contentPanelLayout = new GridLayout();
        contentPanelLayout.setColumns(2);
        contentPanelLayout.setRows(1);
        contentPanel.setLayout(contentPanelLayout);
        contentPanel.add(optionsPanel);
        contentPanel.add(previewPanel);

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
            frameLayout.setRows(2);
            frameLayout.setColumns(1);
            frame.setLayout(frameLayout);

            frame.add(contentPanel);
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

    private static class MyButtonUI extends MetalButtonUI {

        public MyButtonUI() {

            super();

        }

        @Override
        public void paint(Graphics g1, JComponent c) {
            AbstractButton b = (AbstractButton)c;
            Graphics2D g = (Graphics2D)g1;
            ButtonModel model = b.getModel();

            g.setColor(new Color(0x313647));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());

            g.setColor(Color.WHITE);
            paintText(g1, b, b.getBounds(), b.getText());
            super.paint(g1, c);
        }

        @Override
        public void paintButtonPressed(Graphics g, AbstractButton b) {
            g.setColor(Color.BLACK);
            paintText(g, b, b.getBounds(), b.getText());
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, b.getSize().width, b.getSize().height);
        }

        public void paintBorder(Graphics g) {
        }

        @Override
        protected void paintFocus(Graphics g, AbstractButton b,
                                  Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
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
            value.target = fieldMatches.getText();
        });
        return panel;

    }

}
