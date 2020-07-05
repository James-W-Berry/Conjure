package com.berryspace.conjure;

import android.content.Intent;
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
    private static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    private static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
    private String mAccessToken;
    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private final static String TAG = "SplashScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSpotifyId();
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    public void getUserId(){
        final String[] userId = new String[1];
        userId[0] = "";

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
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    assert response.body() != null;
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    userId[0] = jsonObject.get("id").toString();

                    Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                    intent.putExtra("userId", userId[0]);
                    intent.putExtra("token", mAccessToken);
                    startActivity(intent);

                } catch (JSONException error) {
                    Log.d(TAG, error.toString());
                }
            }
        });
    };

    public void getSpotifyId(){
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
    };

    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(true)
                .setScopes(new String[]{"user-follow-read"})
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            mAccessToken = response.getAccessToken();
            getUserId();
        }
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private Uri getRedirectUri() {
        return new Uri.Builder()
                .scheme(getString(R.string.com_spotify_sdk_redirect_scheme))
                .authority(getString(R.string.com_spotify_sdk_redirect_host))
                .build();
    }
}
