<!--
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
 -->

<%@page import="com.amazonaws.memes.*"%>
<%@page import="static com.amazonaws.memes.AWSResources.*"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

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
        <a class="btn btn-primary btn-large" href="memes.jsp">
          See Finished Memes <i class="icon-chevron-right icon-white"></i>
        </a>
      </p>
      <h2>Thanks for submitting!</h2>
      <h2>Your meme will be created shortly.</h2>
      <%
        String imageKey      = request.getParameter("imageKey");
                    String topCaption    = request.getParameter("topCaption");
                    String bottomCaption = request.getParameter("bottomCaption");
                    String createdBy     = request.getParameter("createdBy");

                    MemeUtils memeUtils = new MemeUtils();
                    ImageMacro imageMacro = memeUtils.submitJob(topCaption, bottomCaption, imageKey, createdBy);

                    out.print("<p>Processing...");
                    out.flush();
                    response.flushBuffer();
                    while (!ImageMacro.DONE_STATUS.equals(imageMacro.getStatus())
                        && !ImageMacro.FAILED_STATUS.equals(imageMacro.getStatus())) {
                      Thread.sleep(1000);
                      out.print(".");
                      out.flush();
                      response.flushBuffer();

                      imageMacro = DYNAMODB_MAPPER.load(ImageMacro.class, imageMacro.getId());
                    }
                    if (ImageMacro.DONE_STATUS.equals(imageMacro.getStatus())) {
                      out.print(" All done!</p>");
                    } else {
                      out.print(" Something went wrong :(</p>");
                    }
      %>

      <p>
        <img src="<%= imageMacro.getFinishedImageLink().getUrl() %>">
      </p>

      <p>
        <a class="btn btn-primary btn-large" href="index.jsp">
        <i class="icon-edit icon-white"></i> Create another one!</a>
      </p>
    </div>
  </div>
</body>
</html>
