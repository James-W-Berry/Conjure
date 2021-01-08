package com.berryspace.conjure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private String mAccessToken;
    private Handler handler = new Handler();
    private CardView manageLibraryCard;
    private ConstraintLayout libraryStats;
    private TextView artistStat;
    private TextView albumStat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAccessToken = getToken();

        libraryStats = findViewById(R.id.library_stats_layout);
        artistStat = findViewById(R.id.artist_stat);
        albumStat = findViewById(R.id.album_stat);

        HashMap<String, Integer> stats = fetchStats();
        if (stats.get("artistCount") > 0 && stats.get("albumCount") > 0){
            String artistText =  getResources().getString(R.string.artist_stat, Objects.requireNonNull(stats.get("artistCount")).toString());
            artistStat.setText(artistText);
            String albumText =  getResources().getString(R.string.album_stat, Objects.requireNonNull(stats.get("albumCount")).toString());
            albumStat.setText(albumText);
            libraryStats.setVisibility(View.VISIBLE);
        } else {
            libraryStats.setVisibility(View.GONE);
        }

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
    protected void onStart() {
        super.onStart();
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

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    private HashMap<String, Integer> fetchStats(){
        HashMap<String, Integer> retrievedStats = new HashMap<>();
        SharedPreferences sharedPref = this.getSharedPreferences("LIBRARYSTATS", Context.MODE_PRIVATE);
        retrievedStats.put("artistCount", sharedPref.getInt("artistCount", 0));
        retrievedStats.put("albumCount", sharedPref.getInt("albumCount", 0));
        return retrievedStats;
    }

}
