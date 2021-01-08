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

package com.berryspace.conjure;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.berryspace.common.helpers.SnackbarHelper;
import com.google.ar.core.exceptions.ImageInsufficientQualityException;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

public class AugmentedImageFragment extends ArFragment {
    private static final String TAG = "AugmentedImageFragment";

    // Do a runtime check for the OpenGL level available at runtime to avoid Sceneform crashing the
    // application.
    private static final double MIN_OPENGL_VERSION = 3.0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Check for Sceneform being supported on this device.  This check will be integrated into
        // Sceneform eventually.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            SnackbarHelper.getInstance()
                    .showError(getActivity(), "Sceneform requires Android N or later");
        }

        String openGlVersionString =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later");
            SnackbarHelper.getInstance()
                    .showError(getActivity(), "Sceneform requires OpenGL ES 3.0 or later");
        }

    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Turn off the plane discovery since we're only looking for images
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);
        return view;
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = super.getSessionConfiguration(session);
        try {
            if (!setupAugmentedImageDatabase(config, session)) {
                SnackbarHelper.getInstance()
                        .showError(getActivity(), "Could not setup augmented image database");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public class ImageDatabaseFileFilter implements FileFilter
    {
        private final String[] validFiles = new String[] {"database_0.imgdb"};

        public boolean accept(File file)
        {
            for (String extension : validFiles)
            {
                if (file.getName().toLowerCase().endsWith(extension))
                {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean setupAugmentedImageDatabase(Config config, Session session) throws IOException {
        AugmentedImageDatabase augmentedImageDatabase;

        AssetManager assetManager = getContext() != null ? getContext().getAssets() : null;
        if (assetManager == null) {
            Log.e(TAG, "Context is null, cannot intitialize image database.");
            return false;
        }

        if(imageDatabaseExists()){
            String path =  Objects.requireNonNull(getActivity()).getBaseContext().getFilesDir().toString()+ "/image_databases/database_0.imgdb";
            try (InputStream is = new FileInputStream(path)) {
                augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
            } catch (IOException e) {
                Log.e(TAG, "IO exception loading augmented image database.", e);
                return false;
            }
        } else {
              //createEmptyImageDatabase(session);
            String path =  Objects.requireNonNull(getActivity()).getBaseContext().getFilesDir().toString()+ "/image_databases/database_0.imgdb";
            try (InputStream is = new FileInputStream(path)) {
                augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
            } catch (IOException e) {
                Log.e(TAG, "IO exception loading augmented image database.", e);
                return false;
            }
         }

        File[] newImages = checkForNewImages();
        boolean deleted;
        for (File image : newImages) {
            Log.i(TAG, "adding " + image.getName() + " to image database");
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            try{
                int index = augmentedImageDatabase.addImage(image.getName(), bitmap, (float) 0.3);
                deleted = image.delete();
                Log.i(TAG, "deleted " + image.getName() + " from local storage: " + deleted);
            } catch (ImageInsufficientQualityException e){
                Log.i(TAG, image.getName() + " cannot be added to the image database due insufficient quality (too few features)");
            }
        }

        Log.i(TAG, String.valueOf(augmentedImageDatabase.getNumImages()));
        saveImageDatabase(augmentedImageDatabase);

        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    private File[] checkForNewImages(){
        String path = Objects.requireNonNull(getActivity()).getBaseContext().getFilesDir().toString()+"/images";
        File directory = new File(path);
        return directory.listFiles();
    }

     private Boolean imageDatabaseExists()   {
         File dir = new File(Objects.requireNonNull(getActivity()).getBaseContext().getFilesDir(), "image_databases");
         if(!dir.exists()){
            dir.mkdir();
            return false;
         }
         File[] files = dir.listFiles(new ImageDatabaseFileFilter());
          if(files.length >0){
             Log.i(TAG, "using existing database_0");
             return true;
         } else {
              Log.i(TAG, "existing database not found");
              return false;
         }
    }

    private void createEmptyImageDatabase(Session session) throws IOException {
        AugmentedImageDatabase imageDatabase = new AugmentedImageDatabase(session);
        String path =  Objects.requireNonNull(getActivity()).getBaseContext().getFilesDir().toString()+ "/image_databases/database_0.imgdb";
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        imageDatabase.serialize(fileOutputStream);
        fileOutputStream.close();
    }

    private void saveImageDatabase(AugmentedImageDatabase imageDatabase) throws IOException {
         String path =  Objects.requireNonNull(getActivity()).getBaseContext().getFilesDir().toString()+ "/image_databases/database_0.imgdb";
        File file = new File(path);

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        imageDatabase.serialize(fileOutputStream);
        fileOutputStream.close();
    }


}
