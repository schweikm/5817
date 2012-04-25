import com.amazonaws.AmazonServiceException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;

import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.dynamodb.model.CreateTableRequest;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.KeySchema;
import com.amazonaws.services.dynamodb.model.KeySchemaElement;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutItemResult;
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;
import com.amazonaws.services.dynamodb.model.TableDescription;
import com.amazonaws.services.dynamodb.model.TableStatus;

import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author marc
 *
 */
public class DynamoDBInterface {


    //////////////////////
    // PUBLIC INTERFACE //
    //////////////////////


    /**
     * 
     * @return
     */
    public static DynamoDBInterface getInstance() throws IOException {
        if(null == theInstance) {
            theInstance = new DynamoDBInterface();
            theInstance.init();
        }
        return theInstance;
    }


    /**
     * Create a table with a primary key named 'name', which holds a string
     * @param tableName
     */
    public void createTable(final String tableName) {
        final CreateTableRequest createTableRequest =
          new CreateTableRequest().withTableName(tableName)
            .withKeySchema(new KeySchema(new KeySchemaElement()
              .withAttributeName(nameAttribute).withAttributeType("S")))
            .withProvisionedThroughput(new ProvisionedThroughput()
              .withReadCapacityUnits(myReadCapacity)
              .withWriteCapacityUnits(myWriteCapacity));

        final TableDescription createdTableDescription =
          myDynamoDB.createTable(createTableRequest).getTableDescription();

        System.out.println("\nCreated Table: " + createdTableDescription);

        // then wait for the table to become ACTIVE
        waitForTableToBecomeAvailable(tableName);
    }


    /**
     * Describe our new table
     * @param tableName
     */
    public void describeTable(final String tableName) {
        final DescribeTableRequest describeTableRequest =
          new DescribeTableRequest().withTableName(tableName);
        final TableDescription tableDescription =
          myDynamoDB.describeTable(describeTableRequest).getTable();

        System.out.println("\nTable Description: " + tableDescription);
    }


    /**
     * Add an item
     * @param tableName
     * @param movieName
     * @param year
     * @param rating
     * @param fans
     */
    public void addItem(final String tableName,
                        final String movieName,
                        final int year,
                        final String rating,
                        final String fan) {

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
    public void scanTableYear(final String tableName,
                              final String operator,
                              final int year) {

        final HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
        final Condition condition = new Condition()
            .withComparisonOperator(operator)
            .withAttributeValueList(new AttributeValue().withN(new Integer(year).toString()));
        scanFilter.put(yearAttribute, condition);
        final ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
        final ScanResult scanResult = myDynamoDB.scan(scanRequest);

        final List<Map<String,AttributeValue>> itemList = scanResult.getItems();
        for(int i = 0; i < itemList.size(); i++) {
            final String resultName   = itemList.get(i).get(nameAttribute).getS();
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


    // Singleton pattern - private construct
    private DynamoDBInterface() { }


    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.PropertiesCredentials
     * @see com.amazonaws.ClientConfiguration
     */
    private void init() throws IOException {
        final AWSCredentials credentials =
          new PropertiesCredentials(
            AmazonDynamoDBSample.class.getResourceAsStream("AwsCredentials.properties"));

        myDynamoDB = new AmazonDynamoDBClient(credentials);
    }


    /**
     * Creating a table takes a long time
     * @param tableName
     */
    private void waitForTableToBecomeAvailable(final String tableName) {
        System.out.println("\nWaiting for " + tableName + " to become ACTIVE...");

        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {Thread.sleep(1000 * 20);} catch (Exception e) {}
            try {
                final DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
                final TableDescription tableDescription = myDynamoDB.describeTable(request).getTable();
                final String tableStatus = tableDescription.getTableStatus();
                System.out.println("  - current state: " + tableStatus);
                if (tableStatus.equals(TableStatus.ACTIVE.toString())) return;
            } catch (final AmazonServiceException ase) {
                if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false) throw ase;
            }
        }
        throw new RuntimeException("\nTable " + tableName + " never went active");
    }


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
        item.put(nameAttribute,   new AttributeValue(name));
        item.put(yearAttribute,   new AttributeValue().withN(Integer.toString(year)));
        item.put(ratingAttribute, new AttributeValue(rating));
        item.put(fanAttribute,    new AttributeValue(fan));

        return item;
    }


    /////////////////////
    // PRIVATE MEMBERS //
    /////////////////////


    // Singleton instance
    private static DynamoDBInterface theInstance = null;

    // DynamoDB handle
    private static AmazonDynamoDBClient myDynamoDB;

    // free read capacity
    private static final Long myReadCapacity = 10L;

    // free write capacity
    private static final Long myWriteCapacity = 5L;

    // table attributes
    private static final String nameAttribute = "name";
    private static final String yearAttribute = "year";
    private static final String ratingAttribute = "rating";
    private static final String fanAttribute = "fan";
}
