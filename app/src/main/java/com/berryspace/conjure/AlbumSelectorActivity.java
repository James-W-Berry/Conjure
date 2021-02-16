package com.berryspace.conjure;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.Lottie;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.berryspace.conjure.adapters.AlbumResultsAdapter;
import com.berryspace.conjure.connectors.AlbumDownloadService;
import com.berryspace.conjure.connectors.AlbumSearchService;
import com.berryspace.conjure.connectors.SelectedAlbumsInterface;
import com.berryspace.conjure.models.Album;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.ImageInsufficientQualityException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumSelectorActivity extends AppCompatActivity implements SelectedAlbumsInterface {
    private RecyclerView recyclerView;
    private AlbumResultsAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private static String TAG = "AlbumSelector";
    private RequestQueue queue;
    private String token;
    private ArrayList<Album> searchResults;
    private ProgressBar progressBar;
    private TextView selectedAlbumCount;
    private Button startRecognitionButton;
    private HashMap<String, String> selectedAlbums;
    private String artistName;
    private TextView albumDownloadMessage;
    private LottieAnimationView lottieAnimation;
    private Handler handler = new Handler();
    private CheckBox selectAllButton;
    private ConstraintLayout selectAllView;

    private File imageDirectory;
    private final String imageDatabaseName = "database_0.imgdb";
    private File dbDirectory;
    private String dbFilePath;
     private File libraryDirectory;
    private String unprocessedListPath;
    private String detectableListPath;
    private String undetectableListPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);

        SharedPreferences sharedPref = this.getSharedPreferences("SPOTIFYAUTH", Context.MODE_PRIVATE);
        token = sharedPref.getString("spotifyToken", null);

        setContentView(R.layout.activity_album_selector);
        String id = getIntent().getStringExtra("id");
        artistName = getIntent().getStringExtra("name");

        selectedAlbumCount = findViewById(R.id.selected_album_count);
        progressBar = findViewById(R.id.indeterminateBar);
        recyclerView = findViewById(R.id.artist_album_results);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        startRecognitionButton = findViewById(R.id.start_recognition_button);
        startRecognitionButton.setOnClickListener(v -> {
            Log.i(TAG, selectedAlbums.toString());
            try {
                downloadAlbumImages(selectedAlbums);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException | UnavailableSdkTooOldException | UnavailableDeviceNotCompatibleException | UnavailableArcoreNotInstalledException | UnavailableApkTooOldException e) {
                e.printStackTrace();
            }
        });
        albumDownloadMessage = findViewById(R.id.album_download_message);
        lottieAnimation = findViewById(R.id.animation_view);
        lottieAnimation.setRepeatCount(0);
        selectAllView = findViewById(R.id.select_all_view);
        selectAllButton = findViewById(R.id.select_all_button);

        try {
            search(id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        imageDirectory = new File( this.getBaseContext().getFilesDir(), "images");
        dbDirectory = new File(this.getBaseContext().getFilesDir(), "image_databases");
        dbFilePath = this.getBaseContext().getFilesDir().toString() + "/image_databases/database_0.imgdb";
        imageDirectory = new File( this.getBaseContext().getFilesDir(), "images");
        libraryDirectory = new File(this.getBaseContext().getFilesDir(), "library");
        unprocessedListPath = this.getBaseContext().getFilesDir().toString() + "/library/unprocessed.json";
        detectableListPath = this.getBaseContext().getFilesDir().toString() + "/library/detectable.json";
        undetectableListPath = this.getBaseContext().getFilesDir().toString() + "/library/undetectable.json";
    }

    @Override
    public void transferSelectedAlbumCount(Integer count){
        if(count>0){
            startRecognitionButton.setVisibility(View.VISIBLE);
            selectedAlbumCount.setVisibility(View.VISIBLE);
            selectedAlbumCount.setText(count.toString() + " albums selected");
        } else if(count.equals(1)){
            startRecognitionButton.setVisibility(View.VISIBLE);
            selectedAlbumCount.setVisibility(View.VISIBLE);
            selectedAlbumCount.setText(count.toString() + " album selected");
        } else {
            startRecognitionButton.setVisibility(View.INVISIBLE);
            selectedAlbumCount.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void transferSelectedAlbumImages(HashMap<String, String> albums){
         selectedAlbums = albums;
    }



    private void search(String id) throws JSONException {
        updateSearchStatus(View.VISIBLE);
        AlbumSearchService albumSearchService = new AlbumSearchService(queue, token, id, artistName);
        albumSearchService.getSearchResultArtists(() -> {
            searchResults = albumSearchService.getAlbums();
            updateSearchResult();
            updateSearchStatus(View.INVISIBLE);
        });
    }

    private void updateSearchResult() {
        mAdapter = new AlbumResultsAdapter(searchResults, this);
        if(searchResults.size()>1){
            selectAllView.setVisibility(View.VISIBLE);
        }else{
            selectAllView.setVisibility(View.INVISIBLE);
        }
        selectAllButton.setOnClickListener(v -> {
            mAdapter.selectAll(selectAllButton.isChecked());
        });
        recyclerView.setAdapter(mAdapter);
    }

    private void clearSearchResults(){
        recyclerView.setAdapter(null);
    }

    private void updateSearchStatus(Integer visibility){
        progressBar.setVisibility(visibility);
    }

    private void downloadAlbumImages(HashMap<String, String> images) throws IOException, JSONException, UnavailableSdkTooOldException, UnavailableDeviceNotCompatibleException, UnavailableArcoreNotInstalledException, UnavailableApkTooOldException {
        updateSearchStatus(View.VISIBLE);
        selectedAlbumCount.setVisibility(View.INVISIBLE);
        startRecognitionButton.setVisibility(View.INVISIBLE);

        AlbumDownloadService albumDownloadService = new AlbumDownloadService(images, searchResults, this);
        Boolean downloaded = albumDownloadService.getAlbumImages();
        Boolean addedToDatabase = addAlbumsToImageDatabase();

        if(downloaded && addedToDatabase){
            clearSearchResults();
            updateSearchStatus(View.INVISIBLE);
            selectAllView.setVisibility(View.GONE);
            String text = getString(R.string.album_download_message, images.size(), artistName);
            lottieAnimation.setVisibility(View.VISIBLE);
            albumDownloadMessage.setVisibility(View.VISIBLE);
            albumDownloadMessage.setText(text);
             handler.postDelayed(this::finish, 2000);
        }
    }

    private boolean addAlbumsToImageDatabase() throws UnavailableSdkTooOldException, UnavailableDeviceNotCompatibleException, UnavailableArcoreNotInstalledException, UnavailableApkTooOldException {
        AugmentedImageDatabase database;
        Session session = new Session(this);
        String dbFilePath = getBaseContext().getFilesDir().toString() + "/image_databases/database_0.imgdb";

        try (InputStream is = new FileInputStream(dbFilePath)) {
            database = AugmentedImageDatabase.deserialize(session, is);
            updateDatabase(database);
        } catch (IOException e) {
            Log.e(TAG, "IO exception loading augmented image database.", e);
            return false;
        }

        return true;
    }

    public Boolean updateDatabase(AugmentedImageDatabase database){
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

    private void saveLibraryStats(Integer albumCount){
        SharedPreferences sharedPref = this.getBaseContext().getSharedPreferences("LIBRARYSTATS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Integer artistCount = fetchTotalArtistsInDatabase();
        editor.putInt("artistCount", artistCount);
        editor.putInt("albumCount", albumCount);
        editor.apply();
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

    private File[] checkForNewImages(){
        ensureDirectoryExists(imageDirectory);
        return imageDirectory.listFiles();
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


}
