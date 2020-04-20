package com.berryspace.muse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button updateButton = findViewById(R.id.update_button);
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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d(TAG, "already on the home screen");
                    break;
                case R.id.navigation_camera:
                    Intent intent = new Intent(this, AugmentedImageActivity.class);
                    intent.putExtra("imageDatabaseVersion", currentImageDatabaseVersion.getText());
                    startActivity(intent);
                    break;
            }
            return true;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        MuseImageDatabase museImageDatabase = new MuseImageDatabase();
        museImageDatabase.fetchImageDatabaseUpdate(this);
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
}
