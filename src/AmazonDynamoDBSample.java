import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.amazonaws.services.dynamodb.model.ComparisonOperator;


/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
public class AmazonDynamoDBSample {


    public static void main(String[] args) {
        List<List<String>> data = loadData();
        printLoadedData(data);

//        MediaTableDemo();
    }

    
    public static void MediaTableDemo() {
        try {
            // create a table manager
            MediaTable table = new MediaTable("my-favorite-movies-table");

            // create the table
            table.createTable(MediaTable.nameAttribute_PK, "S");

            // describe the table
            table.describeTable();

            // add an item
            table.addItemToTable(new MediaTableData("Bill & Ted's Excellent Adventure",
                                                    1989,
                                                    "****",
                                                    "James"));

            // add another item
            table.addItemToTable(new MediaTableData("Airplane",
                                                    1980,
                                                    "*****",
                                                    "Billy Bob"));

            // scan the table
            printDataItem(table.scanTable(ComparisonOperator.EQ.toString(),
                                          MediaTable.fanAttribute,
                                          "James"));

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


    public static List<List<String>> loadData() {
        final List<List<String>> returnList = new ArrayList<List<String>>();

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
            
                final String title = reader.readLine();
                final String rating = reader.readLine();
                final String year = reader.readLine();
                final String runTimeMins = reader.readLine();
                final String director = reader.readLine();
                final String imdbRating = reader.readLine();

                final List<String> info = new ArrayList<String>();
                info.add(title.substring(8));
                info.add(rating.substring(9));
                info.add(year.substring(7));
                info.add(runTimeMins.substring(17));
                info.add(director.substring(11));
                info.add(imdbRating.substring(14));

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


    public static void printDataItem(final List<MediaTableData> items) {
        if(null == items) {
            System.out.println("No results returned!");
            return;
        }

        for(int i = 0; i < items.size(); i++) {
            final MediaTableData data = items.get(i);

            System.out.println("\nItem " + (i + 1) + " of " + items.size());
            System.out.println("----------------------------------------\n" +
                               "    Name  :  " + data.name   + "\n" +
                               "    Year  :  " + data.year   + "\n" +
                               "    Rating:  " + data.rating + "\n" +
                               "    Fan   :  " + data.fan    + "\n" +
                               "----------------------------------------\n");
        }
    }


    public static void printLoadedData(final List<List<String>> data) {
        if(null == data) {
            System.out.println("No data was loaded!");
            return;
        }

        for(int i = 0; i < data.size(); i++) {
            final List<String> datum = data.get(i);

            System.out.println("\nItem " + (i + 1) + " of " + data.size());
            System.out.println("----------------------------------------\n" +
                               "    Title        :  " + datum.get(0) + "\n" +
                               "    Rating       :  " + datum.get(1) + "\n" +
                               "    Year         :  " + datum.get(2) + "\n" +
                               "    Runtime (min):  " + datum.get(3) + "\n" +
                               "    Director     :  " + datum.get(4) + "\n" +
                               "    IMDB Rating  :  " + datum.get(5) + "\n" +
                               "----------------------------------------\n");
        }
    }
}
