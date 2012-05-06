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
import com.amazonaws.services.dynamodb.model.DeleteTableRequest;
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
     * 
     * @param primaryKeyName
     * @param primaryKeyType
     */
    public void createTable(final String primaryKeyName,
                            final String primaryKeyType) {
        final CreateTableRequest createTableRequest =
          new CreateTableRequest().withTableName(myTableName)
            .withKeySchema(new KeySchema(new KeySchemaElement()
              .withAttributeName(primaryKeyName).withAttributeType(primaryKeyType)))
            .withProvisionedThroughput(new ProvisionedThroughput()
              .withReadCapacityUnits(ourReadCapacity)
              .withWriteCapacityUnits(ourWriteCapacity));

        final TableDescription createdTableDescription =
                myDynamoDB.createTable(createTableRequest).getTableDescription();
        System.out.println("Created Table: " + createdTableDescription);

        // we have to wait for the table to be usable
        waitWhileTableIs(TableStatus.CREATING.toString());
    }


    /**
     * 
     */
    public void deleteTable() {
        final DeleteTableRequest deleteTableRequest =
          new DeleteTableRequest().withTableName(myTableName);

        final TableDescription deletedTableDescription =
                myDynamoDB.deleteTable(deleteTableRequest).getTableDescription();
        System.out.println("\nDeleted Table: " + deletedTableDescription);

        // we have to wait for the table to be usable
        waitWhileTableIs(TableStatus.DELETING.toString());
    }


    /**
     * 
     */
    public void describeTable() {
        final DescribeTableRequest describeTableRequest =
          new DescribeTableRequest().withTableName(myTableName);
        final TableDescription tableDescription =
          myDynamoDB.describeTable(describeTableRequest).getTable();

        System.out.println("Table Description: " + tableDescription);
    }


    /**
     * 
     * @return
     */
    public String getTableName() {
        return myTableName;
    }


    /////////////////////////
    // PROTECTED INTERFACE //
    /////////////////////////


    /**
     * 
     * @return
     */
    protected AmazonDynamoDBClient getDynamoDBClient() {
        return myDynamoDB;
    }


    /**
     * 
     * @param operator
     * @param attribute
     * @param valueObj
     * @return
     */
    protected List<Map<String,AttributeValue>> getRawScanResult(final String operator,
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
            System.err.println("Unable to complete scan request for attribute:  " +
                               attribute + "  value:  " + valueObj.toString());
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

        // if no items matched the filter return null
        if(0 == scanResult.getCount()) {
            return null;
        }

        return scanResult.getItems();
    }


    ///////////////////////
    // PRIVATE INTERFACE //
    ///////////////////////


    /**
     * 
     * @throws IOException
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
     * 
     * @param state
     */
    private void waitWhileTableIs(final String state) {
        System.out.println("\nWaiting while \"" + myTableName + "\" is in state " + state + "...");

        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {Thread.sleep(1000 * 2);} catch (Exception e) {}
            try {
                final DescribeTableRequest request =
                  new DescribeTableRequest().withTableName(myTableName);

                final TableDescription tableDescription =
                  myDynamoDB.describeTable(request).getTable();

                final String tableStatus = tableDescription.getTableStatus();
                System.out.println("  - current state: " + tableStatus);

                if (!tableStatus.equals(state)) return;
            } catch (final AmazonServiceException ase) {
                if (ase.getErrorCode().equals("ResourceNotFoundException")) {
                    //:MAINTENANCE
                    //  If we catch this Exception then the table we are querying
                    //  no longer exists.  This is what we want when we are waiting
                    //  for a table in the DELETING state
                    return;
                }
                else {
                    throw ase;
                }
            }
        }
        throw new RuntimeException("\nTable " + myTableName + " never got out of state " + state);
    }


    /////////////////////
    // PRIVATE MEMBERS //
    /////////////////////


    // the table name we are managing
    private String myTableName;

    // DynamoDB handle
    private AmazonDynamoDBClient myDynamoDB;

    // free read capacity
    private static final Long ourReadCapacity = 10L;

    // free write capacity
    private static final Long ourWriteCapacity = 5L;
}
