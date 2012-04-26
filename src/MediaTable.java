import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutItemResult;
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;


/**
 * 
 * @author schweikm
 *
 */
public class MediaTable extends DynamoTable {


    //////////////////////
    // PUBLIC INTERFACE //
    //////////////////////


    /**
     * 
     * @param tableName
     */
    public MediaTable(final String tableName) throws IOException {
        super(tableName);
    }


    public void addItemToTable(final MediaTableData data) {
        final Map<String, AttributeValue> item = newItem(movieName, year, rating, fan);
        final PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
        final PutItemResult putItemResult = myDynamoDB.putItem(putItemRequest);
        System.out.println("\nItem: " + putItemResult);
    }


    /**
     * Scan items for movies
     * @param tableName
     * @param operator
     * @param year
     */
    public void scanTableYear(final String operator,
                              final int year) {

        final HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
        final Condition condition = new Condition()
            .withComparisonOperator(operator)
            .withAttributeValueList(new AttributeValue().withN(new Integer(year).toString()));
        scanFilter.put(yearAttribute, condition);
        final ScanRequest scanRequest = new ScanRequest(getTableName()).withScanFilter(scanFilter);
        final ScanResult scanResult = getDynamoDBClient().scan(scanRequest);

        final List<Map<String,AttributeValue>> itemList = scanResult.getItems();
        for(int i = 0; i < itemList.size(); i++) {
            final String resultName   = itemList.get(i).get(nameAttribute_PK).getS();
            final int    resultYear   = Integer.parseInt(itemList.get(i).get(yearAttribute).getN());
            final String resultRating = itemList.get(i).get(ratingAttribute).getS();
            final String resultFan    = itemList.get(i).get(fanAttribute).getS();

            System.out.println("\n----------------------------------------\n" +
                               "    Name  :  " + resultName   + "\n" +
                               "    Year  :  " + resultYear   + "\n" +
                               "    Rating:  " + resultRating + "\n" +
                               "    Fan   :  " + resultFan    + "\n" +
                               "----------------------------------------\n");
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
     * @param name
     * @param year
     * @param rating
     * @param fans
     * @return
     */
    private final Map<String, AttributeValue> newItem(final String name,
                                                      final int year,
                                                      final String rating,
                                                      final String fan) {

        final Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put(nameAttribute_PK, new AttributeValue(name));
        item.put(yearAttribute,    new AttributeValue().withN(Integer.toString(year)));
        item.put(ratingAttribute,  new AttributeValue(rating));
        item.put(fanAttribute,     new AttributeValue(fan));

        return item;
    }


    /////////////////////
    // PRIVATE MEMBERS //
    /////////////////////


    // table attributes
    private static final String nameAttribute_PK = "name";
    private static final String yearAttribute = "year";
    private static final String ratingAttribute = "rating";
    private static final String fanAttribute = "fan";
}
