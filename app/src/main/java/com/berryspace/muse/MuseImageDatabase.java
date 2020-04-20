package com.berryspace.muse;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import java.util.Objects;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

class MuseImageDatabase {
    private static final String TAG = "MuseImageDatabase";

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    void fetchImageDatabaseUpdate(Context context) {
      final DocumentReference docRef = firestore.collection("status").document("update");
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: " + snapshot.getData());
                TextView updateMessage = ((HomeActivity)context).findViewById(R.id.update_available);
                if((boolean) Objects.requireNonNull(snapshot.getData()).get("available")){
                    updateMessage.setText(R.string.update_available);
                    Log.d(TAG, "downloading updated image database");
                } else {
                    updateMessage.setText(R.string.up_to_date);
                    Log.d(TAG, "image database is up to date");
                }
            } else {
                Log.d(TAG, "Current data: null");
            }
        });
    }
}
