package org.infil00p.chargenotification;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.OkHttpClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.BatteryManager;
import android.util.Log;

public class ChargeNotifier extends BroadcastReceiver {

    OkHttpClient client = new OkHttpClient();
    
    private static final String TAG="ChargeNotifier";
    private static final String SITE="http://batteries.infil00p.org/";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "We received a battery event");
        JSONObject batteryLevel = getBatteryLevels(intent);
        String UUID = getUUIDFromServer();
        batteryLevel.
    }

    
    private JSONObject getBatteryLevels(Intent batteryStatus) throws JSONException {
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;
        
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;
        String codeName = android.os.Build.PRODUCT;
        String value = manufacturer + " " + model + "(" + codeName + ")";
        String uuid;
        try {
            uuid = getUUIDFromServer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        JSONObject output = new JSONObject();
        output.put("device", value);
        output.put("battery_level", batteryPct);
        output.put("charging", usbCharge || acCharge);
        output.put("id", uuid);
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
    

    private String post(URL url, byte[] body) throws IOException {
        HttpURLConnection connection = client.open(url);
        OutputStream out = null;
        InputStream in = null;
        try {
          // Write the request.
          connection.setRequestMethod("POST");
          out = connection.getOutputStream();
          out.write(body);
          out.close();

          // Read the response.
          if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
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
