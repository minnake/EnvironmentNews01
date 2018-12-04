package com.example.keinonen.environmentnews;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {

    private static final String LOG_TAG = MainActivity.class.getName();

    ConnectivityManager connectivityManager;
    NetworkInfo networkInfo = null;
    LoaderManager loaderManager;

    private NewsAdapter newsAdapter;

    private static final int NEWS_LOADER_ID = 1;

    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emptyStateTextView = findViewById(R.id.empty_text_view);

        ListView newsListView = findViewById(R.id.list);
        newsListView.setEmptyView(emptyStateTextView);

        newsAdapter = new NewsAdapter(this, new ArrayList<News>());

        newsListView.setAdapter(newsAdapter);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                News currentNews = newsAdapter.getItem(i);
                Uri newsUri = null;

                if (currentNews != null) {
                    newsUri = Uri.parse(currentNews.getUrl());
                }

                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                startActivity(websiteIntent);
            }
        });

        connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        if (networkInfo != null && networkInfo.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
            Log.e(LOG_TAG, "initLoader()");
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            emptyStateTextView.setText(R.string.no_internet);
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle bundle) {
        Log.e(LOG_TAG, "onCreateLoader()");
        String GUARDIAN_REQUEST_URL = this.getString(R.string.request_url);
        return new NewsLoader(this, GUARDIAN_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> newest) {
        Log.e(LOG_TAG, "onLoadFinished()");
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        if (networkInfo == null || !networkInfo.isConnected()) {
            emptyStateTextView.setText(R.string.no_internet);
        } else {
            emptyStateTextView.setText(R.string.no_news);
        }

        newsAdapter.clear();

        if (newest != null && !newest.isEmpty()) {
            newsAdapter.addAll(newest);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        Log.e(LOG_TAG, "onLoaderReset()");
        newsAdapter.clear();
    }
}