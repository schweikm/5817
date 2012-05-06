import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutItemResult;


/**
 * 
 * @author schweikm
 *
 */
public class MediaTable extends DynamoTable {


    // table attributes
    public static final String titleAttribute_PK    = "title";
    public static final String mpaaRatingAttribute  = "mpaaRating";
    public static final String yearAttribute        = "year";
    public static final String runtimeAttribute     = "runtime";
    public static final String directorAttribute    = "director";
    public static final String imdbRatingAttribute  = "imdbRating";
    public static final String base64ImageAttribute = "base64Image";


    //////////////////////
    // PUBLIC INTERFACE //
    //////////////////////


    /**
     * 
     * @return
     * @throws IOException
     */
    public static MediaTable getInstance() throws IOException {
        if(null == theInstance) {
            theInstance = new MediaTable(ourTableName);
        }

        return theInstance;
    }


    /**
     * 
     * @param data
     */
    public void addItemToTable(final MediaTableData data) {
        final Map<String, AttributeValue> item = newItem(data);
        final PutItemRequest putItemRequest = new PutItemRequest(getTableName(), item);

        final PutItemResult putItemResult = getDynamoDBClient().putItem(putItemRequest);
        System.out.println("Successfully added item - " + putItemResult);
    }


    /**
     * 
     * @param operator
     * @param attribute
     * @param valueObj
     * @return
     */
    public List<MediaTableData> scanTable(final String attribute,
                                          final String operator,
                                          final Object valueObj) {

        final List<Map<String,AttributeValue>> itemList =
          getRawScanResult(operator, attribute, valueObj);

        if(null == itemList) {
            return null;
        }

        final List<MediaTableData> returnList =
          new ArrayList<MediaTableData>();

        for(int i = 0; i < itemList.size(); i++) {
            final String title       = itemList.get(i).get(titleAttribute_PK).getS();
            final String mpaaRating  = itemList.get(i).get(mpaaRatingAttribute).getS();
            final int    year        = Integer.parseInt(itemList.get(i).get(yearAttribute).getN());
            final int    runtime     = Integer.parseInt(itemList.get(i).get(runtimeAttribute).getN());
            final String director    = itemList.get(i).get(directorAttribute).getS();
            final int    imdbRating  = Integer.parseInt(itemList.get(i).get(imdbRatingAttribute).getN());
            final String base64Image = itemList.get(i).get(base64ImageAttribute).getS();

            returnList.add(new MediaTableData(title, mpaaRating, year, runtime,
                                              director, imdbRating, base64Image));
        }

        Collections.sort(returnList);
        System.out.println("Successfully retreived " + returnList.size() +
                           " items from \"" + getTableName() + "\"");
        return returnList;
    }


    /////////////////////////
    // PROTECTED INTERFACE //
    /////////////////////////


    ///////////////////////
    // PRIVATE INTERFACE //
    ///////////////////////


    /**
     * 
     * @param tableName
     * @throws IOException
     */
    private MediaTable(final String tableName) throws IOException {
        super(tableName);
    }


    /**
     * 
     * @param data
     * @return
     */
    private Map<String, AttributeValue> newItem(final MediaTableData data) {
        final Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();

        item.put(titleAttribute_PK,    new AttributeValue(data.title));
        item.put(mpaaRatingAttribute,  new AttributeValue(data.mpaaRating));
        item.put(yearAttribute,        new AttributeValue().withN(Integer.toString(data.year)));
        item.put(runtimeAttribute,     new AttributeValue().withN(Integer.toString(data.runtime)));
        item.put(directorAttribute,    new AttributeValue(data.director));
        item.put(imdbRatingAttribute,  new AttributeValue().withN(Integer.toString(data.imdbRating)));
        item.put(base64ImageAttribute, new AttributeValue(data.base64Image));

        return item;
    }


    /////////////////////
    // PRIVATE MEMBERS //
    /////////////////////


    // Singleton instance
    private static MediaTable theInstance = null;

    // table name
    private static final String ourTableName = "media";
}
