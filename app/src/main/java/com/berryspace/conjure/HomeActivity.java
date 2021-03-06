package com.berryspace.conjure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.HashMap;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private Handler handler = new Handler();
    private ConstraintLayout libraryStats;
    private TextView artistStat;
    private TextView albumStat;
    private TextView lastScannedAlbum;
    private TextView lastScannedArtist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        libraryStats = findViewById(R.id.library_stats_layout);
        artistStat = findViewById(R.id.artist_stat);
        albumStat = findViewById(R.id.album_stat);
        lastScannedAlbum = findViewById(R.id.last_played_album_0);
        lastScannedArtist = findViewById(R.id.last_played_artist_0);
        lastScannedAlbum.setSelected(true);
        lastScannedArtist.setSelected(true);

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

        HashMap<String, String> lastScanned = fetchLastScanned();
        lastScannedArtist.setText(lastScanned.get("artist"));
        lastScannedAlbum.setText(lastScanned.get("album"));

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
                case R.id.navigation_library:
                    Intent libraryIntent = new Intent(this, LibraryActivity.class);
                    startActivity(libraryIntent);
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

    @Override
    protected void onResume() {
        super.onResume();

    }

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

    private HashMap<String, String> fetchLastScanned(){
        HashMap<String, String> lastScanned = new HashMap<>();
        SharedPreferences sharedPref = this.getSharedPreferences("LASTSCANNED", Context.MODE_PRIVATE);
        lastScanned.put("album", sharedPref.getString("album", "No albums scanned yet"));
        lastScanned.put("artist", sharedPref.getString("artist", ""));
        return lastScanned;
    }

}
