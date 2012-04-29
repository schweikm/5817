import java.awt.Dimension;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.codec.binary.Base64;


public class MediaPanel extends JPanel implements ActionListener {


    //////////////////////
    // PUBLIC INTERFACE //
    //////////////////////


    /**
     * 
     * @return
     */
    public static MediaPanel getInstance() {
        if(null == theInstance) {
            theInstance = new MediaPanel();
        }

        return theInstance;
    }


    /**
     * 
     * @param items
     */
    public void addItems(final List<MediaTableData> items) {
        for(final MediaTableData item : items) {
            myItems.add(item);
        }
        Collections.sort(myItems);
    }


    /**
     * 
     */
    public void actionPerformed(final ActionEvent event) {
        try {
            if(event.getActionCommand().equals(PREVIOUS_BUTTON)) {
                if(mySelectedIndex > 0) {
                    updateDisplay(--mySelectedIndex);
                }
            }
            else if(event.getActionCommand().equals(NEXT_BUTTON)) {
                if(mySelectedIndex < (myItems.size() - 1)) {
                    updateDisplay(++mySelectedIndex);
                }
            }
            else {
                System.err.println("MediaPanel:  Unknown action received:  " + event.getActionCommand());
            }
        }
        catch(final IOException ioex) {
            System.err.println("MediaManager::actionPerformed - Caught IOException!");
            System.err.println(ioex.getMessage());
            ioex.printStackTrace();
        }
    }


    /**
     * 
     * @param index
     */
    public void updateDisplay(final int index) throws IOException {
        if(index >= myItems.size()) {
            System.err.println("MediaPanel::updateDisplay() - Invalid index specified!  " +
                               "Index:  " + index + "  Size:  " + myItems.size());
            return;
        }

        myTitleTextField.setText(myItems.get(index).title);
        myMpaaRatingTextField.setText(myItems.get(index).mpaaRating);
        myYearTextField.setText(((Integer)myItems.get(index).year).toString());
        myRuntimeTextField.setText(((Integer)myItems.get(index).runtime).toString());
        myDirectorTextField.setText(myItems.get(index).director);
        myImdbRatingTextField.setText(((Integer)myItems.get(index).imdbRating).toString());

        // reconstruct the image from the Base64 encoded string
        final byte[] imageBytes = Base64.decodeBase64(myItems.get(index).base64Image);
        final InputStream in = new ByteArrayInputStream(imageBytes);
        final BufferedImage bImageFromConvert = ImageIO.read(in);
        myImageLabel.setIcon(new ImageIcon(bImageFromConvert));
    }


    public void clearDisplay() {
        myTitleTextField.setText("");
        myMpaaRatingTextField.setText("");
        myYearTextField.setText("");
        myRuntimeTextField.setText("");
        myDirectorTextField.setText("");
        myImdbRatingTextField.setText("");

        myImageLabel.setIcon(null);
        myImageLabel.setText("No data loaded");

        myItems.clear();
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
    private MediaPanel() {
        this.setLayout(new GridLayout(1, 1));
        addComponentsToPanel();
        clearDisplay();
    }


    /**
     * 
     */
    private void addComponentsToPanel() {
        final JPanel imagePanel = new JPanel();
        final JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(10, 1));


        //
        // TITLE
        //
        final JLabel titleLabel = new JLabel("Title");
        myTitleTextField.setEditable(false);
        textPanel.add(titleLabel);
        textPanel.add(myTitleTextField);


        //
        // MPAA RATING
        //
        final JLabel mpaaRatingLabel = new JLabel("MPAA Rating");
        myMpaaRatingTextField.setEditable(false);
        textPanel.add(mpaaRatingLabel);
        textPanel.add(myMpaaRatingTextField);


        //
        // YEAR
        //
        final JLabel yearLabel = new JLabel("Year");
        myYearTextField.setEditable(false);
        textPanel.add(yearLabel);
        textPanel.add(myYearTextField);


        //
        // RUNTIME
        //
        final JLabel runtimeLabel = new JLabel("Runtime (min)");
        myRuntimeTextField.setEditable(false);
        textPanel.add(runtimeLabel);
        textPanel.add(myRuntimeTextField);


        //
        // DIRECTOR
        //
        final JLabel directorLabel = new JLabel("Director");
        myDirectorTextField.setEditable(false);
        textPanel.add(directorLabel);
        textPanel.add(myDirectorTextField);


        //
        // IMDB RATING
        //
        final JLabel imdbRatingLabel = new JLabel("IMDB Rating");
        myImdbRatingTextField.setEditable(false);
        textPanel.add(imdbRatingLabel);
        textPanel.add(myImdbRatingTextField);


        //
        // PREVIOUS BUTTON
        //
        final JButton previousButton = new JButton("Previous");
        previousButton.setActionCommand(PREVIOUS_BUTTON);
        previousButton.addActionListener(this);
        textPanel.add(previousButton);


        //
        // NEXT BUTTON
        //
        final JButton nextButton = new JButton("Next");
        nextButton.setActionCommand(NEXT_BUTTON);
        nextButton.addActionListener(this);
        textPanel.add(nextButton);


        //
        // MOVIE POSTER
        //
        myImageLabel.setPreferredSize(new Dimension(220, 328));
        imagePanel.add(myImageLabel);

        this.add(imagePanel);
        this.add(textPanel);
    }


    /////////////////////
    // PRIVATE MEMBERS //
    /////////////////////


    // Singleton instance
    private static MediaPanel theInstance = null;

    // swing components
    private final JTextField myTitleTextField = new JTextField();
    private final JTextField myMpaaRatingTextField = new JTextField();
    private final JTextField myYearTextField = new JTextField();
    private final JTextField myRuntimeTextField = new JTextField();
    private final JTextField myDirectorTextField = new JTextField();
    private final JTextField myImdbRatingTextField = new JTextField();
    private final JLabel myImageLabel = new JLabel();

    // action commands
    private static final String PREVIOUS_BUTTON = "previous_button";
    private static final String NEXT_BUTTON = "next_button";

    // list of items to display
    private final List<MediaTableData> myItems = new ArrayList<MediaTableData>();
    private int mySelectedIndex = 0;

    // suppress Java warning
    private static final long serialVersionUID = 0L;
}
