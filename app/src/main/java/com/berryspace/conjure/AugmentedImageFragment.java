package com.berryspace.conjure;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.berryspace.common.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class AugmentedImageFragment extends ArFragment {
    private static final String TAG = "AugmentedImageFragment";
    private AugmentedImageDatabase database;
    private final String imageDatabaseName = "database_0.imgdb";
    private File dbDirectory;
    private String dbFilePath;
    public Session mSession;

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

        dbDirectory = new File(Objects.requireNonNull(getActivity()).getBaseContext().getFilesDir(), "image_databases");
        dbFilePath = Objects.requireNonNull(getActivity()).getBaseContext().getFilesDir().toString() + "/image_databases/database_0.imgdb";
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
        mSession = session;
        Config config = super.getSessionConfiguration(session);
        if (!loadDatabase(session)) {
            SnackbarHelper.getInstance()
                    .showError(getActivity(), "Could not load augmented image database");
        } else {
            Log.i(TAG, "Augmented image database loaded successfully");
        }

        config.setAugmentedImageDatabase(database);
        return config;
    }

    private class ConjureFileFilter implements FileFilter {
        private final String[] validFiles = new String[] {imageDatabaseName};

        public boolean accept(File file) {
            for (String target : validFiles)
            {
                if (file.getName().equals(target))
                {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean loadDatabase(Session session)   {
        if(imageDatabaseExists()){
            Log.i(TAG, "Located existing augmented image database");
        } else {
            Log.i(TAG, "Existing augmented image database not found. Creating empty database.");
            try {
                createEmptyImageDatabase(session);
            } catch (IOException e){
                Log.e(TAG, "IO exception creating new augmented image database.", e);
            }
        }

        try (InputStream is = new FileInputStream(dbFilePath)) {
            database = AugmentedImageDatabase.deserialize(session, is);
        } catch (IOException e) {
            Log.e(TAG, "IO exception loading augmented image database.", e);
            return false;
        }

        return true;
    }

    private boolean imageDatabaseExists() {
        ensureDirectoryExists(dbDirectory);
        ConjureFileFilter fileFilter = new ConjureFileFilter();
        File[] validDatabases = dbDirectory.listFiles(fileFilter);
        if(validDatabases.length >0){
            return true;
        } else {
            return false;
        }
    }

    private void createEmptyImageDatabase(Session session) throws IOException {
        AugmentedImageDatabase imageDatabase = new AugmentedImageDatabase(session);
        ensureDirectoryExists(dbDirectory);
        try{
            File file = new File(dbFilePath);
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            imageDatabase.serialize(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException exception){
            Log.e(TAG, "Failed to create new image database file");
        }
    }

    private void ensureDirectoryExists(File directory){
        if(!directory.exists()){
            directory.mkdir();
        }
    }

}
