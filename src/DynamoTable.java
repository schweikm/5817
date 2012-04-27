import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;
import com.amazonaws.services.dynamodb.model.TableDescription;
import com.amazonaws.services.dynamodb.model.TableStatus;


/**
 * 
 * @author schweikm
 *
 */
public abstract class DynamoTable {


    //////////////////////
    // PUBLIC INTERFACE //
    //////////////////////


    /**
     * 
     * @param tableName
     * @throws IOException
     */
    DynamoTable(final String tableName) throws IOException {
        myTableName = tableName;
        init();
    }


    /**
     * Create a table
     * 
     * @param tableName
     */
    public void createTable(final String primaryKeyName,
                            final String primaryKeyType) {
        final CreateTableRequest createTableRequest =
          new CreateTableRequest().withTableName(myTableName)
            .withKeySchema(new KeySchema(new KeySchemaElement()
              .withAttributeName(primaryKeyName).withAttributeType(primaryKeyType)))
            .withProvisionedThroughput(new ProvisionedThroughput()
              .withReadCapacityUnits(myReadCapacity)
              .withWriteCapacityUnits(myWriteCapacity));

        final TableDescription createdTableDescription =
          myDynamoDB.createTable(createTableRequest).getTableDescription();

        System.out.println("\nCreated Table: " + createdTableDescription);

        // we have to wait for the table to be usable
        waitForTableToBecomeAvailable();
    }


    /**
     * Describe our new table
     * 
     * @param tableName
     */
    public void describeTable() {
        final DescribeTableRequest describeTableRequest =
          new DescribeTableRequest().withTableName(myTableName);
        final TableDescription tableDescription =
          myDynamoDB.describeTable(describeTableRequest).getTable();

        System.out.println("\nTable Description: " + tableDescription);
    }


    /**
     * 
     * @return
     */
    public final String getTableName() {
        return myTableName;
    }


    /////////////////////////
    // PROTECTED INTERFACE //
    /////////////////////////


    /**
     * 
     * @return
     */
    protected final AmazonDynamoDBClient getDynamoDBClient() {
        return myDynamoDB;
    }


    /**
     * 
     * @param operator
     * @param attribute
     * @param valueObj
     * @return
     */
    protected final List<Map<String,AttributeValue>>
      getRawScanResult(final String operator,
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

        return itemList;
    }


    ///////////////////////
    // PRIVATE INTERFACE //
    ///////////////////////


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
        // only need to run init once!
        if(null != myDynamoDB) {
            return;
        }

        final AWSCredentials credentials =
          new PropertiesCredentials(
            DynamoTable.class.getResourceAsStream("AwsCredentials.properties"));

        myDynamoDB = new AmazonDynamoDBClient(credentials);
    }


    /**
     * Creating a table takes a long time
     * 
     * @param tableName
     */
    private void waitForTableToBecomeAvailable() {
        System.out.println("\nWaiting for " + myTableName + " to become ACTIVE...");

        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {Thread.sleep(1000 * 20);} catch (Exception e) {}
            try {
                final DescribeTableRequest request =
                  new DescribeTableRequest().withTableName(myTableName);
                final TableDescription tableDescription =
                  myDynamoDB.describeTable(request).getTable();
                final String tableStatus = tableDescription.getTableStatus();
                System.out.println("  - current state: " + tableStatus);
                if (tableStatus.equals(TableStatus.ACTIVE.toString())) return;
            } catch (final AmazonServiceException ase) {
                if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false) {
                    throw ase;
                }
            }
        }
        throw new RuntimeException("\nTable " + myTableName + " never went active");
    }


    /////////////////////
    // PRIVATE MEMBERS //
    /////////////////////


    // the table name we are managing
    private String myTableName;

    // DynamoDB handle
    private static AmazonDynamoDBClient myDynamoDB;

    // free read capacity
    private static final Long myReadCapacity = 10L;

    // free write capacity
    private static final Long myWriteCapacity = 5L;
}
