import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;

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


/**
 * 
 * @author marc
 *
 */
public class MediaPanel extends JPanel implements ActionListener {


    //////////////////////
    // PUBLIC INTERFACE //
    //////////////////////


    /**
     * 
     * @return
     */
    public static MediaPanel getInstance() {
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
            // Previous Button
            if(event.getActionCommand().equals(PREVIOUS_BUTTON)) {
                if(mySelectedIndex > 0) {
                    updateDisplay(--mySelectedIndex);
                }
            }

            // Next Button
            else if(event.getActionCommand().equals(NEXT_BUTTON)) {
                if(mySelectedIndex < (myItems.size() - 1)) {
                    updateDisplay(++mySelectedIndex);
                }
            }

            // I don't know!
            else {
                System.err.println("Unknown action received:  " + event.getActionCommand());
            }
        }
        catch(final IOException ioex) {
            System.err.println(ioex.getMessage());
            ioex.printStackTrace();
        }
    }


    /**
     * 
     * @throws IOException
     */
    public void refreshDisplay() throws IOException {
        updateDisplay(0);
    }


    /**
     * 
     */
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

        mySelectedIndex = 0;
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


    /**
     * 
     * @param index
     * @throws IOException
     */
    private void updateDisplay(final int index) throws IOException {
        if(index >= myItems.size()) {
//            System.err.println("MediaPanel::updateDisplay() - Invalid index specified!  " +
//                               "Index:  " + index + "  Size:  " + myItems.size());
            return;
        }

        myTitleTextField.setText(myItems.get(index).title);
        myMpaaRatingTextField.setText(myItems.get(index).mpaaRating);
        myYearTextField.setText(((Integer)myItems.get(index).year).toString());
        myRuntimeTextField.setText(((Integer)myItems.get(index).runtime).toString());
        myDirectorTextField.setText(myItems.get(index).director);

        // reconstruct the double from the Base64 encoded string
        final Double imdbRating = decodeDouble(myItems.get(index).base64ImdbRating);
        myImdbRatingTextField.setText(imdbRating.toString());

        // reconstruct the image from the Base64 encoded string
        final Image poster = decodeImage(myItems.get(index).base64Image);
        myImageLabel.setText("");
        myImageLabel.setIcon(new ImageIcon(poster));
    }


    /**
     * 
     * @param strVal
     * @return
     */
    private Double decodeDouble(final String encodedString) {
        final byte[] imdbBytes = Base64.decodeBase64(encodedString);
        final String imdbStr = new String(imdbBytes);
        return Double.parseDouble(imdbStr);
    }


    private Image decodeImage(final String encodedString) 
      throws IOException {
        final byte[] imageBytes = Base64.decodeBase64(encodedString);
        final InputStream in = new ByteArrayInputStream(imageBytes);
        final BufferedImage bImageFromConvert = ImageIO.read(in);
        return bImageFromConvert;
    }


    /////////////////////
    // PRIVATE MEMBERS //
    /////////////////////


    // Singleton instance
    private static final MediaPanel theInstance = new MediaPanel();

    // Swing components
    private final JTextField myTitleTextField      = new JTextField();
    private final JTextField myMpaaRatingTextField = new JTextField();
    private final JTextField myYearTextField       = new JTextField();
    private final JTextField myRuntimeTextField    = new JTextField();
    private final JTextField myDirectorTextField   = new JTextField();
    private final JTextField myImdbRatingTextField = new JTextField();
    private final JLabel     myImageLabel          = new JLabel();

    // action commands
    private static final String PREVIOUS_BUTTON = "previous_button";
    private static final String NEXT_BUTTON     = "next_button";

    // list of items to display
    private final List<MediaTableData> myItems = new ArrayList<MediaTableData>();
    private int mySelectedIndex = 0;

    // suppress Java warning
    private static final long serialVersionUID = 0L;
}
