package com.cloud.baymax;

/**
 * Created by Abhi on 5/10/2017.
 */

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

public class AWSSNSUtil {

    AmazonSNSClient snsClient;
    private static final String topicArn = "arn:aws:sns:us-east-1:069315280182:Location-warning";
    private static final String message = "Tum rasta bhatak gye ho...";

    public void init(CognitoCachingCredentialsProvider credentialsProvider) {
        snsClient = new AmazonSNSClient(credentialsProvider.getCredentials());
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
    }

    public void subscribeToTopic(){
        SubscribeRequest subRequest = new SubscribeRequest(topicArn, "sms", "19172831618");
        snsClient.subscribe(subRequest);

        //get request id for SubscribeRequest from SNS metadata
        System.out.println("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subRequest));
    }

    public void sendSMS(){
        PublishRequest publishRequest = new PublishRequest(topicArn, message);
        PublishResult publishResult = snsClient.publish(publishRequest);

        //print MessageId of message published to SNS topic
        System.out.println("MessageId - " + publishResult.getMessageId());
    }
}