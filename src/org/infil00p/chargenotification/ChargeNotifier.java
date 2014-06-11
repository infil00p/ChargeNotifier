package org.infil00p.chargenotification;


import java.text.DateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.OkHttpClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.provider.Settings;
import android.util.Log;

public class ChargeNotifier extends BroadcastReceiver {

    OkHttpClient client = new OkHttpClient();
    
    private static final String TAG="ChargeNotifier";
    public static final String PREFS_NAME="ChargePreferences";
    
    private static final String SITE="http://batteries.infil00p.org/charge_notifier/";
    private Context mCtx;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "We received a battery event");
        mCtx = context;
        
        SharedPreferences settings = mCtx.getSharedPreferences(PREFS_NAME, 0);
        String savedEmail = settings.getString("email","");
        
        if(savedEmail.length() > 0)
        {
            try {
                JSONObject batteryLevel = getBatteryLevels(intent, savedEmail);
                int battery_level = batteryLevel.getInt("battery_level");
                if(battery_level == 100 || battery_level < 15)
                {
                    //Only go on the Internet when we are fully charged, or when we are below 15%
                    //Plug/Unplug events will be negative, so they always go online
                    Log.d(TAG, "JSON Object received");
                    Log.d(TAG, batteryLevel.toString());
                    new UpdateTask().execute(batteryLevel);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Malformed JSON");
            }
        }
    }

    
    private JSONObject getBatteryLevels(Intent batteryStatus, String savedEmail) throws JSONException {
        
        String messagePurpose;
        
        if(batteryStatus.getAction().equals(Intent.ACTION_BATTERY_LOW))
        {
            messagePurpose = "battery_low";
        }
        else if(batteryStatus.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
        {
            messagePurpose = "battery_changed";
        }
        else if(batteryStatus.getAction().equals(Intent.ACTION_BATTERY_OKAY))
        {
            messagePurpose = "battery_okay";
        }
        else if(batteryStatus.getAction().equals(Intent.ACTION_POWER_CONNECTED))
        {
            messagePurpose = "power_connected";
        }
        else if(batteryStatus.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
        {
            messagePurpose = "power_disconnected";
        }
        else
        {
            messagePurpose = "WTF?";
        }
        
        
        
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        
        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        
        //It's null, so that we don't drain the battery every time we do this!
        Intent batteryIntent = mCtx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);

        int batteryPct = (int) (((float)level / (float)scale) * 100);
        Log.d(TAG, "Level: " + Integer.toString(level) + ", Scale:" + Integer.toString(scale));
        Log.d(TAG, "Battery Percent:" + Float.toString(batteryPct));
        
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;
        String codeName = android.os.Build.PRODUCT;
        String deviceId = Settings.Secure.getString(mCtx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        String value = manufacturer + " " + model + "(" + codeName + ")";
        
        //Get the Date/Time of the Update, since Couch doesn't have this.
        DateFormat df = DateFormat.getDateTimeInstance();
        String date = df.format(new Date());
        
        
        JSONObject output = new JSONObject();
        output.put("device", value);
        output.put("battery_level", batteryPct);
        output.put("charging", usbCharge || acCharge);
        output.put("deviceId", deviceId);
        output.put("email", savedEmail);
        output.put("message_type", messagePurpose);
        output.put("datetime", date);
        
        return output;
    }
    


}
