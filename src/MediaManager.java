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


public class MediaManager implements ActionListener {


    //////////////////////
    // PUBLIC INTERFACE //
    //////////////////////


    public static void main(final String[] args) {
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(MediaPanel.getInstance());
        mainPanel.add(Box.createGlue());
    }


    public void actionPerformed(final ActionEvent event) {
        try {
            //
            // FILE MENU
            //
            if(event.getActionCommand().equals(FILE_REFRESH)) {
                MediaPanel.getInstance().addItems(myItems);
                MediaPanel.getInstance().updateDisplay(0);
            }
            else if(event.getActionCommand().equals(FILE_CLEAR)) {
                MediaPanel.getInstance().clearDisplay();
            }
            else if(event.getActionCommand().equals(FILE_QUIT)) {
                mainFrame.setVisible(false);
                mainFrame.dispose();
                System.exit(0);
            }


            //
            // DYNAMO MENU
            //
            else if(event.getActionCommand().equals(DYNAMO_CREATE)) {
                runDynamoCommand(DYNAMO_CREATE);
            }
            else if(event.getActionCommand().equals(DYNAMO_DESCRIBE)) {
                runDynamoCommand(DYNAMO_DESCRIBE);
            }
            else if(event.getActionCommand().equals(DYNAMO_LOAD)) {
                runDynamoCommand(DYNAMO_LOAD);
            }
            else if(event.getActionCommand().equals(DYNAMO_SCAN)) {
                runDynamoCommand(DYNAMO_SCAN);
            }
            else if(event.getActionCommand().equals(DYNAMO_DELETE)) {
                runDynamoCommand(DYNAMO_DELETE);
            }


            //
            // DEFAULT
            //
            else {
                System.err.println("Unknown action event caught:  " + event.getActionCommand());
            }
        }
        catch(final IOException ioex) {
            System.err.println("MediaManager():  Caught IOException!");
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
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setPreferredSize(new Dimension(600, 500));

        //Create and set up the content pane.
        MediaManager gui = new MediaManager();
        mainFrame.setJMenuBar(gui.createMenuBar());
        gui.mainPanel.setOpaque(true); //content panes must be opaque
        mainFrame.setContentPane(gui.mainPanel);

        //Display the window.
        mainFrame.pack();
        mainFrame.setVisible(true);
    }
    
    
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
    
    
    private void addMenuItem(final JMenu menu, final String name,
                             final String action) {

        final JMenuItem menuItem = new JMenuItem(name);
        menuItem.setActionCommand(action);
        menuItem.addActionListener(this);
        menu.add(menuItem);
    }
    
    
    private void runDynamoCommand(final String action) {
        new Thread() {
            public void run() {
                try {
                    if(action.equals(DYNAMO_CREATE)) {
                        MediaTable.getInstance().createTable(MediaTable.titleAttribute_PK, "S");
                    }
                    else if(action.equals(DYNAMO_DESCRIBE)) {
                        MediaTable.getInstance().describeTable();
                    }
                    else if(action.equals(DYNAMO_LOAD)) {
                        for(final MediaTableData data : loadData()) {
                            MediaTable.getInstance().addItemToTable(data);
                        }
                    }
                    else if(action.equals(DYNAMO_SCAN)) {
                        myItems = MediaTable.getInstance().scanTable(ComparisonOperator.NE.toString(),
                                                                     MediaTable.imdbRatingAttribute,
                                                                     0);
                    }
                    else if(action.equals(DYNAMO_DELETE)) {
                        MediaTable.getInstance().deleteTable();
                    }
                    else {
                        System.err.println("Unknown Dynamo command:  " + action);
                    }
                    
                    System.out.println("\nCommand completed successfully");
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
                    System.err.println("MediaTableDemo:  Caught IOException!");
                    System.err.println("Message:  " + ioex.getMessage());
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
                final int    imdbRating = Integer.parseInt(imdbRatingStr);

                final String base64Image = encodeImage("data/" + child + "/image.jpg");

                final MediaTableData info = new MediaTableData(title,
                                                               rating,
                                                               year,
                                                               runtime,
                                                               director,
                                                               imdbRating,
                                                               base64Image);

                returnList.add(info);
            }
        }
        catch(final FileNotFoundException fnfex) {
            System.err.println("loadData:  Caught FileNotFoundException!");
            System.err.println(fnfex.getMessage());
            fnfex.printStackTrace();
        }
        catch(final IOException ioex) {
            System.err.println("loadData:  Caught IOException!");
            System.err.println(ioex.getMessage());
            ioex.printStackTrace();
        }

        System.out.println("Successfully read " + returnList.size() + " items from disk");
        return returnList;
    }


    private String encodeImage(final String path)
      throws FileNotFoundException, IOException {
        // The encodeBase64 method take a byte[] as the parameter. The byte[] 
        // can be from a simple string like in this example or it can be from
        // an image file data.
        final File image = new File(path);
        byte[] encoded = Base64.encodeBase64(IOUtils.toByteArray(new FileInputStream(image)));
        return new String(encoded);
    }


    @SuppressWarnings("unused")
    private void printDataItem(final List<MediaTableData> data) {
        if(null == data) {
            System.out.println("No data to print!");
            return;
        }

        for(int i = 0; i < data.size(); i++) {
            final MediaTableData datum = data.get(i);

            System.out.println("\nItem " + (i + 1) + " of " + data.size());
            System.out.println("---------------------------------------------\n" +
                               "    Title        :  " + datum.title       + "\n" +
                               "    MPAA Rating  :  " + datum.mpaaRating  + "\n" +
                               "    Year         :  " + datum.year        + "\n" +
                               "    Runtime (min):  " + datum.runtime     + "\n" +
                               "    Director     :  " + datum.director    + "\n" +
                               "    IMDB Rating  :  " + datum.imdbRating  + "\n" +
                               "    Base64 Image :  " + datum.base64Image + "\n" +
                               "---------------------------------------------\n");
        }
    }


    /////////////////////
    // PRIVATE MEMBERS //
    /////////////////////


    // Swing components
    private final JPanel mainPanel = new JPanel();
    private final JFrame mainFrame = new JFrame("Media Manager");

    // data loaded from table scan
    private List<MediaTableData> myItems = null;

    //Specify the look and feel to use.  Valid values:
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    private static final String LOOKANDFEEL = "Metal";

    // action commands - File menu
    private static final String FILE_REFRESH = "file_refresh";
    private static final String FILE_CLEAR = "file_clear";
    private static final String FILE_QUIT = "file_quit";

    // action commands - Dynamo menu
    private static final String DYNAMO_CREATE = "dynamo_create";
    private static final String DYNAMO_DESCRIBE = "dynamo_describe";
    private static final String DYNAMO_LOAD = "dynamo_load";
    private static final String DYNAMO_SCAN = "dynamo_scan";
    private static final String DYNAMO_DELETE = "dynamo_delete";
}
