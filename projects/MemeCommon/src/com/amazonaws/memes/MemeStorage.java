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

import java.util.List;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;


/**
 * Helper class to manage meme creation jobs
 */
public class MemeStorage {

    private final AmazonSQS sqs;
    private final DynamoDBMapper dynamoDBMapper;
    
    /**
     * Singleton instance to conserve resources
     */
    private static MemeStorage _instance;
    
    public static synchronized MemeStorage getInstance() {
    	if (_instance == null)
    		_instance = new MemeStorage();
    	return _instance;
    }
    
    public MemeStorage() {
        ClasspathPropertiesFileCredentialsProvider provider = new ClasspathPropertiesFileCredentialsProvider();
        sqs = new AmazonSQSClient(provider);
        sqs.setEndpoint(AWSResources.SQS_ENDPOINT);
        AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(provider);
        dynamoDB.setEndpoint(AWSResources.DYNAMO_ENDPOINT);
		dynamoDBMapper = new DynamoDBMapper(dynamoDB);
    }

    public void submitJob(MemeCreationJob job) {
        dynamoDBMapper.save(job);
        String queueUrl = sqs.getQueueUrl(new GetQueueUrlRequest().withQueueName(AWSResources.SQS_QUEUE)).getQueueUrl();
        sqs.sendMessage(new SendMessageRequest().withMessageBody(job.getId()).withQueueUrl(queueUrl));
    }
    
    public MemeCreationJob loadJob(String id) {
        return dynamoDBMapper.load(MemeCreationJob.class, id);    
    }
    
    public void saveJob(MemeCreationJob job) {
    	dynamoDBMapper.save(job);
    }
    
    public List<MemeCreationJob> getFinishedJobs() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition("status", new Condition().withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(MemeCreationJob.DONE_STATUS)));
        return dynamoDBMapper.scan(MemeCreationJob.class, scanExpression);
    }
    
}
