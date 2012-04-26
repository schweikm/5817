import java.io.IOException;

import java.util.ArrayList;
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
        final Map<String, AttributeValue> item = newItem(data.name,
                                                         data.year,
                                                         data.rating,
                                                         data.fan);

        final PutItemRequest putItemRequest = new PutItemRequest(getTableName(), item);
        final PutItemResult putItemResult = getDynamoDBClient().putItem(putItemRequest);
        System.out.println("\nItem: " + putItemResult);
    }


    /**
     * Scan items for movies
     * @param tableName
     * @param operator
     * @param year
     */
    public List<MediaTableData> scanTable(final String operator,
                                          final String attribute,
                                          final Object valueObj) {

        // to be generic, let's find the type of the attribute
        AttributeValue value = null;
        if(valueObj instanceof String) {
            value = new AttributeValue().withS(valueObj.toString());
        }
        else if(valueObj instanceof Integer) {
            int val = ((Number)valueObj).intValue();
            value = new AttributeValue().withN(new Integer(val).toString());
        }
        else {
            System.err.println("Unable to complete scan request for  attribute:  " +
                               attribute + "  value" + valueObj.toString());
            return null;
        }

        // then do the scan request
        final HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
        final Condition condition = new Condition()
            .withComparisonOperator(operator)
            .withAttributeValueList(value);
        scanFilter.put(attribute, condition);
        final ScanRequest scanRequest = new ScanRequest(getTableName()).withScanFilter(scanFilter);
        final ScanResult scanResult = getDynamoDBClient().scan(scanRequest);

        // parse the result and transform it to a List of MediaTableData
        final List<Map<String,AttributeValue>> itemList = scanResult.getItems();

        // if no items matched the filter return null
        if(0 == scanResult.getCount()) {
            return null;
        }

        // otherwise create a list and return it
        final List<MediaTableData> returnList = new ArrayList<MediaTableData>(); 
        for(int i = 0; i < itemList.size(); i++) {
            final String resultName   = itemList.get(i).get(nameAttribute_PK).getS();
            final int    resultYear   = Integer.parseInt(itemList.get(i).get(yearAttribute).getN());
            final String resultRating = itemList.get(i).get(ratingAttribute).getS();
            final String resultFan    = itemList.get(i).get(fanAttribute).getS();

            returnList.add(new MediaTableData(resultName,
                                              resultYear,
                                              resultRating,
                                              resultFan));
        }

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
     * @param name
     * @param year
     * @param rating
     * @param fans
     * @return
     */
    private final Map<String, AttributeValue> newItem(final String name,
                                                      final int    year,
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
    public static final String nameAttribute_PK = "name";
    public static final String yearAttribute    = "year";
    public static final String ratingAttribute  = "rating";
    public static final String fanAttribute     = "fan";
}
