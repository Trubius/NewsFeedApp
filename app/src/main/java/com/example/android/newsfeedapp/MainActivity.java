package com.example.android.newsfeedapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<Article>> {

    private static final String REQUEST_URL = "https://content.guardianapis.com/search?";
    private static final int ARTICLE_LOADER_ID = 1;
    private ArticleAdapter mArticleAdapter;
    private TextView mEmptyView;
    private View loadingIndicator;
    private EditText searchTextView;
    private String getQuery = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LoaderManager loaderManager = getLoaderManager();
    private EmptyRecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (EmptyRecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        recyclerView.setEmptyView(mEmptyView);
        mArticleAdapter = new ArticleAdapter(this, new ArrayList<Article>());
        recyclerView.setAdapter(mArticleAdapter);
        loadingIndicator = findViewById(R.id.loading_indicator);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);

        setSwipeRefresh();
        hideKeyboard(findViewById(R.id.root_view));

        if (checkNetworkConnection()) {
            handleIntent(getIntent());
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
        } else {
            loadingIndicator.setVisibility(GONE);
            mEmptyView.setText(getString(R.string.no_internet));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        Uri baseUri = Uri.parse(REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        if (getQuery != null) {
            uriBuilder.appendQueryParameter("q", getQuery);
            uriBuilder.appendQueryParameter("order-by", "relevance");
        } else {
            uriBuilder.appendQueryParameter("order-by", "newest");
        }
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("show-fields", "all");
        uriBuilder.appendQueryParameter("api-key", "aeeecf4b-8953-4a2b-850d-f2fabdadc38c");

        return new ArticleLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {
        loadingIndicator.setVisibility(GONE);
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mEmptyView.setText(R.string.no_articles);
        mArticleAdapter.clear();

        if (articles != null && !articles.isEmpty()) {
            mArticleAdapter.addAll(articles);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        mArticleAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenu = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchMenu.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.onActionViewExpanded();
        searchView.setQueryHint("Search");
        int searchSrcTextView = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        searchTextView = searchView.findViewById(searchSrcTextView);
        searchTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideSoftKeyboard(v);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getQuery = query;
                searchView.clearFocus();
                searchMenu.collapseActionView();
                recyclerView.setVisibility(GONE);
                loadingIndicator.setVisibility(VISIBLE);
                if (checkNetworkConnection()) {
                    return false;
                } else {
                    mArticleAdapter.clear();
                    recyclerView.setEmptyView(mEmptyView);
                    loadingIndicator.setVisibility(GONE);
                    mEmptyView.setText(getString(R.string.no_internet));
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search) {
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            getQuery = intent.getStringExtra(SearchManager.QUERY);
            getSupportActionBar().setTitle("Search for " + getQuery);
            mEmptyView.setVisibility(GONE);
            loaderManager.restartLoader(0, null, this);
        }
    }

    private void hideKeyboard(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    searchTextView.clearFocus();
                    hideSoftKeyboard(v);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                hideKeyboard(innerView);
            }
        }
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private boolean checkNetworkConnection() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void setSwipeRefresh() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getQuery = null;
                getSupportActionBar().setTitle(R.string.app_name);
                mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_dark),
                        getResources().getColor(android.R.color.holo_red_dark),
                        getResources().getColor(android.R.color.holo_orange_dark),
                        getResources().getColor(android.R.color.holo_green_dark));
                recyclerView.setVisibility(GONE);
                mEmptyView.setVisibility(GONE);
                loadingIndicator.setVisibility(VISIBLE);
                if (checkNetworkConnection()) {
                    loaderManager.restartLoader(0, null, MainActivity.this);
                } else {
                    mArticleAdapter.clear();
                    recyclerView.setEmptyView(mEmptyView);
                    loadingIndicator.setVisibility(GONE);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mEmptyView.setText(getString(R.string.no_internet));
                }
            }
        });
    }
}
