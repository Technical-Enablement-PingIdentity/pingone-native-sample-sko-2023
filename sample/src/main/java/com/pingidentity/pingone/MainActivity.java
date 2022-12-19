package com.pingidentity.pingone;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends SampleActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logFCMRegistrationToken();
        /*
         * since Android 13 (API level 33) notifications must be explicitly approved by the
         * user at the runtime.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationsPermission();
        }

        Button b1 = findViewById(R.id.button_pairing_key);
        b1.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PairActivity.class)));

        Button b2 = findViewById(R.id.button_pairing_oidc);
        b2.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OIDCActivity.class)));

    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create()
                .show();
    }

    public SharedPreferences getSharedPreferences(){
        return getSharedPreferences("InternalPrefs", Context.MODE_PRIVATE);
    }

    private void logFCMRegistrationToken(){
        SharedPreferences prefs = getSharedPreferences("InternalPrefs", MODE_PRIVATE);
        String token = prefs.getString("pushToken", null);
        if(token==null){
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {

                    SharedPreferences.Editor editor = getSharedPreferences("InternalPrefs", MODE_PRIVATE).edit();
                    editor.putString("pushToken", task.getResult());
                    editor.apply();
                    Log.d(TAG,"FCM Token = " + task.getResult());
                });
        }else{
            Log.d(TAG,"FCM Token = " + token);
        }
    }

    /*
     * Don't call the following method if your application targets API lower than 33 (Android Tiramisu)
     */
    @RequiresApi(api = 33)
    private void requestNotificationsPermission(){
        /*
         * request the notifications permission if needed
         * NOTE: if user explicitly denied the permission - the system will not present the
         * dialog again, the user will be able to allow the application to show notifications only
         * from the application permissions settings
         */
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            registerActivityResultLauncher().launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private ActivityResultLauncher<String> registerActivityResultLauncher(){
        /*
         * Register the permissions callback, which handles the user's response to the
         * system permissions prompt dialog. For more information see documentation at
         * https://developer.android.com/reference/androidx/activity/result/ActivityResultLauncher
         */
        return registerForActivityResult(new ActivityResultContracts.RequestPermission(), isPermissionGranted -> {
            if (isPermissionGranted) {
                //do nothing
                Log.i(TAG, "Notifications permission granted");
            }else{
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                    /*
                     * the user disallowed the permission (for the first time only):
                     * you can choose to do nothing or present the user the explanation why are you
                     * requesting the permission and what will be the consequences of denying it
                     */
                    Log.i(TAG, "Notifications permission rejected with rationale");
                }else{
                    /*
                     * the user has denied the notifications permission
                     * the application will not be able to show the notifications to the user
                     */
                    Log.i(TAG, "Notifications permission rejected");

                }
            }
        });
    }

}
