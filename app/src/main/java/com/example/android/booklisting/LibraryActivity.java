package com.example.android.booklisting;

/**
 * Created by hp on 06-06-2017.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

public class LibraryActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = LibraryActivity.class.getSimpleName();

    /*
    Setting all necessary global variables and constants.
    */
    private String gbRequestURL = "";
    private String titleText;
    private String authorText;
    private int hits = 0;
    private int index = 0;
    private static final int MAX_HITS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        /*
        Finding and setting views to variables.
         */
        TextView prevBtn = (TextView) findViewById(R.id.previous_btn);
        TextView nextBtn = (TextView) findViewById(R.id.next_btn);
        ImageView prevArrow = (ImageView)findViewById(R.id.prev_arrow);
        ImageView nextArrow = (ImageView)findViewById(R.id.next_arrow);

        /*
        Getting extras from MainActivity.  Extras are the search criteria entered by user
        in main activity.
         */
        Intent intent = getIntent();

        titleText = intent.getStringExtra("TITLE_SEARCH_STRING");
        if (!titleText.equals("")) {
            titleText = getString(R.string.TITLE_QUERY_URL) + titleText; //Adding prefix for Title search to use in URL for google API.
        } else {
            titleText = "";
        }

        authorText = intent.getStringExtra("AUTHOR_SEARCH_STRING");
        if (!authorText.equals("")) {
            authorText = getString(R.string.AUTHOR_QUERY_URL) + authorText; //Adding prefix for Author search to use in URL for google API.
        } else {
            authorText = "";
        }


        /*
        Creating URL string for google books API search by combining all variables in the appropriate syntax.
         */
        gbRequestURL = getString(R.string.MAIN_QUERY_URL) + titleText + authorText + getString(R.string.INDEX_URL) + String.valueOf(index) + getString(R.string.MAX_RESULTS_URL) + String.valueOf(MAX_HITS);

        /*
        Starting AsyncTask to connect to google books API and fetch book data.
         */
        LibrarySearchAsyncTask task = new LibrarySearchAsyncTask();
        task.execute();

        /*
        Setting onClickListeners for next and previous buttons
         */
        if (prevBtn != null && nextBtn != null && prevArrow != null && nextArrow != null) {
            nextBtn.setOnClickListener(this);
            prevBtn.setOnClickListener(this);
            nextArrow.setOnClickListener(this);
            prevArrow.setOnClickListener(this);
        }
    }

    /**
     * Update the screen to display information from the given {@link Library}.
     */
    private void updateUi(final ArrayList<Library> books) {

        // Find a reference to the ListView} in the layout
        final LibraryAdapter libraryAdapter = new LibraryAdapter(this, books);

        // Get a reference to the ListView, and attach the adapter to the listView if listview is not null.
        ListView booksListView = (ListView) findViewById(R.id.list);
        if (booksListView != null) {
            booksListView.setAdapter(libraryAdapter);

            // Setting OnItemClickListener for items in listview, if clicked will load books infoLink in Web Browser.
            booksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(books.get(position).getUrl())));
                }
            });
        }

        /*
        Finding and setting views to variables for new scope.
         */
        TextView prevBtn = (TextView) findViewById(R.id.previous_btn);
        TextView nextBtn = (TextView) findViewById(R.id.next_btn);
        ImageView prevArrow = (ImageView)findViewById(R.id.prev_arrow);
        ImageView nextArrow = (ImageView)findViewById(R.id.next_arrow);

        /*
        Setting visibility for buttons if more info is available to view then is currently displayed in listview.
         */
        if (prevBtn != null && nextBtn != null && prevArrow != null && nextArrow != null) {

            if (hits <= MAX_HITS + index) {
                nextBtn.setVisibility(View.GONE);
                nextArrow.setVisibility(View.GONE);
            } else {
                nextBtn.setVisibility(View.VISIBLE);
                nextArrow.setVisibility(View.VISIBLE);
            }

            if(index == 0){
                prevBtn.setVisibility(View.GONE);
                prevArrow.setVisibility(View.GONE);
            }else{
                prevBtn.setVisibility(View.VISIBLE);
                prevArrow.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {

        /*
        Starting a new AsyncTask to load new page of books.
         */
        if(v.getId() == R.id.previous_btn || v.getId() == R.id.prev_arrow ) {
            index = index - MAX_HITS;
            gbRequestURL = getString(R.string.MAIN_QUERY_URL) + titleText + authorText + getString(R.string.INDEX_URL) + String.valueOf(index) + getString(R.string.MAX_RESULTS_URL) + String.valueOf(MAX_HITS);
            LibrarySearchAsyncTask task = new LibrarySearchAsyncTask();
            task.execute();
        }else {
            index = index + MAX_HITS;
            gbRequestURL = getString(R.string.MAIN_QUERY_URL) + titleText + authorText + getString(R.string.INDEX_URL) + String.valueOf(index) + getString(R.string.MAX_RESULTS_URL) + String.valueOf(MAX_HITS);
            LibrarySearchAsyncTask task = new LibrarySearchAsyncTask();
            task.execute();
        }
    }

    /**
     * {@link AsyncTask} to perform the network request and fetch data from google books API on a background thread.
     */
    private class LibrarySearchAsyncTask extends AsyncTask<URL, Void, ArrayList> {

        //setting progress dialog @ onPreExecute to show while background thread is running
        ProgressDialog asyncDialog = new ProgressDialog(LibraryActivity.this);
        @Override
        protected void onPreExecute() {

            //setting message of the dialog
            asyncDialog.setMessage(getString(R.string.loading_book_data));
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Library> doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(gbRequestURL);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException thrown, problem making Http Request", e);
            }

            // Extract relevant fields from the JSON response and create an  object
            // Return the  object as the result of the LibrarySearchAsyncTask
            return extractFeatureFromJson(jsonResponse);
        }


        @Override
        protected void onPostExecute(ArrayList books) {
            if (books == null || books.size() == 0) {
                asyncDialog.dismiss();
                searchResultsDialogShower(getString(R.string.no_data));
            }else{
                updateUi(books);
                asyncDialog.dismiss();
            }
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error with HttpRequest.  Error response code: " + urlConnection.getResponseCode());

                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException thrown, problem collecting google books JSON results", e);
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

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
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

        /**
         * Return an {@link Library} object by parsing out information
         * about the first book from the input booksJSON string.
         */
        private ArrayList<Library> extractFeatureFromJson(String booksJSON) {

            final ArrayList<Library> books = new ArrayList<>();
            if (TextUtils.isEmpty(booksJSON)) {
                return null;
            }
            try {
                JSONObject baseJsonResponse = new JSONObject(booksJSON);
                JSONArray itemsArray = baseJsonResponse.getJSONArray("items");

                hits = baseJsonResponse.optInt("totalItems");

                // If there are results in the itemsArray
                if (itemsArray.length() > 0) {

                    // Extract out the first feature (which is an earthquake)
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject bookItem = itemsArray.getJSONObject(i);
                        JSONObject volumeInfo = bookItem.getJSONObject("volumeInfo");
                        JSONObject images = volumeInfo.optJSONObject("imageLinks");
                        JSONArray authors = volumeInfo.optJSONArray("authors");

                        // Extract out the title, author, details, and urls.
                        String imageUrl;
                        String author;
                        String title = volumeInfo.optString("title");
                        if (authors != null) {
                            author = authors.optString(0);
                        } else {
                            author = "unknown"; //Add unknown if no author string is found
                        }
                        String description = volumeInfo.optString("description");
                        String url = volumeInfo.getString("infoLink");

                        Library library = new Library(title, author, description, url);
                        books.add(library);
                    }
                    // Create a new {@link Event} object
                    return new ArrayList<>(books);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the books JSON results", e);
            }
            return null;
        }
    }

    /**
     * Show a alert dialog.
     */
    public void searchResultsDialogShower(String alertMessage) {

        //inflating custom alert message view @R.layout.custom_alert
        LayoutInflater factory = LayoutInflater.from(this);
        final View searchDialogView = factory.inflate(R.layout.alert, null);

        //Setting inflated view to AlertDialog.
        final AlertDialog searchDialog = new AlertDialog.Builder(this).create();
        searchDialog.setView(searchDialogView);
        searchDialog.show();

        Button okBtn = (Button) searchDialogView.findViewById(R.id.ok_btn);

        //Setting alert message to view from String passed in @param alertMessage
        TextView messageTextView = (TextView) searchDialogView.findViewById(R.id.alert);
        messageTextView.setText(alertMessage);

        //Setting OnclickListener for OK button to get user confirmation
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDialog.dismiss();
                finish();
            }
        });
    }
}
