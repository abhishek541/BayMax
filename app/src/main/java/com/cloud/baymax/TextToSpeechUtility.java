package com.cloud.baymax;

import android.content.Context;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

/**
 * Created by PRAVAR on 07-05-2017.
 */

public class TextToSpeechUtility {
    private final TextToSpeech service = new TextToSpeech();
    private final Context context;
    private final String TTS_username;
    private final String TTS_password;
    private StreamPlayer streamPlayer;

    public TextToSpeechUtility(Context context) {
        TTS_username = context.getString(R.string.TTS_username);
        TTS_password = context.getString(R.string.TTS_password);
        this.context = context;
        //Watson Text-to-Speech Service on Bluemix
        service.setUsernameAndPassword(TTS_username, TTS_password);

    }

    public void speakOutMessage(final Message outMessage){
        Thread thread = new Thread(new Runnable() {
            public void run() {
                Message audioMessage;
                try {

                    audioMessage =outMessage;
                    streamPlayer = new StreamPlayer();
                    if(audioMessage != null && !audioMessage.getMessage().isEmpty())
                        //Change the Voice format and choose from the available choices
                        streamPlayer.playStream(service.synthesize(audioMessage.getMessage(), Voice.EN_LISA).execute());
                    else
                        streamPlayer.playStream(service.synthesize("No Respone from BayMax", Voice.EN_LISA).execute());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
