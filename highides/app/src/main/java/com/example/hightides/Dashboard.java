package com.example.hightides;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Dashboard extends AppCompatActivity implements SensorEventListener {

    // Initialise Sensor variables
    private SensorManager sensorManager;
    private Sensor sensor;
    private long lastUpdateTime;
    private static float SHAKE_THRESHOLD_GRAVITY = 2;

    // Initialise variables
    TextView textViewCondition, textViewTemp, textViewTempMin, textViewTempMax, textViewWaterTemp ,
            textViewSwellHeight, textViewSwellDirection;
    EditText editText;
    Button button;

    // Key and url strings
    private static final String OPEN_WEATHER_MAP_API_KEY = "bf813bdf8f7ee320cf05189f7e3226b4";
    private static final String WORLD_WEATHER_ONLINE_API_KEY = "7ab1469ae5c542a98b4200253181211";

    // OpenWeatherMap base URL
    String baseUrlOWM = "http://api.openweathermap.org/data/2.5/weather?q=";
    // WorldWeatherOnline base URL
    String baseUrlWWO =
            "http://api.worldweatheronline.com/premium/v1/marine.ashx?format=json&key=" +
                    WORLD_WEATHER_ONLINE_API_KEY + "&q=";

    // Initialise URL appended Strings
    String builtUrlOWM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_NORMAL);
        lastUpdateTime = System.currentTimeMillis();

        // Assign views
        textViewCondition = findViewById(R.id.textView_condition);
        textViewTemp = findViewById(R.id.textView_temp);
        textViewTempMin = findViewById(R.id.textView_temp_min);
        textViewTempMax = findViewById(R.id.textView_temp_max);
        textViewWaterTemp = findViewById(R.id.textView_water_temp);
        textViewSwellHeight = findViewById(R.id.textView_swell_height);
        textViewSwellDirection = findViewById(R.id.textView_swell_direction);
        editText = findViewById(R.id.editText_search);
        button = findViewById(R.id.button_search);
    }

    // Logout of app
    public void logOut(View view) {
        Intent intent = new Intent(Dashboard.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // clear the weather report text view
    private void clearTextView() {
        textViewCondition.setText("");
        textViewTemp.setText("");
        textViewTempMin.setText("");
        textViewTempMax.setText("");
        textViewWaterTemp.setText("");
        textViewSwellHeight.setText("");
        textViewSwellDirection.setText("");
    }

    // clear the search location edit text
    private void clearEditText() { editText.setText(""); }

    // Take user input and execute search
    public void searchForLocation(View view) {
        //add validation
        clearTextView();
        String location;
        location = editText.getText().toString();
        if(location.equals("")){
            Toast.makeText(Dashboard.this,
                    "Please enter a search term to get the surf report", Toast.LENGTH_LONG).show();
        }
        else if (isNetworkAvailable()){
            String appendUnitsAndKey = "&units=metric&appid=" + OPEN_WEATHER_MAP_API_KEY;
            builtUrlOWM = baseUrlOWM + location + appendUnitsAndKey;

            Params params = new Params(builtUrlOWM, baseUrlWWO);
            new GetWeatherTask().execute(params);
        }
        else {
            Toast.makeText(Dashboard.this,
                    "Unable to connect. Please check internet connection.", Toast.LENGTH_LONG).show();
            textViewCondition.setText("Unable to connect. Check connection");
        }
    }

    private class GetWeatherTask extends AsyncTask<Params, Void, DataHolder> {
        @Override
        protected DataHolder doInBackground(Params... params) {
            DataHolder report = new DataHolder("", "", "","",
                    "", "", "");

            try {
                // Query both APIs and return full Json
                // Query OpenWeatherMap API
                JSONObject openWeatherTopLevel = readJsonFromWebApi(params[0].firstStr);

                // Build query string using lat & lon values from OpenWeatherMap, pass to marine API
                JSONObject coords = openWeatherTopLevel.getJSONObject("coord");
                String appendedLocation = String.valueOf(coords.getDouble("lat")) + "," +
                        String.valueOf(coords.getDouble("lon"));

                // Query WorldWeatherOnline Marine API
                JSONObject marineWeatherTopLevel = readJsonFromWebApi(
                        params[0].secondStr + appendedLocation);

                // Assign weather values
                JSONObject main = openWeatherTopLevel.getJSONObject("main");
                JSONArray weatherArray = openWeatherTopLevel.getJSONArray("weather");
                JSONObject weatherObj = new JSONObject();

                // Loop through weather json array
                for(int i = 0; i < weatherArray.length(); i++) {
                    weatherObj = weatherArray.getJSONObject(i);
                }

                String condition = "General Condition: " + String.valueOf(
                        weatherObj.getString("main"));
                String temperature = "Temperature: " + String.valueOf(
                        main.getDouble("temp")) + "°C";
                String tempMin = "Min Temperature: " + String.valueOf(
                        main.getDouble("temp_min")) + "°C";
                String tempMax = "Max Temperature: " + String.valueOf(
                        main.getDouble("temp_max")) + "°C";


                // Assign surf report values
                JSONObject data = marineWeatherTopLevel.getJSONObject("data");
                JSONArray marineWeatherJsonArray = data.getJSONArray("weather");
                JSONObject marineWeatherJsonObj = new JSONObject();

                // Loop through marine JSON weather array
                for(int i = 0; i < marineWeatherJsonArray.length(); i++) {
                    marineWeatherJsonObj = marineWeatherJsonArray.getJSONObject(i);
                }

                JSONArray marineHourlyJsonArray = marineWeatherJsonObj.getJSONArray("hourly");
                JSONObject marineHourlyJsonObj = new JSONObject();

                // Loop through marine JSON hourly array
                for(int i = 0; i < marineHourlyJsonArray.length(); i++) {
                    marineHourlyJsonObj = marineHourlyJsonArray.getJSONObject(i);
                }

                String waterTemp = "Water Temperature: " + String.valueOf(
                        marineHourlyJsonObj.getString("waterTemp_C")) + "°C";

                String swellHeight = "Swell Height: " + String.valueOf(
                        marineHourlyJsonObj.getString("swellHeight_ft")) + " ft";

                String swellDirection = "Swell Direction: " + String.valueOf(
                        marineHourlyJsonObj.getString("swellDir16Point"));

                report = new DataHolder(condition, temperature, tempMin, tempMax, waterTemp, swellHeight,
                        swellDirection);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            // Return report object
            return report;
        }

        @Override
        protected void onPostExecute(DataHolder rep) {
            if(rep.str1.equals("") && rep.str2.equals("") && rep.str3.equals("") & rep.str4.equals("")
                    && rep.str5.equals("")  && rep.str6.equals("")  && rep.str7.equals("")  ) {

                Toast.makeText(Dashboard.this, "Please enter a valid location",
                        Toast.LENGTH_LONG).show();
            }
            else {
                textViewCondition.setText(rep.str1);
                textViewTemp.setText(rep.str2);
                textViewTempMin.setText(rep.str3);
                textViewTempMax.setText(rep.str4);
                textViewWaterTemp.setText(rep.str5);
                textViewSwellHeight.setText(rep.str6);
                textViewSwellDirection.setText(rep.str7);
            }
        }
    }

    public JSONObject readJsonFromWebApi(String urlQueryString) {
        URL url = null;
        JSONObject jsonObject = new JSONObject();

        try {
            url = new URL(urlQueryString);
        }
        catch(MalformedURLException e) {
            e.printStackTrace();
        }

        StringBuilder builder = new StringBuilder();
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream content = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
            String inputString;
            while ((inputString = bufferedReader.readLine()) != null) {
                builder.append(inputString);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        try {
            jsonObject = new JSONObject(builder.toString());
        }
        catch(JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private static class Params {
        String firstStr, secondStr;

        Params(String firstStr, String secondStr) {
            this.firstStr = firstStr;
            this.secondStr = secondStr;
        }
    }

    private static class DataHolder {
        String str1, str2, str3, str4, str5, str6, str7;

        DataHolder(String str1, String str2, String str3, String str4, String str5 ,String str6, String str7) {
            this.str1 = str1;
            this.str2 = str2;
            this.str3 = str3;
            this.str4 = str4;
            this.str5 = str5;
            this.str6 = str6;
            this.str7 = str7;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;
        float gForce = (float)Math.sqrt(gX * gY * gY + gZ * gZ);
        long currentTime = System.currentTimeMillis();
        if(gForce >= SHAKE_THRESHOLD_GRAVITY)
        {
            if (currentTime - lastUpdateTime < 200) {
                return;
            }
            lastUpdateTime = currentTime;
            clearTextView();
            clearEditText();
            Toast.makeText(this,  "All Clear!", Toast.LENGTH_LONG).show();
        }
    }

    public void goToSurfNewsFeed(View view) {
       Intent intent = new Intent(Dashboard.this, SurfNews.class);
       startActivity(intent);
    }
}