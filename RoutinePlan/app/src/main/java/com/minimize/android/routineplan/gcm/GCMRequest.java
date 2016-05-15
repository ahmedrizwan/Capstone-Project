package com.minimize.android.routineplan.gcm;

import android.os.AsyncTask;
import com.minimize.android.routineplan.BuildConfig;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import timber.log.Timber;

public class GCMRequest extends AsyncTask<String, Void, String> {

  @Override protected String doInBackground(String... strings) {

    final String API_KEY = BuildConfig.GCM_KEY; // An API key saved on the app server that gives the app server authorized access to Google services
    final String postData = "{ \"to\": \"/topics/" + "sample" + "\", " +
        "\"delay_while_idle\": true, " +
        "\"data\": {\"message\":\"" + strings[0] + "\", " +
        "\"message\": \"Test GCM message from GCMServer-Android\"}}";

    try {
      URL url = new URL("https://android.googleapis.com/gcm/send");
      HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setDoInput(true);
      urlConnection.setDoOutput(true);
      urlConnection.setRequestMethod("POST");
      urlConnection.setRequestProperty("Content-Type", "application/json");
      urlConnection.setRequestProperty("Authorization", "key=" + API_KEY);

      OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
      writer.write(postData);
      writer.flush();
      writer.close();
      outputStream.close();

      int responseCode = urlConnection.getResponseCode();
      InputStream inputStream;
      if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
        inputStream = urlConnection.getInputStream();
      } else {
        inputStream = urlConnection.getErrorStream();
      }
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String temp, response = "";
      while ((temp = bufferedReader.readLine()) != null) {
        response += temp;
      }
      return response;
    } catch (IOException e) {
      e.printStackTrace();
      return e.toString();
    }
  }

  @Override protected void onPostExecute(String message) {
    super.onPostExecute(message);
    Timber.e("onPostExecute : " + message);
  }
}