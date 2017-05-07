package com.cloud.baymax;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;

/**
 * Created by PRAVAR on 06-05-2017.
 */

public class CompareFaces {
    public static final String S3_BUCKET = "faces-for-rekognition";
    private static Context applicationContext;
    private final AWSS3Util awss3Util;
    private final CognitoCachingCredentialsProvider credentialsProvider;

    public CompareFaces(Context applicationContext,AWSS3Util awss3Util,CognitoCachingCredentialsProvider credentialsProvider){
        this.applicationContext = applicationContext;
        this.awss3Util = awss3Util;
        this.credentialsProvider = credentialsProvider;

        try {
            //credentials = new ProfileCredentialsProvider("AdminUser").getCredentials();
            //credentials= new BasicAWSCredentials(String.valueOf(R.string.aws_access_key_id),String.valueOf(R.string.aws_secret_access_key));
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (/Users/userid/.aws/credentials), and is in valid format.", e);
        }

        /*AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();*/

        /*CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                applicationContext,
                "us-east-1:aa997b37-2281-45b2-a96f-fa40bf5d240b", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );*/

        AmazonRekognitionClient rekognitionClient = new AmazonRekognitionClient(credentialsProvider);




        //Image source = getImageUtil(S3_BUCKET, "source.jpg");
        Image target = getImageUtil(S3_BUCKET, "target.jpg");
        Float similarityThreshold = 0F;

        CompareFacesRequestSender compareFacesRequestSender = new CompareFacesRequestSender(target, similarityThreshold, rekognitionClient,applicationContext);
        compareFacesRequestSender.execute("abc");
    }

    private static CompareFacesResult callCompareFaces(Image sourceImage, Image targetImage,
                                                       Float similarityThreshold, AmazonRekognition amazonRekognition) {
        try {
            CompareFacesRequest compareFacesRequest = new CompareFacesRequest()
                    .withSourceImage(sourceImage)
                    .withTargetImage(targetImage)
                    .withSimilarityThreshold(similarityThreshold);
            return amazonRekognition.compareFaces(compareFacesRequest);
        }catch(Exception e){
            Toast.makeText( applicationContext,"No detectable face in the uploaded picture !",Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private static Image getImageUtil(String bucket, String key) {
        return new Image()
                .withS3Object(new S3Object()
                        .withBucket(bucket)
                        .withName(key));
    }

    private class CompareFacesRequestSender extends AsyncTask<String, Void, List<CompareFacesResult>> {
        private final Context context;
        private final Image target;
        private final Float similarityThreshold;
        private final AmazonRekognitionClient rekognitionClient;
        private  List<String> sourceKeys;

        public CompareFacesRequestSender(Image target,Float similarityThreshold,AmazonRekognitionClient rekognitionClient,Context context){
            this.context=context;
            this.target = target;
            this.similarityThreshold = similarityThreshold;
            this.rekognitionClient = rekognitionClient;
        }
        @Override
        protected List<CompareFacesResult> doInBackground(String... url) {
            sourceKeys = awss3Util.getAllKeys();
            List<CompareFacesResult> faceMatchResults = new ArrayList<>();

            for(String sourceKey : sourceKeys){
                if(!sourceKey.equalsIgnoreCase("target.jpg")) {
                    Image source = getImageUtil(S3_BUCKET, sourceKey);
                    CompareFacesResult compareFaceResult = callCompareFaces(source,
                            target,
                            similarityThreshold,
                            rekognitionClient);
                    faceMatchResults.add(compareFaceResult);
                }
            }
            return faceMatchResults;
        }

        @Override
        protected void onPostExecute(List<CompareFacesResult> faceMatchResults) {
            super.onPostExecute(faceMatchResults);
            int indexWithMaxSimilarity = 0;
            float maxSimilarity = 0;
            int index = 0;

            for(CompareFacesResult faceMatchResult : faceMatchResults){
                List <CompareFacesMatch> faceDetails = faceMatchResult.getFaceMatches();
                for (CompareFacesMatch match: faceDetails){
                    ComparedFace face= match.getFace();
                    BoundingBox position = face.getBoundingBox();
                    Log.d("Face Result","Face at " + position.getLeft().toString()
                            + " " + position.getTop()
                            + " matches with " + face.getConfidence().toString()
                            + "% confidence and has "+ match.getSimilarity() + " Similarity");
                    if(match.getSimilarity() > maxSimilarity){
                        indexWithMaxSimilarity = index;
                        maxSimilarity = match.getSimilarity();
                    }
                    index++;
                }

            }

            String detectedPerson = sourceKeys.get(indexWithMaxSimilarity);
            detectedPerson = detectedPerson.substring(0,detectedPerson.length()-4);
            TextToSpeechUtility textToSpeechUtility =new TextToSpeechUtility(applicationContext);
            if(maxSimilarity >70 ) {
                //Toast.makeText(context, "This is " + detectedPerson, Toast.LENGTH_LONG).show();
                Message message = new Message();
                message.setMessage("The name of this person is"+detectedPerson);
                textToSpeechUtility.speakOutMessage(message);
            }else{
                //Toast.makeText(context, "This person is not in the database", Toast.LENGTH_LONG).show();
                Message message = new Message();
                message.setMessage("This person is not in the database");
                textToSpeechUtility.speakOutMessage(message);
            }


        }

    }
}
