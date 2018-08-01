package com.example.android.newsfeedapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private static final String RESPONSE = "response";
    private static final String RESULTS = "results";
    private static final String WEB_TITLE = "webTitle";
    private static final String SECTION_NAME = "sectionName";
    private static final String PUBLISHED = "webPublicationDate";
    private static final String FIELDS = "fields";
    private static final String THUMBNAIL = "thumbnail";
    private static final String SHORT_URL = "shortUrl";
    private static final String BODY_TEXT = "bodyText";
    private static final String TAGS = "tags";
    private static final String UNKNOWN_AUTHOR = "Unknown author";

    private QueryUtils() {
    }

    public static List<Article> fetchArticleData(String requestUrl) {

        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        return extractArticleFromJson(jsonResponse);
    }


    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200)
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving JSON data.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<Article> extractArticleFromJson(String jsonResponse) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<Article> articles = new ArrayList<>();

        // Try to extract JSON
        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONObject responseObj = baseJsonResponse.getJSONObject(RESPONSE);
            JSONArray resultsArray = responseObj.getJSONArray(RESULTS);

            for (int i = 0; i < resultsArray.length(); i++) {
                String author = "";
                Bitmap bitmapThumbnail = null;
                JSONObject currentResult = resultsArray.getJSONObject(i);
                String title = currentResult.getString(WEB_TITLE);
                String category = currentResult.getString(SECTION_NAME);
                String published = currentResult.getString(PUBLISHED);

                JSONObject fields = currentResult.getJSONObject(FIELDS);
                if (fields.has(THUMBNAIL)) {
                    String thumbnail = fields.getString(THUMBNAIL);
                    URL urlThumbnail = new URL(thumbnail);
                    bitmapThumbnail = BitmapFactory.decodeStream(urlThumbnail.openConnection().getInputStream());
                }
                String url = fields.getString(SHORT_URL);
                String content = fields.getString(BODY_TEXT);

                JSONArray tagsArray = currentResult.getJSONArray(TAGS);
                for (int j = 0; j < tagsArray.length(); j++) {
                    JSONObject tag = tagsArray.getJSONObject(j);
                    if (tag.has(WEB_TITLE)) {
                        author = tag.getString(WEB_TITLE);
                    } else {
                        author = UNKNOWN_AUTHOR;
                    }
                }
                if (tagsArray.length() == 0) {
                    author = UNKNOWN_AUTHOR;
                }

                Article article = new Article(title, category, published, bitmapThumbnail, url, author, content);
                articles.add(article);
            }

        } catch (JSONException jsone) {
            Log.e(LOG_TAG, "Problem parsing JSON file", jsone);
        } catch (MalformedURLException urle) {
            Log.e(LOG_TAG, "Malformed URL has occurred", urle);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "I/O exception occurred", ioe);
        }

        return articles;
    }
}
