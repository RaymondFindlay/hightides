package com.example.hightides;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SurfNews extends AppCompatActivity {

    ListView listView;
    TextView textView;
    ArrayList<String> titles;
    ArrayList<String> links;

    private static final String SURFING_RSS_FEED_URL = "https://www.surfer.com/blogs/feed/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surf_news);

        // Assignments
        listView = findViewById(R.id.listView_Surf_News);
        textView = findViewById(R.id.textView_Surf_News_Title);
        titles = new ArrayList<>();
        links = new ArrayList<>();

        if(isNetworkAvailable()) {
            // Set onclick listener for list view
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Uri uri = Uri.parse(links.get(position));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });

            // Execute AsyncTask to get RSS Feed
            new ProcessRSSFeed().execute();
        }
        else {
            listView.setVisibility(View.INVISIBLE);
            textView.setText("Check Connection...");
            Toast.makeText(SurfNews.this,
                    "Unable to connect. Please check internet connection.", Toast.LENGTH_LONG).show();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Logout of app
    public void logOut(View view) {
        Intent intent = new Intent(SurfNews.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        }
        catch (IOException e) {
            return null;
        }
    }

    public class ProcessRSSFeed extends AsyncTask<Integer, Void, Exception> {

        ProgressDialog progressDialog = new ProgressDialog(SurfNews.this);
        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading RSS feed...");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... integers) {

            try {
                URL url = new URL(SURFING_RSS_FEED_URL);

                // Create new instance of XML pull parser factory
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                // will not provide support for xml namespaces
                factory.setNamespaceAware(false);
                // Create new XML pull parser
                XmlPullParser xmlPullParser = factory.newPullParser();

                // Start reading from URL
                xmlPullParser.setInput(getInputStream(url), "UTF_8");

                // Flag to check if inside correct XML tag
                boolean insideItem = false;
                // Returns type of current event
                int eventType = xmlPullParser.getEventType();

                // Loop through XML
                while(eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_TAG) {
                        if(xmlPullParser.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        }
                        else if(xmlPullParser.getName().equalsIgnoreCase("title")) {
                            if(insideItem) {
                                titles.add(xmlPullParser.nextText());
                            }
                        }
                        else if(xmlPullParser.getName().equalsIgnoreCase("link")) {
                            if(insideItem) {
                                links.add(xmlPullParser.nextText());
                            }
                        }
                    }
                    else if(eventType == XmlPullParser.END_TAG && xmlPullParser.getName()
                            .equalsIgnoreCase("item")) {
                            insideItem = false;
                    }

                    eventType = xmlPullParser.next();
                }
            }
            catch(MalformedURLException e){
                exception = e;
            }
            catch(XmlPullParserException e) {
                exception = e;
            }
            catch(IOException e) {
                exception = e;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SurfNews.this,
                    android.R.layout.simple_list_item_1, titles);

            listView.setAdapter(adapter);

            // Dismiss progress dialog
            progressDialog.dismiss();
        }
    }
}
