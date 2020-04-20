package com.berryspace.muse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button updateButton = findViewById(R.id.update_button);
        updateButton.setOnClickListener(v -> {
            File localImageDatabase = new File(this.getFilesDir(), "muse_image_database.imgdb");
            StorageReference storageReference = storage.getReference();

            storageReference.child("ImageDatabase/muse_image_database_001.imgdb")
                    .getFile(localImageDatabase)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "saved Muse image database to local device : " + taskSnapshot.toString());
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
}
