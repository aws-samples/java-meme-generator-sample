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
import static com.amazonaws.memes.AWSResources.SQS;
import static com.amazonaws.memes.AWSResources.SQS_QUEUE_NAME;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.imageio.ImageIO;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

/**
 * The meme worker's job is to take work from Amazon SQS, look up the details in
 * Dynamo, then store the results back into Amazon S3.
 */
public class MemeWorker extends Thread {

    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(10);

    public static void main(String[] args) throws InterruptedException {
        new MemeWorker().start();
    }

    @Override
    public void run() {
        System.out.println("MemeWorker listening for work");
        String queueUrl = SQS.getQueueUrl(new GetQueueUrlRequest(SQS_QUEUE_NAME)).getQueueUrl();

        while (true) {
            try {
                ReceiveMessageResult result = SQS.receiveMessage(
                        new ReceiveMessageRequest(queueUrl).withMaxNumberOfMessages(1));
                for (Message msg : result.getMessages()) {
                    executorService.submit(new MessageProcessor(queueUrl, msg));
                }
                sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new RuntimeException("Worker interrupted");
            } catch (Exception e) {
                // ignore and retry
            }
        }
    }

    private final class MessageProcessor implements Runnable {
        private final String queueUrl;
        private final Message msg;

        private MessageProcessor(String queueUrl, Message msg) {
            this.queueUrl = queueUrl;
            this.msg = msg;
        }

        @Override
        public void run() {
            String id = msg.getBody();
            final ImageMacro imageMacro = DYNAMODB_MAPPER.load(ImageMacro.class, id);

            try {
                if (imageMacro != null) {
                    imageMacro.setStatus(ImageMacro.WORKING_STATUS);
                    DYNAMODB_MAPPER.save(imageMacro);

                    // Process the image
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    imageMacro.getStartingImageLink().downloadTo(output);
                    BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(output.toByteArray()));
                    BufferedImage finishedImage = overlayImage(imageMacro, sourceImage);

                    // Push the new image to S3
                    imageMacro.getFinishedImageLink().uploadFrom(readImageIntoBuffer(finishedImage));
                    imageMacro.getFinishedImageLink().setAcl(CannedAccessControlList.PublicRead);

                    imageMacro.setStatus(ImageMacro.DONE_STATUS);
                    imageMacro.setUpdateTime(new Date());
                    DYNAMODB_MAPPER.save(imageMacro);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    // Assume this job didn't work as expected
                    imageMacro.setStatus(ImageMacro.FAILED_STATUS);
                    DYNAMODB_MAPPER.save(imageMacro);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            SQS.deleteMessage(new DeleteMessageRequest(queueUrl, msg.getReceiptHandle()));
        }
    }

    /**
     * Overlays the specified image with the captions from the MemeCreationJob
     * and returns a new image. If the processing takes too long, this method
     * will exit with an InterruptedException.
     */
    private BufferedImage overlayImage(ImageMacro imageMacro, BufferedImage image)
            throws IOException, InterruptedException {

        // A timer to interrupt us after a timeout
        final Thread thread = Thread.currentThread();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                thread.interrupt();
            }
        }, 10000);

        return ImageOverlay.overlay(
                image, imageMacro.getTopCaption(), imageMacro.getBottomCaption());
    }

    private byte[] readImageIntoBuffer(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } finally {
            baos.close();
        }
    }
}
