package com.berryspace.conjure;

import android.Manifest.permission;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector.ConnectionListener;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.types.Track;

import org.json.JSONException;
import org.json.JSONObject;

public class AugmentedImageActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "AugmentedImageActivity";
    private String currentlyPlaying = "";
    private ArFragment arFragment;
    private ImageView fitToScanView;
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();
    private static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
    private static final String SPOTIFY_REDIRECT_URI = "com.berryspace.conjure://callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ImageView pauseTrack;
    private ImageView resumeTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, CLIENT_ID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (hasPermissions()) {
            Log.i(TAG, "User already granted permissions for camera access");

        } else {
            requestConjurePermissions();
        }


        ImageView previousTrack = findViewById(R.id.previous_track);
        previousTrack.setOnClickListener(v -> {
            playPreviousTrack();
        });

        ImageView nextTrack = findViewById(R.id.next_track);
        nextTrack.setOnClickListener(v -> {
            playNextTrack();
        });

        pauseTrack = findViewById(R.id.pause);
        pauseTrack.setOnClickListener(v->{
            pauseTrack();
        });

        resumeTrack = findViewById(R.id.play);
        resumeTrack.setOnClickListener(v->{
            resumeTrack();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    break;
                case R.id.navigation_camera:
                    Log.d(TAG, "already on the camera screen");
                    break;
                case R.id.navigation_library:
                    Intent libraryIntent = new Intent(this, LibraryActivity.class);
                    startActivity(libraryIntent);
                    break;
            }
            return true;
        });

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);


        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(SPOTIFY_REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        Log.d("AugmentedImageActivity", "Connecting to Spotify");

        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        SpotifyAppRemote.connect(this, connectionParams,
                new ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("AugmentedImageActivity", "Connected to Spotify");
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        if (error instanceof NotLoggedInException || error instanceof UserNotAuthorizedException) {
                            Log.d("AugmentedImageActivity", "Could not connect to Spotify");
                        } else if (error instanceof CouldNotFindSpotifyApp) {
                            // Show button to download Spotify
                            Log.d("AugmentedImageActivity", "Could not find Spotify app on the device");
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime)   {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame, just return.
        if (frame == null) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);

            for (AugmentedImage augmentedImage : updatedAugmentedImages) {
                switch (augmentedImage.getTrackingState()) {
                    case PAUSED:
                        // When an image is in PAUSED state, but the camera is not PAUSED, it has
                        // been detected,  but not yet tracked.

                        // Play album from detected image
                        if(!currentlyPlaying.equals(augmentedImage.getName())) {
                            Log.i(TAG, "was playing: "+ currentlyPlaying);
                            currentlyPlaying = augmentedImage.getName();
                            Log.i(TAG, "now playing: "+ currentlyPlaying);
                            String text = "No album detected yet";
                            String path = getBaseContext().getFilesDir().toString()+ "/library/detectable.json";
                            File file = new File(path);
                            JSONObject libraryObject = new JSONObject();
                            if (!file.exists()){
                                text = "Now Playing: unknown";
                            } else {
                                try{
                                    libraryObject = readLibraryFile(file);
                                    JSONObject detectedAlbum = (JSONObject) libraryObject.get(augmentedImage.getName());
                                    text = "Now Playing: " + detectedAlbum.get("name") + " by " + detectedAlbum.get("artist");
                                    saveLastScanned(detectedAlbum.get("name").toString(), detectedAlbum.get("artist").toString());
                                } catch (IOException | JSONException exception){
                                    Log.i(TAG, exception.getMessage());
                                }
                            }
                            TextView heading = findViewById(R.id.nowPlaying);
                            heading.setText(text);
                            stopMusic();
                            String uri = "spotify:album:" + augmentedImage.getName().replace(".png", "");
                            playMusic(uri);
                        }
                        break;
                    case TRACKING:
                        // Have to switch to UI Thread to update View.
                        //fitToScanView.setVisibility(View.GONE);

                        // Create a new anchor for newly found images.
//                        AugmentedImageNode node = new AugmentedImageNode(this);
//                        node.setImage(augmentedImage);
//                        augmentedImageMap.put(augmentedImage, node);
//                        break;

                    case STOPPED:
                        augmentedImageMap.remove(augmentedImage);
                        break;
                }
            }

    }

    private void playMusic(String spotifyUri) {
        Log.i(TAG + " playing: ", spotifyUri);
        mSpotifyAppRemote.getPlayerApi().play(spotifyUri);
        pauseTrack.setVisibility(View.VISIBLE);
        resumeTrack.setVisibility(View.GONE);
    }

    private void stopMusic() {
        mSpotifyAppRemote.getPlayerApi().pause();
    }

    private void playPreviousTrack(){
        mSpotifyAppRemote.getPlayerApi().skipPrevious();
    }

    private void playNextTrack(){
        mSpotifyAppRemote.getPlayerApi().skipNext();
    }

    private void pauseTrack(){
        mSpotifyAppRemote.getPlayerApi().pause();
        pauseTrack.setVisibility(View.GONE);
        resumeTrack.setVisibility(View.VISIBLE);
    }

    private void resumeTrack(){
        mSpotifyAppRemote.getPlayerApi().resume();
        pauseTrack.setVisibility(View.VISIBLE);
        resumeTrack.setVisibility(View.GONE);
    }

    private void saveLastScanned(String album, String artist){
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("LASTSCANNED", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("album", album);
        editor.putString("artist", artist);
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private JSONObject readLibraryFile(File file) throws IOException, JSONException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while (line != null){
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        String response = stringBuilder.toString();

        return new JSONObject(response);
    }

    private boolean hasPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA_SERVICE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestConjurePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                permission.CAMERA)) {
            showExplanation("Permission Needed", "The camera is used to scan albums. Conjure does not access your images.", permission.CAMERA, PERMISSION_REQUEST_CODE);
        } else {
            requestPermission(permission.CAMERA, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permissions granted");
                } else {
                    Log.i(TAG, "Permissions denied");
                }
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> requestPermission(permission, permissionRequestCode));
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }
}
