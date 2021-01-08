package com.berryspace.conjure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private String mAccessToken;
    Handler handler = new Handler();
    private TextView artistStat;
    private CardView manageLibraryCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAccessToken = getToken();

//        artistStat = findViewById(R.id.artistStat);
//        artistStat.setOnClickListener(v ->{
//            Intent intent = new Intent(this, LibraryActivity.class);
//            intent.putExtra("artists", artistLibrary);
//            startActivity(intent);
//        });
//        albumsStat = findViewById(R.id.albumStat);

        manageLibraryCard = findViewById(R.id.tile_manage_library);
        manageLibraryCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ArtistSearchActivity.class);
            intent.putExtra("token", mAccessToken);
            startActivity(intent);
        });

        if(isTokenValid()){
            Log.d(TAG, "token is valid");
         } else {
            Log.d(TAG, "token is invalid, retrieving  a new token");
            authenticateWithSpotify();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d(TAG, "already on the home screen");
                    break;
                case R.id.navigation_camera:
                    Intent augmentedImageIntent = new Intent(this, AugmentedImageActivity.class);
                    startActivity(augmentedImageIntent);
                    break;
            }
            return true;
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
     }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private String getToken(){
        SharedPreferences sharedPref = this.getSharedPreferences("SPOTIFYAUTH", Context.MODE_PRIVATE);
        return sharedPref.getString("spotifyToken", null);
    }

    private Boolean isTokenValid(){
        Date currentTime = Calendar.getInstance().getTime();
        SharedPreferences sharedPref = this.getSharedPreferences("SPOTIFYAUTH", Context.MODE_PRIVATE);
        return (currentTime.getTime() <= sharedPref.getLong("expiresAt", currentTime.getTime()));
    }

    private void authenticateWithSpotify(){
        Intent intent = new Intent(this, SpotifyAuth.class);
        startActivity(intent);
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

}
