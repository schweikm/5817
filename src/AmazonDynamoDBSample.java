import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.amazonaws.services.dynamodb.model.ComparisonOperator;


/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
public class AmazonDynamoDBSample {


    public static void MediaTableDemo() {
        try {
            // create a table manager
            MediaTable table = new MediaTable("media");

            // create the table
            table.createTable(MediaTable.titleAttribute_PK, "S");

            // describe the table
            table.describeTable();

            // add the items read in from disk
            for(final MediaTableData data : loadData()) {
                table.addItemToTable(data);
            }

            // scan the table
            printDataItem(table.scanTable(ComparisonOperator.NE.toString(),
                                          MediaTable.imdbRatingAttribute,
                                          0));

            System.out.println("Done!");
        } catch(final AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch(final AmazonClientException ace) {
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


    /**
     * 
     * @return
     */
    public static List<MediaTableData> loadData() {
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

        return returnList;
    }


    public static final String encodeImage(final String path)
      throws FileNotFoundException, IOException {
        // The encodeBase64 method take a byte[] as the parameter. The byte[] 
        // can be from a simple string like in this example or it can be from
        // an image file data.
        final File image = new File(path);
        byte[] encoded = Base64.encodeBase64(IOUtils.toByteArray(new FileInputStream(image)));
        return new String(encoded);
    }


    public static void printDataItem(final List<MediaTableData> data) {
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
}
