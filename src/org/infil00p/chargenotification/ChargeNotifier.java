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
    private static final String SITE="http://batteries.infil00p.org/charge_notifier/";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "We received a battery event");

        try {
            JSONObject batteryLevel = getBatteryLevels(intent);
            Log.d(TAG, "JSON Object received");
            Log.d(TAG, batteryLevel.toString());
            new UpdateTask().execute(batteryLevel);
        } catch (JSONException e) {
            Log.e(TAG, "Malformed JSON");
        }
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
        
        JSONObject output = new JSONObject();
        output.put("device", value);
        output.put("battery_level", batteryPct);
        output.put("charging", usbCharge || acCharge);
        return output;
    }
    


}
