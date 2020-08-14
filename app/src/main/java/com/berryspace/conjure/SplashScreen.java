package com.berryspace.conjure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
