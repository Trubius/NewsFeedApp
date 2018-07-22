package com.example.android.newsfeedapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<Article>> {

    private static final String REQUEST_URL = "https://content.guardianapis.com/search?";
    private static final int ARTICLE_LOADER_ID = 1;
    private ArticleAdapter mArticleAdapter;
    private TextView mEmptyView;
    private View loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EmptyRecyclerView recyclerView = (EmptyRecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        recyclerView.setEmptyView(mEmptyView);
        mArticleAdapter = new ArticleAdapter(this, new ArrayList<Article>());
        recyclerView.setAdapter(mArticleAdapter);

        checkNetworkConnection();
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        Uri baseUri = Uri.parse(REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("show-fields", "all");
        uriBuilder.appendQueryParameter("api-key", "aeeecf4b-8953-4a2b-850d-f2fabdadc38c");

        return new ArticleLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {
        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(GONE);
        mEmptyView.setText(R.string.no_articles);
        mArticleAdapter.clear();

        if (articles != null && !articles.isEmpty()){
            mArticleAdapter.addAll(articles);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        mArticleAdapter.clear();
    }

    private void checkNetworkConnection() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
        } else {
            loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(GONE);

            mEmptyView.setText(getString(R.string.no_internet));
        }
    }
}
