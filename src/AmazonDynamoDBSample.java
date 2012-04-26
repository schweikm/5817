/*
 * Copyright 2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;


/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
public class AmazonDynamoDBSample {


	
/*	
    try {
        final AWSCredentials credentials =
          new PropertiesCredentials(
            DynamoTable.class.getResourceAsStream("AwsCredentials.properties"));

         myDynamoDB = new AmazonDynamoDBClient(credentials);
    }
    catch(final IOException ioex) {
        System.err.println("Caught exception while creating DynamoDBClient!");
        System.err.println("Message:  " + ioex.getMessage());
        ioex.printStackTrace();
    }
*/	
	

    public static void main(String[] args) throws Exception {
        

        try {
            final String tableName = "my-favorite-movies-table";

            // create the table
//            DynamoDBInterface.getInstance().createTable(tableName);

            // describe the table
//            DynamoDBInterface.getInstance().describeTable(tableName);

            // add an item
/*            DynamoDBInterface.getInstance().addItem(tableName,
                                                    "Bill & Ted's Excellent Adventure",
                                                    1989,
                                                    "****",
                                                    "James");

            // add another item
            DynamoDBInterface.getInstance().addItem(tableName,
                                                    "Airplane",
                                                    1980,
                                                    "*****",
                                                    "Billy Bob");

            // scan the table
            DynamoDBInterface.getInstance().scanTableYear(tableName,
                                                          ComparisonOperator.GT.toString(),
                                                          1900);
*/
            System.out.println("Done!");
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
