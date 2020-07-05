package com.berryspace.conjure;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.FirebaseFunctionsException.Code;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String mUserId;
    private String mAccessToken;
    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mFunctions = FirebaseFunctions.getInstance();

        TextView updateButton = findViewById(R.id.update_button);
        TextView setupButton = findViewById(R.id.btn_setup);
        TextView currentImageDatabaseVersion = findViewById(R.id.currentVersion);
        TextView availableImageDatabaseVersion = findViewById(R.id.available_version);

        updateButton.setOnClickListener(v -> {
            File localImageDatabase = new File(this.getFilesDir(),
                    (String) availableImageDatabaseVersion.getText());
            StorageReference storageReference = storage.getReference();

            storageReference.child("ImageDatabase/" + availableImageDatabaseVersion.getText())
                    .getFile(localImageDatabase)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG,
                                "saved Muse image database to local device : " + availableImageDatabaseVersion.getText());
                        deleteOldImageDatabases((String) availableImageDatabaseVersion.getText());
                        currentImageDatabaseVersion.setText(availableImageDatabaseVersion.getText());
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "failed to save Muse image database to local device: " + e);
                    });
        });

        setupButton.setOnClickListener(v->{
            setupConjure(mAccessToken, mUserId)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        if (e instanceof FirebaseFunctionsException) {
                            FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                            Code code = ffe.getCode();
                            Object details = ffe.getDetails();
                        }
                        Log.w(TAG, "setupConjure:onFailure", e);
                        return;
                    }
                    Object result = task.getResult();
                    assert result != null;
                    Log.d(TAG, result.toString());
                });
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d(TAG, "already on the home screen");
                    break;
                case R.id.navigation_camera:
                    Intent augmentedImageIntent = new Intent(this, AugmentedImageActivity.class);
                    augmentedImageIntent.putExtra("imageDatabaseVersion", currentImageDatabaseVersion.getText());
                    startActivity(augmentedImageIntent);
                    break;
            }
            return true;
        });

        try{
            mUserId = Objects.requireNonNull(getIntent().getStringExtra("userId"));
            mAccessToken = Objects.requireNonNull(getIntent().getStringExtra("token"));
            Log.d(TAG, "Spotify userId: " + mUserId);
            Log.d(TAG, "Spotify token: " + mAccessToken);
        } catch (Error error){
            Log.d(TAG, error.toString());
        }
    }

    private Task<Object> setupConjure(String token, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("spotifyToken", token);
        data.put("userId", userId);

        return mFunctions
            .getHttpsCallable("setupConjure")
            .call(data)
            .continueWith(task -> Objects.requireNonNull(task.getResult()).getData());
    }

    @Override
    protected void onStart() {
        super.onStart();
        MuseImageDatabase museImageDatabase = new MuseImageDatabase();
        museImageDatabase.fetchImageDatabaseUpdate(this);
        fetchStats();
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

    void fetchStats() {
        if(mUserId != null) {
            final DocumentReference docRef = firestore.collection("users").document(mUserId);
            docRef.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    String artistTotal =
                            (String) Objects.requireNonNull(snapshot.getData()).get(
                                    "totalArtists");
                    String albumTotal =
                            (String) Objects.requireNonNull(snapshot.getData()).get(
                                    "totalAlbums");

                    String totalArtists = getResources().getString(R.string.artistStat, artistTotal);
                    String totalAlbums = getResources().getString(R.string.albumStat, albumTotal);

                    TextView artistStat = findViewById(R.id.artistStat);
                    TextView albumsStat = findViewById(R.id.albumStat);

                    artistStat.setText(totalArtists);
                    albumsStat.setText(totalAlbums);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            });
        }
    }

}
