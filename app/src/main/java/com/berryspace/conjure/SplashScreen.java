package com.berryspace.conjure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    private final static String TAG = "SplashScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(isNewUser()){
            Log.d(TAG, "new user detected, proceeding to onboarding");
            Intent intent = new Intent(getBaseContext(), OnboardingActivity.class);
            startActivity(intent);
        } else {
            Log.d(TAG, "returning user detected, proceeding to home");
        }
    }

    @Override
    protected void onResume() {
        if(isNewUser()){
            Log.d(TAG, "new user detected, proceeding to onboarding");
            Intent intent = new Intent(getBaseContext(), OnboardingActivity.class);
            startActivity(intent);
        } else {
            Log.d(TAG, "returning user detected, proceeding to home");
            Intent intent = new Intent(getBaseContext(), HomeActivity.class);
            startActivity(intent);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isNewUser(){
        SharedPreferences sharedPref = this.getSharedPreferences("NEWUSER", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("newUser", true);
    }

}
