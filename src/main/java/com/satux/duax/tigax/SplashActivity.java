package com.satux.duax.tigax;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.satux.duax.tigax.ads.Ads;
import com.satux.duax.tigax.ads.GdprHelper;
import com.satux.duax.tigax.utils.CommonUtils;
import com.satux.duax.tigax.utils.SharedPrefsUtils;
import com.satux.duax.tigax.utils.SongsUtils;

import java.util.Arrays;

public class SplashActivity extends AppCompatActivity {

    @NonNull
    String TAG = "SplashActivityLog";
    SharedPrefsUtils pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        pref = new SharedPrefsUtils(this);
        pref.writeSharedPrefs("path", getString(R.string.url_json));
        ProgressBar spinner = findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, (new CommonUtils(this).accentColor(new SharedPrefsUtils(this)))),
                PorterDuff.Mode.MULTIPLY);


        AppLovinSdk.getInstance( this ).setMediationProvider( "max" );
        AppLovinSdk.getInstance(this).getSettings().setTestDeviceAdvertisingIds(Arrays.asList("80784a92-8b3a-47c6-8de5-e290aa8db75c","5bc74b6a-89f9-48b5-ab81-3c5a1eeab1f1"));

        AppLovinSdk.initializeSdk( this, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration)
            {
                // AppLovin SDK is initialized, start loading ads

            }
        } );



        ((TextView) findViewById(R.id.textView10)).setText("Syncing..");


        loadTask(new MCallback() {
            @Override
            public void onAction() {
             Ads ads= new Ads(SplashActivity.this);
             ads.setCustomObjectListener(new Ads.MyCustomObjectListener() {
                 @Override
                 public void onAdsfinish() {
                     Intent i = new Intent(SplashActivity.this, MainActivity.class);
                     startActivity(i);
                     finish();
                 }
             });
             ads.showInters();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                loadTask(new MCallback() {
                    @Override
                    public void onAction() {
                       Ads ads= new Ads(SplashActivity.this);
                       ads.setCustomObjectListener(new Ads.MyCustomObjectListener() {
                           @Override
                           public void onAdsfinish() {
                               Intent i = new Intent(SplashActivity.this, MainActivity.class);
                               startActivity(i);
                               finish();
                           }
                       });
                        ads.showInters();
                    }
                });
                //weGotPermissions();
                // permission was granted, yay! Do the
                // contacts-related task you need to do.

            } else {
                Toast.makeText(this, "Application needs permission to run. Go to Settings > Apps > " + getString(R.string.app_name) + "to allow permission.", Toast.LENGTH_SHORT).show();
                finish();
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void loadTask(MCallback mCallback) {
        SongsUtils songsUtils = new SongsUtils(this);
        songsUtils.sync(mCallback);
    }

}
