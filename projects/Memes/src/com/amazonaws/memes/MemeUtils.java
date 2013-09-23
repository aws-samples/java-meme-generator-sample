/*
 * Copyright 2012-2013 Amazon Technologies, Inc.
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

import static com.amazonaws.memes.AWSResources.DYNAMODB_MAPPER;
import static com.amazonaws.memes.AWSResources.FINISHED_PATH;
import static com.amazonaws.memes.AWSResources.S3_BUCKET_NAME;
import static com.amazonaws.memes.AWSResources.SQS;
import static com.amazonaws.memes.AWSResources.SQS_QUEUE_NAME;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * Helper class to manage meme creation jobs
 */
public class MemeUtils {

    public ImageMacro submitJob(String topCaption, String bottomCaption, String imageKey, String createdBy) {
        ImageMacro macro = new ImageMacro();
        macro.setTopCaption(topCaption);
        macro.setBottomCaption(bottomCaption);
        macro.setStartingImageLink(DYNAMODB_MAPPER.createS3Link(AWSResources.S3_BUCKET_NAME, imageKey));
        macro.setFinishedImageLink(DYNAMODB_MAPPER.createS3Link(S3_BUCKET_NAME, FINISHED_PATH + UUID.randomUUID() + ".png"));
        macro.setCreatedBy(createdBy);
        macro.setCreationTime(new Date());

        DYNAMODB_MAPPER.save(macro);

        /*
         * Use Amazon SQS to send a message to the queue our worker processes
         * are monitoring.
         *
         * Another option in the SDK for sending messages to a queue is the
         * AmazonSQSBufferedAsyncClient. This client will buffer messages
         * locally so that they are batched together in groups when they're
         * sent. This means more efficient network communication with SQS
         * because less individual requests with single messages are being sent.
         * For high throughput applications this can not only help with
         * throughput, but can also decrease your SQS costs because of the
         * reduction in the amount of API calls. The tradeoff is that individual
         * messages can be slightly delayed while a full batch is created on the
         * client-side.
         */
        String queueUrl = SQS.getQueueUrl(new GetQueueUrlRequest(SQS_QUEUE_NAME)).getQueueUrl();
        SQS.sendMessage(new SendMessageRequest(queueUrl, macro.getId()));

        return macro;
    }

    /** Returns a list of the completed image macros. */
    public List<ImageMacro> getFinishedJobs() {
        /*
         * DynamoDBMapper allows you to run scans and queries against the
         * data in your table, and interpret the results as your domain objects.
         *
         * In addition to unmarshalling DynamoDB results into your domain
         * objects, DynamoDBMapper also takes care of result set pagination
         * for you. You don't have to drop down to the low level client
         * to manually manage pagination tokens.
         */
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.addFilterCondition("status", new Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue(ImageMacro.DONE_STATUS)));
        return DYNAMODB_MAPPER.scan(ImageMacro.class, scanExpression);
    }
}
