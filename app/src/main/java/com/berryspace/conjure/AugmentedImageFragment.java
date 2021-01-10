package com.berryspace.conjure;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.ar.core.exceptions.ImageInsufficientQualityException;
import com.google.ar.sceneform.ux.ArFragment;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class AugmentedImageFragment extends ArFragment {
    private static final String TAG = "AugmentedImageFragment";
    private AugmentedImageDatabase database;
    private final String imageDatabaseName = "database_0.imgdb";
    private File dbDirectory;
    private String dbFilePath;
    private File imageDirectory;
    private File libraryDirectory;
    private String unprocessedListPath;
    private String detectableListPath;
    private String undetectableListPath;

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
        imageDirectory = new File( Objects.requireNonNull(getActivity()).getBaseContext().getFilesDir(), "images");
        libraryDirectory = new File(Objects.requireNonNull(getActivity()).getBaseContext().getFilesDir(), "library");
        unprocessedListPath = getActivity().getBaseContext().getFilesDir().toString() + "/library/unprocessed.json";
        detectableListPath = getActivity().getBaseContext().getFilesDir().toString() + "/library/detectable.json";
        undetectableListPath = getActivity().getBaseContext().getFilesDir().toString() + "/library/undetectable.json";
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
        if (!loadDatabase(session)) {
            SnackbarHelper.getInstance()
                    .showError(getActivity(), "Could not setup augmented image database");
        } else {
            Log.i(TAG, "Augmented image database loaded successfully");
        }
        if(!updateDatabase()){
            SnackbarHelper.getInstance()
                    .showError(getActivity(), "Could not update augmented image database");
        } else {
            Log.i(TAG, "Augmented image database updated successfully");
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

    private Boolean updateDatabase(){
        File[] images = checkForNewImages();
        boolean deleted;
        boolean imagesSaved = false;
        boolean databaseSaved;

        for (File image : images) {
            Log.i(TAG, "Attempting to add " + image.getName() + " to image database");
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            try {
                int index = database.addImage(image.getName(), bitmap, (float) 0.3);
                Log.i(TAG, "Successfully added " + image.getName() + " to image database");
                imagesSaved = saveToLibrary(image, true);
                Log.i(TAG, "Successfully added " + image.getName() + " to library");
                deleted = image.delete();
                Log.i(TAG, image.getName() + " deleted - " + deleted);
            } catch (ImageInsufficientQualityException e){
                Log.i(TAG, image.getName() + " cannot be added to the image database due " +
                        "insufficient quality (too few features). " +
                        "Deleting image file from local storage.");
                imagesSaved = saveToLibrary(image, false);
                Log.i(TAG, "Successfully added " + image.getName() + " to undetectable list");
                deleted = image.delete();
                Log.i(TAG, image.getName() + " deleted - " + deleted);
            }
        }

        Log.i(TAG, "Images in database: " + database.getNumImages());
        saveLibraryStats(database.getNumImages());

        databaseSaved = saveImageDatabase(database);
        //TODO: decide what message to return to show the user, if any
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

    private File[] checkForNewImages(){
        ensureDirectoryExists(imageDirectory);
        return imageDirectory.listFiles();
    }

    private boolean saveToLibrary(File image, Boolean detectable) {
        ensureDirectoryExists(libraryDirectory);

        File listFile;
        listFile = detectable ? new File(detectableListPath) : new File(undetectableListPath);
        ensureFileExists(listFile, detectable ? "detectable images"
                : "undetectable images");
        JSONObject listObject = convertFileToJson(listFile);

        File unprocessedFile = new File(unprocessedListPath);
        ensureFileExists(unprocessedFile, "images that have not been added to the database or sorted yet");
        JSONObject unprocessedObject = convertFileToJson(unprocessedFile);

        try {
            JSONObject album = (JSONObject) unprocessedObject.get(image.getName().replace(".png", ""));
            Log.i(TAG, "Transferring album " + image.getName() + " from unprocessed list to library list");
            listObject.put(image.getName(), album);
            writeJsonToFile(listObject, listFile);
            unprocessedObject.remove(image.getName().replace(".png", ""));
            writeJsonToFile(unprocessedObject, unprocessedFile);
            return true;
        } catch (JSONException | NullPointerException exception) {
            Log.i(TAG, Objects.requireNonNull(exception.getMessage()));
            return false;
        }
    }

    private void ensureDirectoryExists(File directory){
        if(!directory.exists()){
            directory.mkdir();
        }
    }

    private void ensureFileExists(File file, String description) {
        if(!file.exists()){
            try{
                file.createNewFile();
                JSONObject empty = new JSONObject();
                empty.put("file_description", description);
                writeJsonToFile(empty, file);
            } catch (IOException | JSONException exception){
                Log.e(TAG, "Failed to created file: " + file.getAbsolutePath());
            }
        } else{
            Log.i(TAG, "Found existing file: " + file.getAbsolutePath());
        }
    }

    private JSONObject convertFileToJson(File file) {
        Log.i(TAG, "Converting " + file.getName() + " to JSON");
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            String response = stringBuilder.toString();
            Log.i(TAG, "Success converting "+file.getName()+" to JSON");
            return new JSONObject(response);
        } catch (JSONException | IOException exception) {
            Log.e(TAG, "Failure converting "+file.getName()+" to JSON: " +  exception.getMessage());
            return null;
        }
    }

    private void writeJsonToFile(JSONObject object, File file) {
        try {
            String objectString = object.toString();
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(objectString);
            bufferedWriter.close();
        } catch (IOException exception){
            Log.e(TAG, "Failed to write JSON to file");
        }

    }

    private void saveLibraryStats(Integer albumCount){
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getBaseContext().getSharedPreferences("LIBRARYSTATS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Integer artistCount = fetchTotalArtistsInDatabase();
        editor.putInt("artistCount", artistCount);
        editor.putInt("albumCount", albumCount);
        editor.apply();
    }

    private boolean saveImageDatabase(AugmentedImageDatabase imageDatabase) {
        try{
            File file = new File(dbFilePath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            imageDatabase.serialize(fileOutputStream);
            fileOutputStream.close();
            return true;
        } catch (IOException exception){
            Log.e(TAG, "Failed to save augmented image database");
            return false;
        }
    }

    private Integer fetchTotalArtistsInDatabase(){
        File file = new File(detectableListPath);
        JSONObject detectables = convertFileToJson(file);
        HashMap<String, Integer> artists = new HashMap<>();

        try {
            assert detectables != null;
            Iterator<String> keys = detectables.keys();

            while(keys.hasNext()) {
                String key = keys.next();
                if (detectables.get(key) instanceof JSONObject) {
                    String name = (String) ((JSONObject) detectables.get(key)).get("artist");
                    if(artists.containsKey(name)){
                        artists.put(name, artists.get(name) + 1);
                    } else {
                        artists.put(name, 1);
                    }
                }
            }
        } catch (NullPointerException | JSONException exception){
            Log.e(TAG, "Failed to fetch number of unique artists: " + exception.getMessage());
        }
        return artists.size();
    }
}
