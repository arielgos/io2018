package com.agos.ioextended2018;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.Manifest;
import android.util.Log;

public class Splash extends AppCompatActivity {


    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setCurrentScreen(Splash.this, Splash.class.getName(), null);

    }

    @Override
    public void onResume() {
        super.onResume();
        // Validamos google play services
        if (checkPlayServices()) {
            if (isStoragePermissionGranted()) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        Crashlytics.log(Log.DEBUG, App.tag, "test");
                        Intent intent = new Intent(Splash.this, Login.class);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            intent = new Intent(Splash.this, Main.class);
                        }
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.log(Log.ERROR, App.tag, e.getMessage());
                        Crashlytics.logException(e);
                    }
                }).start();
            } else {
                Snackbar.make(findViewById(R.id.layout_splash), R.string.message_permission, Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(findViewById(R.id.layout_splash), R.string.message_play_services, Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed() {

    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }

}
