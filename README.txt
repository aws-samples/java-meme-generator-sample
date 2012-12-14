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
 
 Meme Generator Sample Application
 =================================
 
 This sample application demonstrates using S3, DynamoDB, and SQS to run an image processing application with a web front-end on Elastic Beanstalk and a back-end running on EC2.  For a demonstration of the application and a discussion of its architecture, please watch the following presentation. 
 
 http://www.youtube.com/watch?v=YeRNErD81VA&feature=youtu.be
 
 The application builds in Eclipse.  Follow the instruction below.
 
 Instruction for setup:
 
 1) Download Eclipse for J2EE developers
 2) Download the AWS Toolkit for Eclipse from http://aws.amazon.com/eclipse
 3) Install a tomcat runtime environment for Eclipse.  The simplest way is to choose Window > Preferences > Server > Runtime Environments.  Then add a new Apache Tomcat 7.0 runtime environment, and use the "Download and Install" button to download and install a new version of Apache Tomcat.
 4) Import the 3 projects contained in this sample application into your eclipse workspace.  Chose File > Import > Existing projects into workspace.  Navigate to the directory containing this file and select the "projects" directory.
 5) You should now see the Memes, MemeCommon, and MemeWorker projects in your workspace.
 6) Add the AWS SDK for Java classpath container to each of the three projects.  Right-click on each project and select Build Path > Configure Build Path...  Then, in the "Libraries" tab, select "Add Library" and choose the AWS SDK for Java library.  The latest SDK will be downloaded at this point, if you don't have one installed already.  The code should all compile at this point.  If not, clean all three projects with Project > Clean.
 7) For the Memes project, which runs in Tomcat, you will need to add the SDK jar and third-party jars into WebContent/WEB-INF/lib.  The simplest way to do this is to drag and drop from the file explorer.
   7a) Find the directory where the AWS SDK for Java was downloaded.  This defaults to ${home}/aws-java-sdk, but can be configured via preferences.  We'll call this directory $SDK.
   7b) Drag the $SDK/lib/aws-java-sdk-X.Y.Z.jar file into Memes/WebContent/WEB-INF/lib in Eclipse's file explorer.  Eclipse will ask if you want to link or copy the files.  Copying is more dependable.
   7c) Repeat this process for every jar file in $SDK/third-party/*.  You can leave out the aspectj, spring, freemarker, and java-mail libraries, since they aren't used by the sample.
   7d) Yes, the above process is very tedious.  But we don't want to check an SDK (and all required third-party libs) into source control. 
 8) Locate the AWSCredentials.properties file for each project, and fill in your access key and secret key
 9) In the MemeCommon project, edit src/com/amazonaws/memes/AWSResources.java to configure the name of the S3 bucket to use.  The application uses the us-west-1 region by default.  To use another region, edit the service endpoint constants in this file. 
 10) To create all the AWS resources required by the application, run the above file as a Java program (right-click, Run As... > Java application)
 
 Deploying the Web application:
 
 The application is now ready to deploy to elastic beanstalk (or to a local tomcat server).
 
 To deploy the web application to elastic beanstalk, right-click on the Memes project in the package explorer, then select Run As... > Run on server.  In the deployment wizard, you can create a new elastic beanstalk environment to deploy the application onto if you haven't created one yet.
 
 Running the image processing worker:
 
 The other half of the application is the back-end image processing worker, contained in the MemeWorker project.  You can either run it locally or on EC2.  
 
 To run it locally, just right-click on the MemeWorker class and select Run As ... > Java application.
 
 To run it on EC2, export the MemeWorker project as an executable jar using the File > Export menu.  Then copy it to an EC2 host, and invoke the command "java -f <jar name>".