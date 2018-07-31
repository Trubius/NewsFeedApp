package com.example.android.newsfeedapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
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

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<Article>>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String REQUEST_URL = "https://content.guardianapis.com/search?";
    private static final int ARTICLE_LOADER_ID = 1;
    private static final String QUERY_STRING = "queryString";
    private ArticleAdapter mArticleAdapter;
    private TextView mEmptyView;
    private View loadingIndicator;
    private EditText searchTextView;
    private String queryString;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LoaderManager loaderManager = getLoaderManager();
    private EmptyRecyclerView recyclerView;
    private SharedPreferences sharedPrefs;

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
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        loadingIndicator = findViewById(R.id.loading_indicator);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        if (savedInstanceState != null) {
            queryString = savedInstanceState.getString(QUERY_STRING);
        }
        if (queryString != null) {
            getSupportActionBar().setTitle("Search for " + queryString);
        }
        setSwipeRefresh();
        hideKeyboard(findViewById(R.id.root_view));

        if (checkNetworkConnection()) {
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
            handleIntent(getIntent());
        } else {
            loadingIndicator.setVisibility(GONE);
            mEmptyView.setText(getString(R.string.no_internet));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY_STRING, queryString);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_categories_key))) {
            mArticleAdapter.clear();
            mEmptyView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
            if (checkNetworkConnection()) {
                loaderManager.restartLoader(ARTICLE_LOADER_ID, null, this);
            } else {
                recyclerView.setEmptyView(mEmptyView);
                loadingIndicator.setVisibility(GONE);
                mEmptyView.setText(getString(R.string.no_internet));
            }
        }
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String categories = sharedPrefs.getString(getString(R.string.settings_categories_key), getString(R.string.settings_categories_default));
        Uri baseUri = Uri.parse(REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        if (!categories.equals("recent")) {
            uriBuilder.appendQueryParameter("section", categories);
        }
        if (queryString != null) {
            uriBuilder.appendQueryParameter("q", queryString);
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
        recyclerView.setEmptyView(mEmptyView);
        if (checkNetworkConnection()) {
            mEmptyView.setText(R.string.no_articles);
        } else {
            mEmptyView.setText(getString(R.string.no_internet));
        }
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
        inflater.inflate(R.menu.menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenu = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchMenu.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.onActionViewExpanded();
        searchView.setQueryHint(getString(R.string.search));
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
                queryString = query;
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
        if (id == R.id.settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            queryString = intent.getStringExtra(SearchManager.QUERY);
            getSupportActionBar().setTitle("Search for " + queryString);
            mEmptyView.setVisibility(GONE);
            loaderManager.restartLoader(ARTICLE_LOADER_ID, null, this);
        }
    }

    private void hideKeyboard(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
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
                queryString = null;
                getSupportActionBar().setTitle(R.string.app_name);
                mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_dark),
                        getResources().getColor(android.R.color.holo_red_dark),
                        getResources().getColor(android.R.color.holo_orange_dark),
                        getResources().getColor(android.R.color.holo_green_dark));
                recyclerView.setVisibility(GONE);
                mEmptyView.setVisibility(GONE);
                loadingIndicator.setVisibility(VISIBLE);
                if (checkNetworkConnection()) {
                    loaderManager.restartLoader(ARTICLE_LOADER_ID, null, MainActivity.this);
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
