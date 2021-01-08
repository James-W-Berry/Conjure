package com.berryspace.conjure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import okhttp3.Call;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String mUserId;
    private String mAccessToken;
    private FirebaseFunctions mFunctions;
    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    Handler handler = new Handler();
    private Integer artistProcessDelay = 60000;

    private TextView artistStat;
    private TextView albumsStat;
    private String[] artistLibrary;
    private Button updateLibraryButton;

    private CardView manageLibraryCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mFunctions = FirebaseFunctions.getInstance();

        // get local app storage for Spotify credentials
        mUserId = getUser();
        mAccessToken = getToken();

        // populate user library stats
        artistStat = findViewById(R.id.artistStat);
        artistStat.setOnClickListener(v ->{
            Intent intent = new Intent(this, LibraryActivity.class);
            intent.putExtra("artists", artistLibrary);
            startActivity(intent);
        });
        albumsStat = findViewById(R.id.albumStat);

        manageLibraryCard = findViewById(R.id.tile_manage_library);
        manageLibraryCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecognizerActivity.class);
            intent.putExtra("token", mAccessToken);
            startActivity(intent);
        });

        updateLibraryButton = findViewById(R.id.update_library_button);
//        updateLibraryButton.setOnClickListener(v -> {
//            ImageDatabaseService imageDatabaseService = new ImageDatabaseService(getBaseContext());
//            String path = getBaseContext().getFilesDir().toString()+"/images";
//            File directory = new File(path);
//            File[] files = directory.listFiles();
//            imageDatabaseService.addImagesToDatabase(files);
//        });

        if(isTokenValid()){
            Log.d(TAG, "token is valid");
         } else {
            Log.d(TAG, "token is invalid, retrieving  a new token");
            authenticateWithSpotify();
        }

        handler.postDelayed(() -> {
            if(isTokenValid()){
                Log.d(TAG, "token is valid");
                //checkForUnprocessedArtists();
            } else {
                Log.d(TAG, "token is invalid, retrieving  a new token");
                authenticateWithSpotify();
            }
         }, artistProcessDelay);

//        TextView updateButton = findViewById(R.id.update_button);
//        TextView currentImageDatabaseVersion = findViewById(R.id.currentVersion);
//        TextView availableImageDatabaseVersion = findViewById(R.id.available_version);
//        updateButton.setOnClickListener(v -> {
//            File localImageDatabase = new File(this.getFilesDir(),
//                    (String) availableImageDatabaseVersion.getText());
           //StorageReference storageReference = storage.getReference();
//
//            storageReference.child("ImageDatabase/" + availableImageDatabaseVersion.getText())
//                    .getFile(localImageDatabase)
//                    .addOnSuccessListener(taskSnapshot -> {
//                        Log.d(TAG,
//                                "saved Muse image database to local device : " + availableImageDatabaseVersion.getText());
//                        deleteOldImageDatabases((String) availableImageDatabaseVersion.getText());
//                        currentImageDatabaseVersion.setText(availableImageDatabaseVersion.getText());
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.d(TAG, "failed to save Muse image database to local device: " + e);
//                    });
//        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d(TAG, "already on the home screen");
                    break;
                case R.id.navigation_camera:
                    Intent augmentedImageIntent = new Intent(this, AugmentedImageActivity.class);
                    //augmentedImageIntent.putExtra("imageDatabaseVersion", currentImageDatabaseVersion.getText());
                    augmentedImageIntent.putExtra("imageDatabaseVersion", "1.0");
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

    private String getUser(){
        SharedPreferences sharedPref = this.getSharedPreferences("SPOTIFYAUTH", Context.MODE_PRIVATE);
        return sharedPref.getString("userId", null);
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
        //MuseImageDatabase museImageDatabase = new MuseImageDatabase();
        //museImageDatabase.fetchImageDatabaseUpdate(this);
    }

    private void deleteOldImageDatabases(String fileName) {
        Log.d(TAG, "Cleaning up old image databases");
        File[] localFiles = getBaseContext().getFilesDir().listFiles();
        assert localFiles != null;
        for (File file : localFiles) {
            if (file.getName().equals(fileName)) {
                Log.d(TAG, "Keeping current image database " + fileName);
            } else if (file.getName().contains("muse_image_database")) {
                if (file.delete()) {
                    Log.d(TAG, "successfully removed old image database " + file.getName());
                } else {
                    Log.d(TAG, "could not remove old image database " + file.getName());
                }
            }
        }
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

}
