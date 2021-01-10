package com.berryspace.conjure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import com.berryspace.conjure.adapters.LibraryAdapter;
import com.berryspace.conjure.models.Artist;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LibraryActivity extends AppCompatActivity {
    private static final String TAG = "LibraryActivity";
     private CardView viewLibraryCard;
    private CardView manageLibraryCard;
    private String mAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        mAccessToken = getToken();

        if(isTokenValid()){
            Log.d(TAG, "token is valid");
        } else {
            Log.d(TAG, "token is invalid, retrieving  a new token");
            authenticateWithSpotify();
        }

        viewLibraryCard = findViewById(R.id.tile_view_library);
        viewLibraryCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, LibraryExplorerActivity.class);
            startActivity(intent);
        });

        manageLibraryCard = findViewById(R.id.tile_manage_library);
        manageLibraryCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ArtistSearchActivity.class);
            intent.putExtra("token", mAccessToken);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    break;
                case R.id.navigation_library:
                    Log.d(TAG, "already on the camera screen");
                    break;
                case R.id.navigation_camera:
                    Intent augmentedImageIntent = new Intent(this, AugmentedImageActivity.class);
                    startActivity(augmentedImageIntent);
                    break;
            }
            return true;
        });
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
        Intent intent = new Intent(this, SpotifyAuthActivity.class);
        startActivity(intent);
    };

}
