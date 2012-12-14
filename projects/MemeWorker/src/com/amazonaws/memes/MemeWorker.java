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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

/**
 * The meme worker's job is to take work from SQS, look up the details in
 * Dynamo, then store the results back into S3.
 * 
 * @author zachmu
 */
public class MemeWorker extends Thread {

	private final S3ImageStorage s3ImageStore;
	private final AmazonSQS sqs;
	private final MemeStorage memeStorage;
	private final ScheduledExecutorService executorService;
	
	public MemeWorker() {
		ClasspathPropertiesFileCredentialsProvider provider = new ClasspathPropertiesFileCredentialsProvider();
		s3ImageStore = new S3ImageStorage();
		sqs = new AmazonSQSClient(provider);
		sqs.setEndpoint(AWSResources.SQS_ENDPOINT);
		memeStorage = new MemeStorage();
		executorService = new ScheduledThreadPoolExecutor(10);
	}

	public static void main(String[] args) {
		MemeWorker memeWorker = new MemeWorker();
		memeWorker.start();
		try {
			memeWorker.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String queueUrl = sqs.getQueueUrl(
				new GetQueueUrlRequest().withQueueName(AWSResources.SQS_QUEUE))
				.getQueueUrl();

		while (true) {
			try {
				ReceiveMessageResult receiveMessage = sqs
						.receiveMessage(new ReceiveMessageRequest()
								.withMaxNumberOfMessages(1).withQueueUrl(
										queueUrl));
				for (Message msg : receiveMessage.getMessages()) {
					processMessage(msg, queueUrl);
				}
				sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			} catch (Exception e) {
				// ignore and retry
			}
		}
	}

	/**
	 * Processes the message given
	 */
	private void processMessage(final Message msg, final String queueUrl)
			throws IOException {
		
		Runnable processRunnable = new Runnable() {
			
			@Override
			public void run() {
				String id = msg.getBody();
				MemeCreationJob job = memeStorage.loadJob(id);

				try {
					if (job != null) {
						job.setStatus(MemeCreationJob.WORKING_STATUS);
						memeStorage.saveJob(job);

						BufferedImage image = s3ImageStore.loadBlankImage(job
								.getImageKey());
						BufferedImage overlay = overlayImage(job, image);
						String finishedImageKey = s3ImageStore.storeFinishedImage(
								overlay, job.getId());
						job.setFinishedKey(finishedImageKey);
						job.setStatus(MemeCreationJob.DONE_STATUS);
						job.setUpdateTime(new Date());
						memeStorage.saveJob(job);
					}
				} catch (Exception e) {
					e.printStackTrace();
					// Assume this job didn't work as expected
					try {
						job.setStatus(MemeCreationJob.FAILED_STATUS);
						memeStorage.saveJob(job);
					} catch (Exception e2) {
						// oh well.
					}
				}

				sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(queueUrl)
						.withReceiptHandle(msg.getReceiptHandle()));
			}
		};

		executorService.schedule(processRunnable, 0, TimeUnit.SECONDS);
	}

	/**
	 * Performs the buffered image overlay process with a timeout
	 */
	private BufferedImage overlayImage(MemeCreationJob job, BufferedImage image)
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

		BufferedImage overlay = ImageOverlay.overlay(image,
				job.getTopCaption(), job.getBottomCaption());
		return overlay;
	}
}
