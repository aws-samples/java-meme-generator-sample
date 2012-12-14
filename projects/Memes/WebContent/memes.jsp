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
 
<%@page import="com.amazonaws.memes.MemeCreationJob"%>
<%@page import="com.amazonaws.memes.MemeStorage"%>
<%@page import="com.amazonaws.memes.AWSResources"%>
<%@page import="com.amazonaws.memes.S3ImageStorage"%>
<%@page import="java.util.List"%>
<%@page import="com.amazonaws.auth.BasicAWSCredentials"%>
<%@page import="com.amazonaws.auth.AWSCredentials"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>


<%-- This is a JSP Comment before JSP Scriplet --%>
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
	margin: 10px;
	border: 3px solid white;
	border-radius: 3px;
}
</style>
</head>
<body>
	<div class="container">
		<div class="hero-unit">
			<p class="create-meme">
				<a class="btn btn-primary btn-large" href="index.jsp"><i
					class="icon-edit icon-white"></i> Create a New Meme</a>
			</p>
			<h1>AWS Meme Generator</h1>
			<h2>Please enjoy these wonderful finished memes</h2> 
		</div>
		<div class="memes">

			<%
			    MemeStorage jobStorage = MemeStorage.getInstance();
			    List<MemeCreationJob> jobs = jobStorage.getFinishedJobs();
			    for ( MemeCreationJob job : jobs ) {
			%>
			<div>
				<img
					src="http://<%=AWSResources.BUCKET_NAME%>.s3.amazonaws.com/<%=job.getFinishedKey()%>">
				<p style="textsize: larger; color: white">
					Posted by
					<%=job.getCreatedBy() != null ? job.getCreatedBy() : "Anonymous" %>
					on
					<%=job.getCreationTime()%>
			</div>
			<%
			    }
			%>
		</div>
	</div>
	<script src="http://code.jquery.com/jquery-latest.js"></script>
	<script src="bootstrap/js/bootstrap.min.js"></script>
</body>
</html>
