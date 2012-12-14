<!-- 
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
 -->

<%@page import="java.util.Date"%>
<%@page import="java.io.Console"%>
<%@page import="com.amazonaws.memes.MemeStorage"%>
<%@page import="com.amazonaws.memes.MemeCreationJob"%>
<%@page import="com.amazonaws.memes.AWSResources"%>
<%@page import="com.amazonaws.memes.S3ImageStorage"%>
<%@page import="java.util.List"%>
<%@page import="com.amazonaws.auth.BasicAWSCredentials"%>
<%@page import="com.amazonaws.auth.AWSCredentials"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>


<%@ page
	import="com.amazonaws.services.s3.*,com.amazonaws.services.s3.model.*"%>
<html>
<head>
<title>AWS Meme Generator</title>
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet">
<style>
body {
	background-color: #222;
}

.hero-unit {
	padding: 30px;
	margin-bottom: 10px;
}

.create-meme {
	float: right;
	text-align: center;
	margin-top: 20px;
}

.memes img {
	padding: 10px;
}

.meme-option {
	float: left;
	padding-right: 20px;
}

.meme-option label {
	font-weight: bold;
	font-size: 1.5em;
}

.meme-option img {
	width: 160px;
}

.caption-fields {
	clear: left;
	padding-top: 10px;
}

#fff {
	font-weight: normal;
}
</style>
</head>
<body>

	<div class="container">
		<div class="hero-unit">
			<p class="create-meme">
				<a class="btn btn-primary btn-large" href="memes.jsp">See
					Finished Memes <i class="icon-chevron-right icon-white"></i>
				</a>
			</p>
			<h2>Thanks for submitting!</h2>
			<h2>Your meme will be created shortly.</h2>
			<%
				String imageKey = request.getParameter("imageKey");
				String topCaption = request.getParameter("topCaption");
				String bottomCaption = request.getParameter("bottomCaption");
				String createdBy = request.getParameter("createdBy");

				MemeCreationJob job = new MemeCreationJob();
				job.setImageKey(imageKey);
				job.setTopCaption(topCaption);
				job.setBottomCaption(bottomCaption);
				job.setCreationTime(new Date());
				job.setCreatedBy(createdBy);

				MemeStorage memeStorage = MemeStorage.getInstance();
				memeStorage.submitJob(job);

				out.print("<p>Processing...");
				out.flush();
				response.flushBuffer();
				while (!MemeCreationJob.DONE_STATUS.equals(job.getStatus())
						&& !MemeCreationJob.FAILED_STATUS.equals(job.getStatus())) {
					Thread.sleep(1000);
					out.print(".");
					out.flush();
					response.flushBuffer();
					job = memeStorage.loadJob(job.getId());
				}
				if (MemeCreationJob.DONE_STATUS.equals(job.getStatus())) {
					out.print(" All done!</p>");
				} else {
					out.print(" Something went wrong :(</p>");
				}
			%>

			<p>
				<img
					src="http://<%=AWSResources.BUCKET_NAME%>.s3.amazonaws.com/<%=job.getFinishedKey()%>">
			</p>

			<p>
				<a class="btn btn-primary btn-large" href="index.jsp"><i
					class="icon-edit icon-white"></i> Create another one!</a>
			</p>
		</div>
	</div>
</body>
</html>
