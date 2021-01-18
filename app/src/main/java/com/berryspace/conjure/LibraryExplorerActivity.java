package com.berryspace.conjure;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.SearchView;
import com.berryspace.conjure.adapters.LibraryAdapter;
import com.berryspace.conjure.models.Album;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LibraryExplorerActivity extends AppCompatActivity {
    private static final String TAG = "LibraryExplorerActivity";
    private RecyclerView recyclerView;
    private LibraryAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SearchView searchView;
    private File libraryDirectory;
    private String detectableListPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_explorer);

        libraryDirectory = new File(this.getFilesDir(), "library");
        detectableListPath = this.getFilesDir().toString() + "/library/detectable.json";

        searchView = findViewById(R.id.searchbar);
        searchView.setOnClickListener(v -> searchView.setIconified(false));
        recyclerView = findViewById(R.id.library);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        retrieveLibrary();
    }


    private void retrieveLibrary(){
        ArrayList<Album> results = new ArrayList<>();
        ensureDirectoryExists(libraryDirectory);
        File file = new File(detectableListPath);
        if(!file.exists()){
            return;
        }
        JSONObject library = convertFileToJson(file);
        try {
            assert library != null;
            Iterator<String> keys = library.keys();

            while(keys.hasNext()) {
                String key = keys.next();
                if (library.get(key) instanceof JSONObject) {
                    Album album = new Album();
                    JSONObject detectable = (JSONObject) library.get(key);
                    String artist = (String) detectable.get("artist");

                    album.setArtist(artist);
                    album.setId((String) detectable.get("id"));
                    album.setName((String) detectable.get("name"));
                    album.setYear((String) detectable.get("year"));
                    album.setImageUrl((String) detectable.get("imageUrl"));

                    results.add(album);
                }
            }
        } catch (NullPointerException | JSONException exception){
            Log.e(TAG, "Failed to fetch number of unique artists: " + exception.getMessage());
        }

        mAdapter = new LibraryAdapter(results);
        mAdapter.storeAllResults(results);
        updateLibraryResults(results);
    }

    private void updateLibraryResults(ArrayList<Album> libraryResults) {
        recyclerView.setAdapter(mAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void ensureDirectoryExists(File directory){
        if(!directory.exists()){
            directory.mkdir();
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

}
