package com.cloud.baymax;

/**
 * Created by Abhi on 5/10/2017.
 */

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

public class AWSSNSUtil {

    AmazonSNSClient snsClient;
    private static final String topicArn = "arn:aws:sns:us-east-1:084177367647:Way_Lost";
    private static final String message = "ALERT : You have wandered away from your destination !";

    public void init(Context applicationContext) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                applicationContext,
                "us-east-1:aa997b37-2281-45b2-a96f-fa40bf5d240b", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        snsClient = new AmazonSNSClient(credentialsProvider);
        //snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
    }

    public void subscribeToTopic(){
        //SubscribeRequest subRequest = new SubscribeRequest(topicArn, "sms", "19172831618");
        SubscribeRequest subRequest = new SubscribeRequest(topicArn, "sms", "+16463095628");
        snsClient.subscribe(subRequest);

        //get request id for SubscribeRequest from SNS metadata
        Log.d("SNSLog","SubscribeRequest - " + snsClient.getCachedResponseMetadata(subRequest));
    }

    public void sendSMS(){
        PublishRequest publishRequest = new PublishRequest(topicArn, message);
        PublishResult publishResult = snsClient.publish(publishRequest);

        //print MessageId of message published to SNS topic
        Log.d("SNSLog","MessageId - " + publishResult.getMessageId());
    }
}