package com.cloud.baymax;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhi on 5/5/2017.
 */

public class AWSS3Util {

    private final String SUFFIX = "/";
    public static String bucketName = "faces-for-rekognition";

    AmazonS3Client s3;
    //TransferUtility = getTransferUtility();

    public void init(Context applicationContext,CognitoCachingCredentialsProvider credentialsProvider) {
        s3 = new AmazonS3Client(credentialsProvider.getCredentials());
    }

    public boolean uploadToS3Bucket(String fileName, File file){
        PutObjectResult por = s3.putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
        if (por != null) {
            return true;
        } else {
            return false;
        }
    }

    public S3Object readFromS3(String bucketName, String key) throws IOException {
        S3Object s3object = s3.getObject(new GetObjectRequest(
                bucketName, key));
        return s3object;
    }

    public List<String> getAllKeys() {
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName);
        ListObjectsV2Result result = s3.listObjectsV2(request);
        List<String> keyList = new ArrayList<String>();

        for(S3ObjectSummary objSummary: result.getObjectSummaries()) {
            keyList.add(objSummary.getKey());
        }

        return keyList;
    }

}
