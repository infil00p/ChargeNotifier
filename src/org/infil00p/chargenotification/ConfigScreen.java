package org.infil00p.chargenotification;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConfigScreen extends Activity {

    public static final String LOG_TAG="ChargeConfigScreen";
    public static final String PREFS_NAME="ChargePreferences";
    Context mCtx;
    //This should finally work!
    ChargeNotifier batteryUpdateNotifier;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_screen);
        
        /* This code drains battery!
        batteryUpdateNotifier = new ChargeNotifier();
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryUpdateNotifier, ifilter);
        */
        
        mCtx = this;
        
        //Initial setup
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String savedEmail = settings.getString("email","");
        boolean privacyPolicy = settings.getBoolean("tosApproved", false);
        Log.d(LOG_TAG, "Default value is: " + savedEmail);
        if(privacyPolicy == false)
        {
            
        }
        
        EditText emailField = (EditText) findViewById(R.id.emailText);
        emailField.setText(savedEmail);
        
        //Set up button (I redeclare so I can rip out later)
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Clicked the button, go save now!");
                //Get the text
                EditText emailField = (EditText) findViewById(R.id.emailText);
                Editable email = emailField.getText();
                String rawEmail = email.toString();

                Log.d(LOG_TAG, "Value to be stored is " + rawEmail);
                
                //Save in the settings
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("email", rawEmail);
                editor.commit();
                
                //Do a toast to indicate that we're done
                Toast.makeText(mCtx, R.string.saveText , Toast.LENGTH_SHORT).show();
            }
        });
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.config_screen, menu);
        return true;
    }

}
