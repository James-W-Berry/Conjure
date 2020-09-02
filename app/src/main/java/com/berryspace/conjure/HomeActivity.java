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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.FirebaseFunctionsException.Code;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
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
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class HomeActivity extends AppCompatActivity {
    private String TAG = "HomeActivity";
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String mUserId;
    private String mAccessToken;
    private FirebaseFunctions mFunctions;

    private ImageView progressAlbum;
    private ProgressBar progressBar;
    private AppCompatTextView progressLabel;
    private JSONObject newArtists = new JSONObject();
    private String firebaseUserId;
    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    Handler handler = new Handler();
    private Integer artistProcessDelay = 10000;

    private static final String ADMOB_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";

    private Button refresh;
    private CheckBox startVideoAdsMuted;
    private TextView videoStatus;
    private UnifiedNativeAd nativeAd;

    private TextView artistStat;
    private TextView albumsStat;

    private String[] artistLibrary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //setupAds();

        mFunctions = FirebaseFunctions.getInstance();

        mUserId = getUser();
        mAccessToken = getToken();
        artistStat = findViewById(R.id.artistStat);
        artistStat.setOnClickListener(v ->{
            Intent intent = new Intent(this, LibraryActivity.class);
            intent.putExtra("artists", artistLibrary);
            startActivity(intent);
        });
        albumsStat = findViewById(R.id.albumStat);
        fetchStats();
        fetchLibraryArtists();


        handler.postDelayed(new Runnable(){
            public void run(){
                if(isTokenValid()){
                    Log.d(TAG, "token is valid, checking for unprocessed artists");
                    checkForUnprocessedArtists();
                } else {
                    Log.d(TAG, "token is invalid, retrieving  a new token");
                    authenticateWithSpotify();
                }
//                handler.postDelayed(this, artistProcessDelay);
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
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        handler.removeCallbacksAndMessages(null);
    }

//    private void setupAds(){
//        // Initialize the Mobile Ads SDK.
//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {}
//        });
//
//        refresh = findViewById(R.id.btn_refresh);
//        startVideoAdsMuted = findViewById(R.id.cb_start_muted);
//        videoStatus = findViewById(R.id.tv_video_status);
//
//        refresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View unusedView) {
//                refreshAd();
//            }
//        });
//
//        refreshAd();
//    }
//
//    /**
//     * Populates a {@link UnifiedNativeAdView} object with data from a given
//     * {@link UnifiedNativeAd}.
//     *
//     * @param nativeAd the object containing the ad's assets
//     * @param adView          the view to be populated
//     */
//    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
//        // Set the media view.
//        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));
//
//        // Set other ad assets.
//        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
//        adView.setBodyView(adView.findViewById(R.id.ad_body));
//        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
//        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
//        adView.setPriceView(adView.findViewById(R.id.ad_price));
//        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
//        adView.setStoreView(adView.findViewById(R.id.ad_store));
//        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
//
//        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
//        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
//        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
//
//        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
//        // check before trying to display them.
//        if (nativeAd.getBody() == null) {
//            adView.getBodyView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getBodyView().setVisibility(View.VISIBLE);
//            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
//        }
//
//        if (nativeAd.getCallToAction() == null) {
//            adView.getCallToActionView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getCallToActionView().setVisibility(View.VISIBLE);
//            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
//        }
//
//        if (nativeAd.getIcon() == null) {
//            adView.getIconView().setVisibility(View.GONE);
//        } else {
//            ((ImageView) adView.getIconView()).setImageDrawable(
//                    nativeAd.getIcon().getDrawable());
//            adView.getIconView().setVisibility(View.VISIBLE);
//        }
//
//        if (nativeAd.getPrice() == null) {
//            adView.getPriceView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getPriceView().setVisibility(View.VISIBLE);
//            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
//        }
//
//        if (nativeAd.getStore() == null) {
//            adView.getStoreView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getStoreView().setVisibility(View.VISIBLE);
//            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
//        }
//
//        if (nativeAd.getStarRating() == null) {
//            adView.getStarRatingView().setVisibility(View.INVISIBLE);
//        } else {
//            ((RatingBar) adView.getStarRatingView())
//                    .setRating(nativeAd.getStarRating().floatValue());
//            adView.getStarRatingView().setVisibility(View.VISIBLE);
//        }
//
//        if (nativeAd.getAdvertiser() == null) {
//            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
//        } else {
//            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
//            adView.getAdvertiserView().setVisibility(View.VISIBLE);
//        }
//
//        // This method tells the Google Mobile Ads SDK that you have finished populating your
//        // native ad view with this native ad.
//        adView.setNativeAd(nativeAd);
//
//        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
//        // have a video asset.
//        VideoController vc = nativeAd.getVideoController();
//
//        // Updates the UI to say whether or not this ad has a video asset.
//        if (vc.hasVideoContent()) {
//            videoStatus.setText(String.format(Locale.getDefault(),
//                    "Video status: Ad contains a %.2f:1 video asset.",
//                    vc.getAspectRatio()));
//
//            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
//            // VideoController will call methods on this object when events occur in the video
//            // lifecycle.
//            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
//                @Override
//                public void onVideoEnd() {
//                    // Publishers should allow native ads to complete video playback before
//                    // refreshing or replacing them with another ad in the same UI location.
//                    refresh.setEnabled(true);
//                    videoStatus.setText("Video status: Video playback has ended.");
//                    super.onVideoEnd();
//                }
//            });
//        } else {
//            videoStatus.setText("Video status: Ad does not contain a video asset.");
//            refresh.setEnabled(true);
//        }
//    }
//
//    /**
//     * Creates a request for a new native ad based on the boolean parameters and calls the
//     * corresponding "populate" method when one is successfully returned.
//     *
//     */
//    private void refreshAd() {
//        refresh.setEnabled(false);
//
//        AdLoader.Builder builder = new AdLoader.Builder(this, ADMOB_AD_UNIT_ID);
//
//        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
//            // OnUnifiedNativeAdLoadedListener implementation.
//            @Override
//            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
//                // If this callback occurs after the activity is destroyed, you must call
//                // destroy and return or you may get a memory leak.
//                if (isDestroyed()) {
//                    unifiedNativeAd.destroy();
//                    return;
//                }
//                // You must call destroy on old ads when you are done with them,
//                // otherwise you will have a memory leak.
//                if (nativeAd != null) {
//                    nativeAd.destroy();
//                }
//                nativeAd = unifiedNativeAd;
//                FrameLayout frameLayout =
//                        findViewById(R.id.fl_adplaceholder);
//                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
//                        .inflate(R.layout.advertisement, null);
//                populateUnifiedNativeAdView(unifiedNativeAd, adView);
//                frameLayout.removeAllViews();
//                frameLayout.addView(adView);
//            }
//
//        });
//
//        VideoOptions videoOptions = new VideoOptions.Builder()
//                .setStartMuted(startVideoAdsMuted.isChecked())
//                .build();
//
//        NativeAdOptions adOptions = new NativeAdOptions.Builder()
//                .setVideoOptions(videoOptions)
//                .build();
//
//        builder.withNativeAdOptions(adOptions);
//
//        AdLoader adLoader =
//            builder
//                .withAdListener(
//                    new AdListener() {
//                        @Override
//                        public void onAdFailedToLoad(LoadAdError loadAdError) {
//                            refresh.setEnabled(true);
//                            String error =
//                                    String.format(
//                                            "domain: %s, code: %d, message: %s",
//                                            loadAdError.getDomain(),
//                                            loadAdError.getCode(),
//                                            loadAdError.getMessage());
//                            Toast.makeText(
//                                    HomeActivity.this,
//                                    "Failed to load native ad with error " + error,
//                                    Toast.LENGTH_SHORT)
//                                    .show();
//                        }
//                    })
//                .build();
//
//        adLoader.loadAd(new AdRequest.Builder().build());
//
//        videoStatus.setText("");
//    }


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
                    Object artistTotal = Objects.requireNonNull(snapshot.getData()).get("totalArtists");
                    Object albumTotal = Objects.requireNonNull(snapshot.getData()).get("totalAlbums");

                    if(artistTotal != null){
                        totalArtists[0] = getResources().getString(R.string.artistStat, " " + artistTotal.toString());
                        Log.d(TAG, totalArtists[0]);
                    }

                    if(albumTotal != null) {
                        totalAlbums[0] = getResources().getString(R.string.albumStat, albumTotal.toString());
                        Log.d(TAG, albumTotal.toString());
                    }

                    artistStat.setText(totalArtists[0]);
                    albumsStat.setText(totalAlbums[0]);
                } else {
                    Log.d(TAG, "No user information found in Firebase - performing initial setup");
                    fetchArtists();
                }
            });
        }
    }


    private void fetchLibraryArtists() {
        if(mUserId != null) {
            final DocumentReference docRef = firestore.collection("users").document(mUserId).collection("library").document("artists");

            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Object artists = document.getData().get("artists");
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            String jsonString = objectMapper.writeValueAsString(artists);
                            JSONObject json = new JSONObject(jsonString);
                            Iterator<String> keys = json.keys();
                            int i = 0;
                            String[] library = new String[json.length()];

                            while(keys.hasNext()) {
                                String key = keys.next();
                                if (json.get(key) instanceof JSONObject) {
                                    library[i] = ((JSONObject) json.get(key)).get("name").toString();
                                    ++i;
                                }
                            }

                            String[] sorted = Stream.of(library).sorted().toArray(String[]::new);
                            artistLibrary = sorted;
                        } catch (JsonProcessingException | JSONException e) {
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
                                progressLabel = findViewById(R.id.progressLabel);
                                progressLabel.setText(R.string.progress_complete);
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
