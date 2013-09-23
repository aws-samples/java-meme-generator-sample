/*
 * Copyright 2013 Amazon Technologies, Inc.
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
 
 Meme Generator Sample Application
 =================================
 
 This sample application demonstrates using Amazon S3, Amazon DynamoDB, and Amazon SQS to run an image processing application with a web front-end on AWS Elastic Beanstalk and a back-end running on Amazon EC2.  For a demonstration of the application and a discussion of its architecture, please watch the following presentation. 
 
 http://www.youtube.com/watch?v=YeRNErD81VA&feature=youtu.be
 
 The application builds in Eclipse.  Follow the instruction below.
 
 Instruction for setup:
 
 1) Download Eclipse for J2EE developers
 2) Install the AWS Toolkit for Eclipse from http://aws.amazon.com/eclipse
 3) Install a Tomcat runtime environment for Eclipse.  The simplest way is to choose Window > Preferences > Server > Runtime Environments.  Then add a new Apache Tomcat 7.0 runtime environment, and use the "Download and Install" button to download and install a new version of Apache Tomcat.
 4) Import the 3 projects contained in this sample application into your eclipse workspace.  Chose File > Import > Existing projects into workspace.  Navigate to the directory containing this file and select the "projects" directory.
 5) You should now see the Memes, MemeCommon, and MemeWorker projects in your workspace.  If the code isn't compiling, make sure you've installed a Tomcat 7.0 runtime as described in step 3.  If the code still doesn't compile, you can try cleaning all three projects with Project > Clean.
 6) Locate the AWSCredentials.properties file in the MemeCommon project, and fill in your AWS security credentials.
 7) In the MemeCommon project, edit src/com/amazonaws/memes/AWSResources.java to configure the name of the S3 bucket to use.  The application uses the us-west-1 region by default.  To use another region, edit the service endpoint constants in this file. 
 8) To create all the AWS resources required by the application, run the above file as a Java program (right-click, Run As... > Java application)
 
 Deploying the Web application:
 
 The application is now ready to deploy to AWS Elastic Beanstalk (or to a local tomcat server).
 
 To deploy the web application to AWS Elastic Beanstalk, right-click on the Memes project in the package explorer, then select Run As... > Run on server.  In the deployment wizard, you can create a new AWS Elastic Beanstalk environment to deploy the application into if you haven't created one yet.
 
 Running the image processing worker:
 
 The other half of the application is the back-end image processing worker, contained in the MemeWorker project.  You can either run it locally or on Amazon EC2.  
 
 To run it locally, just right-click on the MemeWorker class and select Run As ... > Java application.
 
 To run it on Amazon EC2, export the MemeWorker project as an executable jar using the File > Export menu.  Then copy it to an EC2 host, and invoke the command "java -f <jar name>".
