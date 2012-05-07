import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.amazonaws.services.dynamodb.model.ComparisonOperator;

import org.apache.commons.codec.binary.Base64;

import org.apache.commons.io.IOUtils;


/**
 * 
 * @author marc
 *
 */
public class MediaManager implements ActionListener {


    //////////////////////
    // PUBLIC INTERFACE //
    //////////////////////


    /**
     * 
     * @param args
     */
    public static void main(final String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MediaManager().createAndShowGUI();
            }
        });
    }


    /**
     * 
     */
    public MediaManager() {
        myMainPanel.setLayout(new BoxLayout(myMainPanel, BoxLayout.PAGE_AXIS));
        myMainPanel.setOpaque(true);
        myMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        myMainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        myMainPanel.add(MediaPanel.getInstance());
        myMainPanel.add(Box.createGlue());
    }


    /**
     * 
     */
    public void actionPerformed(final ActionEvent event) {
        try {
            //
            // FILE MENU
            //

            // Refresh Display
            if(event.getActionCommand().equals(FILE_REFRESH)) {
                MediaPanel.getInstance().refreshDisplay();
            }

            // Clear Display
            else if(event.getActionCommand().equals(FILE_CLEAR)) {
                MediaPanel.getInstance().clearDisplay();
            }

            // Quit
            else if(event.getActionCommand().equals(FILE_QUIT)) {
                myMainFrame.setVisible(false);
                myMainFrame.dispose();
                System.exit(0);
            }


            //
            // DYNAMO MENU
            //

            //:MAINTENANCE
            //   These events are redirected to the runDynamoCommand method
            //   which spawns a new thread to service the request.
            //   Dynamo commands take a long time to complete and it will
            //   hang the GUI otherwise

            else if(event.getActionCommand().equals(DYNAMO_CREATE)   ||
                    event.getActionCommand().equals(DYNAMO_DESCRIBE) ||
                    event.getActionCommand().equals(DYNAMO_LOAD)     ||
                    event.getActionCommand().equals(DYNAMO_SCAN)     ||
                    event.getActionCommand().equals(DYNAMO_DELETE)     ) {

                // forward the request
                runDynamoCommand(event.getActionCommand());
            }


            //
            // DEFAULT
            //
            else {
                System.err.println("Unknown action event caught:  " + event.getActionCommand());
            }
        }
        catch(final IOException ioex) {
            System.err.println(ioex.getMessage());
            ioex.printStackTrace();
        }
    }


    /////////////////////////
    // PROTECTED INTERFACE //
    /////////////////////////


    ///////////////////////
    // PRIVATE INTERFACE //
    ///////////////////////


    /**
     * 
     */
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
            } catch (final ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:"
                                   + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (final UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel ("
                                   + lookAndFeel
                                   + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (final Exception e) {
                System.err.println("Couldn't get specified look and feel ("
                                   + lookAndFeel
                                   + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }


    /**
     * 
     */
    private void createAndShowGUI() {
        //Set the look and feel.
        initLookAndFeel();

        //Create and set up the window.
        myMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myMainFrame.setPreferredSize(new Dimension(600, 500));

        //Create and set up the content pane.
        final MediaManager gui = new MediaManager();
        myMainFrame.setJMenuBar(gui.createMenuBar());
        gui.myMainPanel.setOpaque(true); //content panes must be opaque
        myMainFrame.setContentPane(gui.myMainPanel);

        //Display the window.
        myMainFrame.pack();
        myMainFrame.setVisible(true);
    }


    /**
     * 
     * @return
     */
    private JMenuBar createMenuBar() {
        final JMenuBar menuBar = new JMenuBar();

        // File menu
        final JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // File menu items
        addMenuItem(fileMenu, "Refresh Display", FILE_REFRESH);
        addMenuItem(fileMenu, "Clear Display", FILE_CLEAR);
        addMenuItem(fileMenu, "Quit", FILE_QUIT);


        // Dynamo menu
        final JMenu dynamoMenu = new JMenu("DynamoDB");
        menuBar.add(dynamoMenu);

        // Dynamo menu items
        addMenuItem(dynamoMenu, "Create Table", DYNAMO_CREATE);
        addMenuItem(dynamoMenu, "Describe Table", DYNAMO_DESCRIBE);
        addMenuItem(dynamoMenu, "Load Data Into Table", DYNAMO_LOAD);
        addMenuItem(dynamoMenu, "Scan All Data", DYNAMO_SCAN);
        addMenuItem(dynamoMenu, "Delete Table", DYNAMO_DELETE);

        return menuBar;
    }


    /**
     * 
     * @param menu
     * @param name
     * @param action
     */
    private void addMenuItem(final JMenu menu,
                             final String name,
                             final String action) {

        final JMenuItem menuItem = new JMenuItem(name);
        menuItem.setActionCommand(action);
        menuItem.addActionListener(this);
        menu.add(menuItem);
    }


    /**
     * 
     * @param action
     */
    private void runDynamoCommand(final String action) {
        new Thread() {
            public void run() {
                try {
                    final long start = System.nanoTime();

                    // create table
                    if(action.equals(DYNAMO_CREATE)) {
                        System.out.println("\n---------- DynamoDB / Create Table -------------------------");
                        MediaTable.getInstance().createTable(MediaTable.titleAttribute_PK, "S");
                    }

                    // describe table
                    else if(action.equals(DYNAMO_DESCRIBE)) {
                        System.out.println("\n---------- DynamoDB / Describe Table -----------------------");
                        MediaTable.getInstance().describeTable();
                    }

                    // load data into table
                    else if(action.equals(DYNAMO_LOAD)) {
                        System.out.println("\n---------- DynamoDB / Load Data Into Table -----------------");
                        for(final MediaTableData data : loadData()) {
                            MediaTable.getInstance().addItemToTable(data);
                        }
                    }

                    // scan all data
                    else if(action.equals(DYNAMO_SCAN)) {
                        System.out.println("\n---------- DynamoDB / Scan All Data ------------------------");

                        // get the data from the Dynamo table
                        final List<MediaTableData> items =
                          MediaTable.getInstance().scanTable(MediaTable.titleAttribute_PK,
                                                             ComparisonOperator.NE.toString(),
                                                             "lol");

                        // then add them to the GUI
                        MediaPanel.getInstance().addItems(items);
                    }

                    // delete table
                    else if(action.equals(DYNAMO_DELETE)) {
                        System.out.println("\n---------- DynamoDB / Delete Table -------------------------");
                        MediaPanel.getInstance().clearDisplay();
                        MediaPanel.getInstance().refreshDisplay();
                        MediaTable.getInstance().deleteTable();
                    }

                    // unknown
                    else {
                        System.err.println("Unknown Dynamo command:  " + action);
                        return;
                    }

                    final long end = System.nanoTime();
                    final double totalTime = (end - start) / 1.0e9;
                    System.out.println("\nTotal time:  " + totalTime + " seconds");
                    System.out.println(LOG_FOOTER);
                }
                catch(final AmazonServiceException ase) {
                    System.out.println("Caught an AmazonServiceException, which means your request made it "
                            + "to AWS, but was rejected with an error response for some reason.");
                    System.out.println("Error Message:    " + ase.getMessage());
                    System.out.println("HTTP Status Code: " + ase.getStatusCode());
                    System.out.println("AWS Error Code:   " + ase.getErrorCode());
                    System.out.println("Error Type:       " + ase.getErrorType());
                    System.out.println("Request ID:       " + ase.getRequestId());
                }
                catch(final AmazonClientException ace) {
                    System.out.println("Caught an AmazonClientException, which means the client encountered "
                            + "a serious internal problem while trying to communicate with AWS, "
                            + "such as not being able to access the network.");
                    System.out.println("Error Message: " + ace.getMessage());
                }
                catch(final IOException ioex) {
                    System.err.println(ioex.getMessage());
                    ioex.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * 
     * @return
     */
    private List<MediaTableData> loadData() {
        final List<MediaTableData> returnList = new ArrayList<MediaTableData>();

        try {
            System.out.print("Reading data from disk... ");
        	
            // find all of the folders in the data directory
            final File data = new File("data");
            final String[] children = data.list();

            // then parse the info
            for(final String child : children) {
                if(child.startsWith(".svn")) {
                    continue;
                }

                final File movieInfo = new File("data/" + child + "/info.txt");
                final BufferedReader reader = new BufferedReader(
                                                new FileReader(movieInfo));

                // the text attributes are easy
                final String title = reader.readLine().substring(8);
                final String rating = reader.readLine().substring(9);

                final String yearStr = reader.readLine().substring(7);
                final int    year = Integer.parseInt(yearStr);

                final String runtimeStr = reader.readLine().substring(16);
                final int    runtime = Integer.parseInt(runtimeStr);

                final String director = reader.readLine().substring(11);

                final String imdbRatingStr = reader.readLine().substring(14);
                final String base64ImdbRating = encodeDouble(Double.parseDouble(imdbRatingStr));

                final String base64Image = encodeImage("data/" + child + "/image.jpg");

                final MediaTableData info = new MediaTableData(title,
                                                               rating,
                                                               year,
                                                               runtime,
                                                               director,
                                                               base64ImdbRating,
                                                               base64Image);

                returnList.add(info);
            }
        }
        catch(final FileNotFoundException fnfex) {
            System.err.println(fnfex.getMessage());
            fnfex.printStackTrace();
        }
        catch(final IOException ioex) {
            System.err.println(ioex.getMessage());
            ioex.printStackTrace();
        }

        System.out.println("Done!  Read " + returnList.size() + " items\n");
        return returnList;
    }


    /**
     * 
     * @param path
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String encodeImage(final String path)
      throws FileNotFoundException, IOException {
        // The encodeBase64 method take a byte[] as the parameter. The byte[] 
        // can be from a simple string like in this example or it can be from
        // an image file data.
        final File image = new File(path);
        final byte[] encoded = Base64.encodeBase64(IOUtils.toByteArray(new FileInputStream(image)));
        return new String(encoded);
    }


    /**
     * 
     * @param val
     * @return
     */
    private String encodeDouble(final Double val) {
        final String strVal = new String(val.toString());
        final byte[] encoded = Base64.encodeBase64(strVal.getBytes());
        return new String(encoded);
    }


    /////////////////////
    // PRIVATE MEMBERS //
    /////////////////////


    // Swing components
    private final JPanel myMainPanel = new JPanel();
    private final JFrame myMainFrame = new JFrame("Media Manager");

    //Specify the look and feel to use.  Valid values:
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    private static final String LOOKANDFEEL = "Metal";

    // action commands - File menu
    private static final String FILE_REFRESH = "file_refresh";
    private static final String FILE_CLEAR   = "file_clear";
    private static final String FILE_QUIT    = "file_quit";

    // action commands - Dynamo menu
    private static final String DYNAMO_CREATE   = "dynamo_create";
    private static final String DYNAMO_DESCRIBE = "dynamo_describe";
    private static final String DYNAMO_LOAD     = "dynamo_load";
    private static final String DYNAMO_SCAN     = "dynamo_scan";
    private static final String DYNAMO_DELETE   = "dynamo_delete";

    // GUI
    private static final String LOG_FOOTER =
      "------------------------------------------------------------\n";
}
