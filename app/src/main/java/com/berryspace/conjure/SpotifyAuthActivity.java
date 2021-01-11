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
import java.util.Calendar;
import java.util.Date;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpotifyAuthActivity extends AppCompatActivity {
    private final static String TAG = "SpotifyAuthActivity";
    private static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    private static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
    private String mAccessToken;
    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_auth);
        getCredentials();
    }

    public void getCredentials(){
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        Log.d(TAG, "opening login activity");
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
        Log.d(TAG, "closing login activity");
    };

    private void getUserId(){
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("SPOTIFYAUTH", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Failed to fetch data: " + e);
                editor.putBoolean("valid", false);
                editor.apply();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    assert response.body() != null;
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    String userId = jsonObject.get("id").toString();
                    Date currentTime = Calendar.getInstance().getTime();

                    Log.d(TAG, "attempting to store Spotify token and user id");
                    editor.putString("spotifyToken", mAccessToken);
                    editor.putString("userId", userId);
                    editor.putBoolean("valid", true);
                    editor.putLong("expiresAt", (currentTime.getTime() + (1000*60*29)));
                    editor.apply();
                    Log.d(TAG, "recorded Spotify token and user id");

                } catch (JSONException error) {
                    Log.d(TAG, error.toString());
                    editor.putBoolean("valid", false);
                    editor.apply();
                }
            }
        });
        finish();
    };

    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(true)
                .setScopes(new String[]{"user-follow-read"})
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        Log.i(TAG, "authorization attempt result code:"+response.getCode());

        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            mAccessToken = response.getAccessToken();
            Log.d(TAG, "Retrieved access token: "+ mAccessToken);
            getUserId();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
     }

    @Override
    protected void onResume() {
        super.onResume();
     }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private Uri getRedirectUri() {
        return new Uri.Builder()
                .scheme(getResources().getString(R.string.com_spotify_sdk_redirect_scheme))
                .authority(getResources().getString(R.string.com_spotify_sdk_redirect_host))
                .build();
    }

}
