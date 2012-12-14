/*
 * Copyright 2012 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amazonaws.memes;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.dynamodb.AmazonDynamoDB;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.CreateTableRequest;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.KeySchema;
import com.amazonaws.services.dynamodb.model.KeySchemaElement;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodb.model.ScalarAttributeType;
import com.amazonaws.services.dynamodb.model.TableDescription;
import com.amazonaws.services.dynamodb.model.TableStatus;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;

/**
 * Configuration for AWS resources. Run to set up your resources.
 * 
 * @author zachmu
 */
public class AWSResources {

	public static final String BUCKET_NAME = "CHANGEME";
    public static final String MACRO_PATH = "macros/";
    public static final String FINISHED_PATH = "memes/";   
    public static final String SQS_QUEUE = "reinvent-memes";
    public static final String DYNAMO_TABLE_NAME = "reinvent-memes";
    
    public static final String SQS_ENDPOINT = "sqs.us-west-1.amazonaws.com";
	public static final String DYNAMO_ENDPOINT = "dynamodb.us-west-1.amazonaws.com";
    
    public static void main(String[] args) {
        createResources();
    }
    
    private static void createResources() {
        ClasspathPropertiesFileCredentialsProvider provider = new ClasspathPropertiesFileCredentialsProvider();
        
        AmazonSQS sqs = new AmazonSQSClient(provider);
        sqs.setEndpoint(SQS_ENDPOINT);        
        sqs.createQueue(new CreateQueueRequest().withQueueName(SQS_QUEUE));
        
        AmazonS3 s3 = new AmazonS3Client(provider);
        if ( !s3.doesBucketExist(BUCKET_NAME) ) {
            s3.createBucket(new CreateBucketRequest(BUCKET_NAME));
        }
        
        AmazonDynamoDBClient dynamo = new AmazonDynamoDBClient(provider);
        dynamo.setEndpoint(DYNAMO_ENDPOINT);

        if ( !doesTableExist(dynamo, DYNAMO_TABLE_NAME) ) {
            dynamo.createTable(new CreateTableRequest()
                    .withTableName(DYNAMO_TABLE_NAME)
                    .withProvisionedThroughput(
                            new ProvisionedThroughput().withReadCapacityUnits(50l).withWriteCapacityUnits(50l))
                    .withKeySchema(
                            new KeySchema().withHashKeyElement(new KeySchemaElement().withAttributeName("id")
                                    .withAttributeType(ScalarAttributeType.S))));
            waitForTableToBecomeAvailable(dynamo, DYNAMO_TABLE_NAME);
        }
    }

    private static boolean doesTableExist(AmazonDynamoDB dynamo, String tableName) {
        try {
            TableDescription table = dynamo.describeTable(new DescribeTableRequest().withTableName(tableName))
                    .getTable();
            return "ACTIVE".equals(table.getTableStatus());
        } catch ( AmazonServiceException ase ) {
            if ( ase.getErrorCode().equals("ResourceNotFoundException") )
                return false;
            throw ase;
        }
    }

    private static void waitForTableToBecomeAvailable(AmazonDynamoDB dynamo, String tableName) {
        System.out.println("Waiting for " + tableName + " to become ACTIVE...");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);
        while ( System.currentTimeMillis() < endTime ) {
            try {
                Thread.sleep(1000 * 20);
            } catch ( Exception e ) {
            }
            try {
                DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
                TableDescription table = dynamo.describeTable(request).getTable();
                if ( table == null )
                    continue;

                String tableStatus = table.getTableStatus();
                System.out.println("  - current state: " + tableStatus);
                if ( tableStatus.equals(TableStatus.ACTIVE.toString()) )
                    return;
            } catch ( AmazonServiceException ase ) {
                if ( ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false )
                    throw ase;
            }
        }

        throw new RuntimeException("Table " + tableName + " never went active");
    }
}
