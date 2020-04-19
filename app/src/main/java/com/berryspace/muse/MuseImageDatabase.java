package com.berryspace.muse;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

class MuseImageDatabase {
    private static final String TAG = "MuseImageDatabase";

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    void fetchImageDatabaseUpdate(Context context) {
      final DocumentReference docRef = firestore.collection("status").document("update");
      docRef.get().addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
              DocumentSnapshot document = task.getResult();
              assert document != null;
              if (document.exists()) {
                  try {
                      fetchImageDatabase(context, (HashMap) Objects.requireNonNull(document.getData()));
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              } else {
                  Log.d(TAG, "No such document");
              }
          } else {
              Log.d(TAG, "get failed with ", task.getException());
          }
      });
    }

    private void fetchImageDatabase(Context context, HashMap data) throws IOException {
        if((boolean)data.get("available")){
            Log.d(TAG, "downloading updated image database");

            File localImageDatabase = new File(context.getFilesDir(), "muse_image_database.imgdb");
            StorageReference storageReference = storage.getReference();

            storageReference.child("ImageDatabase/muse_image_database_001.imgdb")
                    .getFile(localImageDatabase)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "saved Muse image database to local device : " + taskSnapshot.toString());
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "failed to save Muse image database to local device: " + e);
                    });
        } else {
            Log.d(TAG, "image database is up to date");
        }
    }
}
