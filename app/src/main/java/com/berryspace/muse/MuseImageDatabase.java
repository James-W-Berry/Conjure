package com.berryspace.muse;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.Objects;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

class MuseImageDatabase {
    private static final String TAG = "MuseImageDatabase";
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String currentVersion;

    void fetchImageDatabaseUpdate(Context context) {
        final DocumentReference docRef = firestore.collection("status").document("update");
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                TextView updateMessage =
                        ((HomeActivity) context).findViewById(R.id.update_available_label);
                Button updateButton = ((HomeActivity) context).findViewById(R.id.update_button);
                TextView currentImageDatabaseVersion =
                        ((HomeActivity) context).findViewById(R.id.currentVersion);
                TextView availableImageDatabaseVersion =
                        ((HomeActivity) context).findViewById(R.id.available_version);

                String masterImageDatabaseName =
                        (String) Objects.requireNonNull(snapshot.getData()).get(
                                "masterImageDatabase");

                if (isFilePresent(masterImageDatabaseName, context)) {
                    updateMessage.setText(context.getResources().getString(R.string.update_available_label, "Your image database is up to date"));
                    availableImageDatabaseVersion.setText(context.getResources().getString(R.string.available_version, ""));
                    currentImageDatabaseVersion.setText(masterImageDatabaseName);
                    updateButton.setVisibility(View.GONE);
                    Log.d(TAG, "image database is up to date");
                } else {
                    updateMessage.setText(context.getResources().getString(R.string.update_available_label, "Updated image database is available"));
                    availableImageDatabaseVersion.setText(context.getResources().getString(R.string.available_version, masterImageDatabaseName));
                    currentImageDatabaseVersion.setText(context.getResources().getString(R.string.current_version, currentVersion));
                    updateButton.setVisibility(View.VISIBLE);
                    Log.d(TAG, "there is a newer image database available");
                }
            } else {
                Log.d(TAG, "Current data: null");
            }
        });
    }

    private boolean isFilePresent(String fileName, Context context) {
        Log.d(TAG, "Looking for " + fileName + " in local files");
        File[] localFiles = context.getFilesDir().listFiles();
        assert localFiles != null;
        for (File file : localFiles) {
            if (file.getName().equals(fileName)) {
                Log.d(TAG, "Found " + fileName + " in local files");
                return true;
            } else if (file.getName().contains("muse_image_database")) {
                currentVersion = file.getName();
            }
        }
        return false;
    }


}
