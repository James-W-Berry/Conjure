package com.berryspace.conjure;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.berryspace.Connectors.AlbumDownloadService;
import com.berryspace.Connectors.AlbumSearchService;
 import com.berryspace.Connectors.SelectedAlbumsInterface;

import org.json.JSONException;
import java.util.ArrayList;
import java.util.HashMap;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumSelectorActivity extends AppCompatActivity implements SelectedAlbumsInterface {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
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
            downloadAlbumImages(selectedAlbums);
        });
        albumDownloadMessage = findViewById(R.id.album_download_message);

        try {
            search(id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void transferSelectedAlbumCount(Integer count){
        if(count>0){
            startRecognitionButton.setVisibility(View.VISIBLE);
            selectedAlbumCount.setText(count.toString() + " albums selected");
        } else if(count.equals(1)){
            startRecognitionButton.setVisibility(View.VISIBLE);
            selectedAlbumCount.setText(count.toString() + " album selected");
        } else {
            startRecognitionButton.setVisibility(View.INVISIBLE);
            selectedAlbumCount.setText("No albums selected");
        }
    }

    @Override
    public void transferSelectedAlbumImages(HashMap<String, String> albums){
         selectedAlbums = albums;
    }

    private void search(String id) throws JSONException {
        updateSearchStatus(View.VISIBLE);
        AlbumSearchService albumSearchService = new AlbumSearchService(queue, token, id);
        albumSearchService.getSearchResultArtists(() -> {
            searchResults = albumSearchService.getAlbums();
            updateSearchResult();
            updateSearchStatus(View.INVISIBLE);
        });
    }

    private void updateSearchResult() {
        mAdapter = new AlbumSearchResultsAdapter(searchResults, this);
        recyclerView.setAdapter(mAdapter);
    }

    private void clearSearchResults(){
        recyclerView.setAdapter(null);
    }

    private void updateSearchStatus(Integer visibility){
        progressBar.setVisibility(visibility);
    }

    private void downloadAlbumImages(HashMap<String, String> images){
        updateSearchStatus(View.VISIBLE);
        AlbumDownloadService albumDownloadService = new AlbumDownloadService(images, this);
        albumDownloadService.getAlbumImages();
        updateSearchStatus(View.INVISIBLE);
        startRecognitionButton.setVisibility(View.INVISIBLE);
        clearSearchResults();
        String text = getString(R.string.album_download_message, images.size(), artistName);
        selectedAlbumCount.setVisibility(View.INVISIBLE);
        albumDownloadMessage.setVisibility(View.VISIBLE);
        albumDownloadMessage.setText(text);
    }

}
