import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class MediaManager {


    //////////////////////
    // PUBLIC INTERFACE //
    //////////////////////


    public static void main(final String[] args) {
        AmazonDynamoDBSample.MediaTableDemo();

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MediaManager().createAndShowGUI();
            }
        });
    }

    public MediaManager() {
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setOpaque(true);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
//        mainPanel.add(ConfigurationPanel.getInstance());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
//        mainPanel.add(ProgressPanel.getInstance());
        mainPanel.add(Box.createGlue());
    }


    /////////////////////////
    // PROTECTED INTERFACE //
    /////////////////////////


    ///////////////////////
    // PRIVATE INTERFACE //
    ///////////////////////


    private void initLookAndFeel() {
        String lookAndFeel = null;

        if (LOOKANDFEEL != null) {
            if (LOOKANDFEEL.equals("Metal")) {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("System")) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("Motif")) {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            } else if (LOOKANDFEEL.equals("GTK+")) { //new in 1.4.2
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            } else {
                System.err.println("Unexpected value of LOOKANDFEEL specified: "
                                   + LOOKANDFEEL);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }

            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:"
                                   + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel ("
                                   + lookAndFeel
                                   + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (Exception e) {
                System.err.println("Couldn't get specified look and feel ("
                                   + lookAndFeel
                                   + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }

    private void createAndShowGUI() {
        //Set the look and feel.
        initLookAndFeel();

        //Create and set up the window.
        final JFrame frame = new JFrame("Media Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1250, 500));

        //Create and set up the content pane.
        MediaManager gui = new MediaManager();
        gui.mainPanel.setOpaque(true); //content panes must be opaque
        frame.setContentPane(gui.mainPanel);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }


    /////////////////////
    // PRIVATE MEMBERS //
    /////////////////////


    // Swing components
    private final JPanel mainPanel = new JPanel();

    //Specify the look and feel to use.  Valid values:
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    private final static String LOOKANDFEEL = "Metal";
}
