package com.berryspace.conjure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.FirebaseFunctionsException.Code;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String mUserId;
    private String mAccessToken;
    private FirebaseFunctions mFunctions;
    private TextView instructions;
    private CardView actionSetup;
    private TextView actionButton;
    private TextView status;
    private CardView spotifySetup;
    private ImageView spotifyButton;
    private ImageView progressAlbum;
    private ProgressBar progressBar;
    private JSONObject newArtists = new JSONObject();
    private String firebaseUserId;
    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    Handler handler = new Handler();
    private Integer artistProcessDelay = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mFunctions = FirebaseFunctions.getInstance();

        mUserId = getUser();
        mAccessToken = getToken();
        fetchStats();

        handler.postDelayed(new Runnable(){
            public void run(){
                if(isTokenValid()){
                    Log.d(TAG, "token is valid, checking for unprocessed artists");
                    checkForUnprocessedArtists();
                } else {
                    Log.d(TAG, "token is invalid, checking for unprocessed artists");
                    authenticateWithSpotify();
                }
                handler.postDelayed(this, artistProcessDelay);
            }
        }, artistProcessDelay);

        //checkForFollowedArtists();
        //fetchAlbums();

        progressAlbum = findViewById(R.id.progressAlbum);
        progressBar = findViewById(R.id.progressBar);

