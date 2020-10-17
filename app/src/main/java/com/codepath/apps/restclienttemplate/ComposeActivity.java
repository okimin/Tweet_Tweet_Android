package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Headers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

public class ComposeActivity extends AppCompatActivity {

    EditText etCompose;
    Button buttonTweet;
    TwitterClient client;
    public static final int  MAX_TWEET_LEN =140;
    public static final String  TAG = "ComposeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        etCompose = findViewById(R.id.etCompose);
        buttonTweet = findViewById(R.id.buttonTweet);

        client = TwitterApp.getRestClient(this);

        buttonTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweetContent = etCompose.getText().toString();
                if(tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Your Tweet cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(tweetContent.length()>MAX_TWEET_LEN) {
                    Toast.makeText(ComposeActivity.this, "Your Tweet cannot longer than 140 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                client.publishTweets(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess getting Tweet");
                        try {
                            Tweet tweet= Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "onSuccess publishing tweet" + tweet);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));

                            setResult(RESULT_OK,intent);

                            finish();


                        }
                        catch (JSONException error){
                            Log.e(TAG,error.toString());

                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG,"Failure to post tweet", throwable);
                    }
                });
                Toast.makeText(ComposeActivity.this, "Your Tweet is just right :)", Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }
}