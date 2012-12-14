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
			<h1>AWS Meme Generator</h1>
			<h2>Choose an image:</h2>
			<form action="createMeme.jsp" method="post">
				<%
				    S3ImageStorage s3Store = new S3ImageStorage();
				    boolean checked = true;
				    int i = 1;
				    for ( S3ObjectSummary summary : s3Store.getMacros() ) {
				%>
				<div class="meme-option">
					<label class="radio" for="imageKey<%=i%>"><input
						type="radio" name="imageKey" value="<%=summary.getKey()%>"
						<%=checked ? "checked" : ""%> id="imageKey<%=i%>">#<%=i%></label>
					<label for="imageKey<%=i%>"><img
						src="http://<%=AWSResources.BUCKET_NAME%>.s3.amazonaws.com/<%=summary.getKey()%>"></label>
				</div>
				<%
				        checked = false;
				        i++;
				    }
				%>
				<div class="caption-fields">
					<label for="topCaption">Top caption:</label>
					<input type="text" name="topCaption" id="topCaption">
					<label for="bottomCaption">Bottom caption:</label> 
					<input type="text" name="bottomCaption" id="bottomCaption">
					<label for="createdBy">Created by:</label> 
					<input type="text" name="createdBy" id="createdBy">
					<p>
						<button class="btn btn-large btn-primary" type="submit" id="fff">
							<i class="icon-picture icon-white"></i> Create Meme
						</button>
					</p>
				</div>
			</form>
		</div>
	</div>
</body>
</html>