//        TextView updateButton = findViewById(R.id.update_button);
//        TextView currentImageDatabaseVersion = findViewById(R.id.currentVersion);
//        TextView availableImageDatabaseVersion = findViewById(R.id.available_version);
//        updateButton.setOnClickListener(v -> {
//            File localImageDatabase = new File(this.getFilesDir(),
//                    (String) availableImageDatabaseVersion.getText());
//            StorageReference storageReference = storage.getReference();
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
        handler.removeCallbacksAndMessages(null);
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
        setupView();
    }

    private void setupView(){
        Log.d(TAG, mUserId + ", " + mAccessToken + ", " + firebaseUserId);
//        if (mUserId == null && mAccessToken == null ) {
//            Log.d(TAG, "setting view to SPOTIFY_SETUP");
//            setView("SPOTIFY_SETUP");
//        } else if (firebaseUserId == null && mUserId != null){
//            Log.d(TAG, "setting view to CONJURE_SETUP");
//            setView("CONJURE_SETUP");
//        }
//        else if (newArtists.length() > 0) {
//            Log.d(TAG, "setting view to UPDATE");
//            setView("UPDATE");
//        } else {
//            Log.d(TAG, "setting view to UP_TO_DATE");
//            setView("UP_TO_DATE");
//        }
    }

    private void setView(String viewType){
//        switch (viewType){
//            case "SPOTIFY_SETUP":
//                instructions.setText(R.string.instructions_spotify_setup);
//                instructions.setVisibility(View.VISIBLE);
//                spotifySetup.setVisibility(View.VISIBLE);
//                spotifyButton.setOnClickListener(v -> {
//                    goToUrl ( "https://play.google.com/store/apps/details?id=com.spotify.music&hl=en_US");
//                });
//                break;
//            case "CONJURE_SETUP":
//                instructions.setText(R.string.instructions_initial_setup);
//                actionButton.setText(R.string.btn_setup);
//                actionSetup.setVisibility(View.VISIBLE);
//                actionButton.setOnClickListener(v->{
//                    getSpotifyFollowedArtists(mAccessToken, mUserId)
//                            .addOnCompleteListener(task -> {
//                                if (!task.isSuccessful()) {
//                                    Exception e = task.getException();
//                                    if (e instanceof FirebaseFunctionsException) {
//                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
//                                        Code code = ffe.getCode();
//                                        Object details = ffe.getDetails();
//                                    }
//                                    Log.w(TAG, "getSpotifyFollowedArtists:onFailure", e);
//                                    return;
//                                }
//                                Object result = task.getResult();
//                                assert result != null;
//                                Log.d(TAG, result.toString());
//                            });
//                });
//                break;
//            case "UPDATE":
//                instructions.setText(R.string.instructions_update);
//                actionButton.setText(R.string.btn_update);
//                actionSetup.setVisibility(View.VISIBLE);
//                actionButton.setOnClickListener(v->{
//                    instructions.setVisibility(View.INVISIBLE);
//                    actionSetup.setVisibility(View.INVISIBLE);
//                    status.setText(R.string.looking_up);
//                    status.setVisibility(View.VISIBLE);
//                    updateConjure(mAccessToken, mUserId)
//                            .addOnCompleteListener(task -> {
//                                if (!task.isSuccessful()) {
//                                    Exception e = task.getException();
//                                    if (e instanceof FirebaseFunctionsException) {
//                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
//                                        Code code = ffe.getCode();
//                                        Object details = ffe.getDetails();
//                                    }
//                                    Log.w(TAG, "updateConjure:onFailure", e);
//                                    instructions.setText(R.string.instructions_update);
//                                    status.setText(R.string.status_error);
//                                    actionSetup.setVisibility(View.VISIBLE);
//                                    instructions.setVisibility(View.VISIBLE);
//                                    status.setVisibility(View.VISIBLE);
//                                    return;
//                                }
//                                Object result = task.getResult();
//                                assert result != null;
//                                Log.d(TAG, result.toString());
//                                instructions.setText(R.string.instructions_up_to_date);
//                                status.setVisibility(View.INVISIBLE);
//                                instructions.setVisibility(View.VISIBLE);
//                            });
//                });
//                break;
//            case "UP_TO_DATE":
//                instructions.setText(R.string.instructions_up_to_date);
//                actionSetup.setVisibility(View.INVISIBLE);
//        }
    }

    private void checkForFollowedArtists(){
        checkFollowedArtists(mAccessToken, mUserId)
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        Code code = ffe.getCode();
                        Object details = ffe.getDetails();
                    }
                    Log.w(TAG, "checkFollowedArtists:onFailure", e);
                    return;
                }
                Object result = task.getResult();
                assert result != null;

                try {
                    String data = result.toString();
                    Log.d(TAG, data);

                    data = data.replace("{artistDiff=", "");
                    data = data.replace("}}", "}");
                    data = data.replace("=", ":");
                    data= data.replace("{", "{\"");
                    data= data.replace("}", "\"}");
                    data= data.replace(":spotify", "\":\"spotify");
                    Log.d(TAG, data);

                    newArtists = new JSONObject(data);
                    setupView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    private Task<Object> getSpotifyFollowedArtists(String token, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("spotifyToken", token);
        data.put("userId", userId);

        return mFunctions
                .getHttpsCallable("getSpotifyFollowedArtists")
                .call(data)
                .continueWith(task -> Objects.requireNonNull(task.getResult()).getData());
    }

    private Task<Object> checkFollowedArtists(String token, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("spotifyToken", token);
        data.put("userId", userId);

        return mFunctions
                .getHttpsCallable("checkFollowedArtists")
                .call(data)
                .continueWith(task -> Objects.requireNonNull(task.getResult()).getData());
    }

    private Task<Object> processArtistAlbumsBatch(String token, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("spotifyToken", token);
        data.put("userId", userId);

        return mFunctions
                .getHttpsCallable("getBatchOfAlbums")
                .call(data)
                .continueWith(task -> Objects.requireNonNull(task.getResult()).getData());
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

    private void fetchStats() {
        final String[] totalArtists = {getResources().getString(R.string.artistStat, " " + "0")};
        final String[] totalAlbums = {getResources().getString(R.string.albumStat, "0")};

        if(mUserId != null) {
            final DocumentReference docRef = firestore.collection("users").document(mUserId);
            docRef.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    firebaseUserId = mUserId;
                    setupView();
                    Object artistTotal = Objects.requireNonNull(snapshot.getData()).get(
                                    "totalArtists");
                    Object albumTotal = Objects.requireNonNull(snapshot.getData()).get(
                                    "totalAlbums");

                    if(artistTotal != null){
                        totalArtists[0] = getResources().getString(R.string.artistStat, " " + artistTotal.toString());
                        Log.d(TAG, totalArtists[0]);
                    }

                    if(albumTotal != null) {
                        totalAlbums[0] = getResources().getString(R.string.albumStat, albumTotal.toString());
                        Log.d(TAG, albumTotal.toString());
                    }

                    TextView artistStat = findViewById(R.id.artistStat);
                    TextView albumsStat = findViewById(R.id.albumStat);

                    artistStat.setText(totalArtists[0]);
                    albumsStat.setText(totalAlbums[0]);
                } else {
                    Log.d(TAG, "No user information found in Firebase - performing initial setup");
                    fetchArtists();
                }
            });
        }
    }

    private void fetchArtists(){
        getSpotifyFollowedArtists(mAccessToken, mUserId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Exception e = task.getException();
                if (e instanceof FirebaseFunctionsException) {
                    FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                    Code code = ffe.getCode();
                    Object details = ffe.getDetails();
                }
                Log.w(TAG, "could not fetch artists for Firebase user: getSpotifyFollowedArtists:onFailure", e);
                return;
            }
            Object result = task.getResult();
            assert result != null;
            Log.d(TAG, result.toString());
        });
    }

    private void processArtists(){
        processArtistAlbumsBatch(mAccessToken, mUserId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Exception e = task.getException();
                if (e instanceof FirebaseFunctionsException) {
                    FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                    Code code = ffe.getCode();
                    Object details = ffe.getDetails();
                }
                Log.w(TAG, "could not process batch of albums: getSpotifyFollowedArtists:onFailure", e);
                return;
            }
            Object result = task.getResult();
            if(result != null) {
                Log.d(TAG, result.toString());
            } else {
                Log.d(TAG, "null result");
            }
        });
    }

    private void checkForUnprocessedArtists(){
        if(mUserId != null) {
            firestore.collection("users")
                .document(mUserId)
                .collection("unprocessedArtists")
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                Log.d(TAG, "processed all artists, stopping recurring processArtists task");
                                handler.removeCallbacksAndMessages(null);
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "still have unprocessed artists");
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    processArtists();
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        }
    }

    private void fetchAlbums() {
        if(mUserId != null) {
            Log.d(TAG, "fetching album pictures");
            firestore.collection("users").document(mUserId).collection("albums")
//                .whereEqualTo("processed", null)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())){
                            String imageUrl = Objects.requireNonNull(document.get("image")).toString();
                                    final Request request = new Request.Builder()
                                            .url(imageUrl)
                                            .build();

                                    mCall = mOkHttpClient.newCall(request);

                                    mCall.enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            Log.d(TAG, "Failed to fetch album image: " + e);
                                        }
                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            if (response.isSuccessful()){
                                                try{
                                                    assert response.body() != null;
                                                    final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                                                    progressBar.incrementProgressBy(1);
                                                    new Handler(Looper.getMainLooper()).post(() -> progressAlbum.setImageBitmap(bitmap));
                                                } catch (Exception e){
                                                    Log.d(TAG, "Could not set progress image to album image: "
                                                    + e.getMessage());
                                                }
                                            } else {
                                                Log.d(TAG, "Failed to fetch album image");
                                            }
                                        }
                                    });
                        }
                    } else {
                        Log.d(TAG, "Error getting album documents: " , task.getException());
                    }
                });
        }
    }
}
