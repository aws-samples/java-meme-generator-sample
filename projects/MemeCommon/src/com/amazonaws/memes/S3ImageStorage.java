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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Stores and retrieves images in S3.
 */
public class S3ImageStorage {
        
    private static final AmazonS3 s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());   
    
    public S3ImageStorage() {        
    }
    
    public BufferedImage loadBlankImage(String key) throws IOException {        
        S3Object object = s3.getObject(AWSResources.BUCKET_NAME, key);
        return ImageIO.read(object.getObjectContent());
    }
    
    /**
     * Stores the given finished image and returns the key it is stored with.
     */
    public String storeFinishedImage(BufferedImage image, String key) throws IOException {
        key = AWSResources.FINISHED_PATH + key + ".png";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.close();
        s3.putObject(AWSResources.BUCKET_NAME, key, new ByteArrayInputStream(baos.toByteArray()), null);
        s3.setObjectAcl(AWSResources.BUCKET_NAME, key, CannedAccessControlList.PublicRead);
        return key;
    }
    
    public List<S3ObjectSummary> getMacros() {
        return s3.listObjects(AWSResources.BUCKET_NAME, AWSResources.MACRO_PATH).getObjectSummaries();
    }
    
    public List<S3ObjectSummary> getFinishedMemes() {
        return s3.listObjects(AWSResources.BUCKET_NAME, AWSResources.FINISHED_PATH).getObjectSummaries();
    }
    
}
