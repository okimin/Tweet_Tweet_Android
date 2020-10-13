package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Headers;

import android.os.Bundle;
import android.util.Log;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    RecyclerView rvTweets;
    TwitterClient client;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
    client = TwitterApp.getRestClient(this);

    //Find Recycler View
        rvTweets = findViewById(R.id.rvTweets);
        //Init the list of tweets and adapter
        tweets= new ArrayList<>();
        adapter= new TweetsAdapter(this, tweets);
        //Recycler View setup: Layout Manager and Adapter

        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);

        swipeContainer = findViewById(R.id.swipeContainer);
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "fetching new data");
                populateHomeTimeline();
            }
        });

    scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
        @Override
        public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
            Log.i(TAG, "New Page " + page);
            loadMoreData();
        }
    };
    rvTweets.addOnScrollListener(scrollListener);
    populateHomeTimeline();

    }

    private void loadMoreData() {
        // 1. Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "OnSuccess " + json.toString());
                // 2. Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try{
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    adapter.addAll(tweets);

                }catch (JSONException e)
                {
                    Log.e(TAG ,"JSON Exception",e);
                }
                // 3. Append the new data objects to the existing set of items inside the array of items
                // 4. Notify the adapter of the new items made with `notifyItemRangeInserted()`
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "OnFailure on LoadData " , throwable);
            }
        },tweets.get(tweets.size()-1).id);

    }

    private void populateHomeTimeline(){
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "On Success "+ json.toString() );
                JSONArray jsonArray= json.jsonArray;
                try {
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    swipeContainer.setRefreshing(false);

                } catch (JSONException e) {
                    Log.e(TAG ,"JSON Exception",e);

                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "On Failure " + response , throwable);

            }
        });
    }
}