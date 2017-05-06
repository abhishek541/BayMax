package com.cloud.baymax;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;

import java.util.List;

/**
 * Created by Abhi on 5/5/2017.
 */

public class AWSRekognitionUtil {

    AmazonRekognition rekognitionClient;

    public void init(String accessKey, String secretKey) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        rekognitionClient = AmazonRekognitionClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    private static Image getImageUtil(String bucket, String key) {
        return new Image()
                .withS3Object(new S3Object()
                        .withBucket(bucket)
                        .withName(key));
    }

    public boolean identifyFaceFromUser(Image sourceImage, Image targetImage) {
        Float threshold = 80F;
        CompareFacesRequest request = new CompareFacesRequest().withSourceImage(sourceImage).
                                        withTargetImage(targetImage).withSimilarityThreshold(threshold);
        CompareFacesResult result = rekognitionClient.compareFaces(request);
        List<CompareFacesMatch> matchList = result.getFaceMatches();
        if(matchList.size() == 1){
            return true;
        } else {
            return false;
        }
    }
}
