package org.infil00p.chargenotification;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.OkHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class UpdateTask extends AsyncTask<JSONObject, Void, String> {

    private OkHttpClient client;
    private static final String SITE="http://battery.infil00p.org/";
    private static final String DATABASE="charge_notifier/";
    private String TAG = "ChargeUpdateTask";

    @Override
    protected String doInBackground(JSONObject... params) {
        JSONObject batteryLevel = params[0];
        client = new OkHttpClient();
        String output = "";
        try 
        {
            String uuid = getUUIDFromServer();
            String full_site = SITE + DATABASE + uuid;
            Log.d(TAG, "Creating new document at: " + full_site);
            URL docUrl = new URL(full_site);
            output = put(docUrl, batteryLevel);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Unable to connect to the Battery Server");
            Log.e(TAG, "IO Exception Message: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "Unable to fetch proper JSON from the Battery Server");
        }
        
        return output;
    }
    
    private String getUUIDFromServer() throws IOException, JSONException {
        String uuidUrl = SITE + "_uuids";
        URL url = new URL(uuidUrl);
        HttpURLConnection connection = client.open(url);
        InputStream in = null;
        try {
            // Read the response.
            in = connection.getInputStream();
            byte[] response = readFully(in);
            String rawJSON = new String(response, "UTF-8");
            Log.d(TAG, "Raw JSON: " + rawJSON);
            JSONObject uuid = new JSONObject(rawJSON);
            return uuid.getJSONArray("uuids").getString(0);
        } finally {
            if (in != null) in.close();
        }
    }
    
    byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
          out.write(buffer, 0, count);
        }
        return out.toByteArray();
      }
    

    private String put(URL url, JSONObject payload) throws IOException {
        byte[] data = payload.toString().getBytes();
        HttpURLConnection connection = client.open(url);
        OutputStream out = null;
        InputStream in = null;
        try {
          // Write the request.
          connection.setRequestMethod("PUT");
          connection.setRequestProperty("Content-Type", "application/json");
          out = connection.getOutputStream();
          out.write(data);
          out.close();

          // Read the response.
          if (connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            Log.e(TAG, "Response Message:" + connection.getResponseMessage());
            in = connection.getInputStream();
            String body = new String(readFully(in));
            Log.e(TAG, "Response Body:" + body);
            throw new IOException("Unexpected HTTP response: "
                + connection.getResponseCode() + " " + connection.getResponseMessage());
          }
          in = connection.getInputStream();
          return readFirstLine(in);
        } finally {
          // Clean up.
          if (out != null) out.close();
          if (in != null) in.close();
        }
      }
    
    private String readFirstLine(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        return reader.readLine();
      }

}
