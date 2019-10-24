/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.berryspace.muse;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.berryspace.common.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.Connector.ConnectionListener;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 *
 * <p>In this example, we assume all images are static or moving slowly with a large occupation of
 * the screen. If the target is actively moving, we recommend to check
 * ArAugmentedImage_getTrackingMethod() and render only when the tracking method equals to
 * AR_AUGMENTED_IMAGE_TRACKING_METHOD_FULL_TRACKING. See details in <a
 * href="https://developers.google.com/ar/develop/c/augmented-images/">Recognize and Augment
 * Images</a>.
 */
public class AugmentedImageActivity extends AppCompatActivity {

  private ArFragment arFragment;
  private ImageView fitToScanView;

  private static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
  private static final String REDIRECT_URI = "com.berryspace.muse://callback/";
  private SpotifyAppRemote mSpotifyAppRemote;

  // Augmented image and its associated center pose anchor, keyed by the augmented image in
  // the database.
  private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

  private final Map<String, String> spotifyUriMap = new HashMap<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    spotifyUriMap.put("nephilims_noose-rites_of_a_death_merchant.jpg", "spotify:album:4VAKos4MaWN3Y53ay6ahwH");
    spotifyUriMap.put("lik-carnage.jpg", "spotify:album:2kIv6SsdVx9WTANe1R2sm6");
    spotifyUriMap.put("blut_aus_nord-hallucinogen.jpg", "spotify:album:7JE1WpvUTOU06F2CoL5JgB");

    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
    fitToScanView = findViewById(R.id.image_view_fit_to_scan);

    arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Set the connection parameters
    ConnectionParams connectionParams =
            new ConnectionParams.Builder(CLIENT_ID)
              .setRedirectUri(REDIRECT_URI)
              .showAuthView(true)
              .build();

    SpotifyAppRemote.connect(this, connectionParams,
            new ConnectionListener() {
              @Override
              public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                Log.d("AugmentedImageActivity", "Connected to Spotify");
              }

              @Override
              public void onFailure(Throwable throwable) {
                Log.e("AugmentedImageActivity", throwable.getMessage(), throwable);
              }
            });
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
  private void onUpdateFrame(FrameTime frameTime) {
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
          // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
          // but not yet tracked.
          String text = "Detected Image " + augmentedImage.getName();
          // Add newly

          SnackbarHelper.getInstance().showMessage(this, text);

          break;

        case TRACKING:
          // Have to switch to UI Thread to update View.
          fitToScanView.setVisibility(View.GONE);

          // Create a new anchor for newly found images.
          if (!augmentedImageMap.containsKey(augmentedImage)) {

            if(spotifyUriMap.containsKey(augmentedImage.getName())) {
              stopMusic();
              Log.i("AugmentedImageActivity", augmentedImage.getName());
              playMusic(spotifyUriMap.get(augmentedImage.getName()));
            } else {
              Log.e("AugmentedImageActivity", "unknown image");
            }

            AugmentedImageNode node = new AugmentedImageNode(this);
            node.setImage(augmentedImage);
            augmentedImageMap.put(augmentedImage, node);
            //arFragment.getArSceneView().getScene().addChild(node);
          }


          break;

        case STOPPED:
          augmentedImageMap.remove(augmentedImage);
          break;
      }
    }
  }

  private void playMusic(String spotifyUri) {
    Log.i("AugmentedImageActivity", spotifyUri);
    mSpotifyAppRemote.getPlayerApi().play(spotifyUri);
  }

  private void stopMusic() {
    mSpotifyAppRemote.getPlayerApi().pause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    SpotifyAppRemote.disconnect(mSpotifyAppRemote);
  }
}
