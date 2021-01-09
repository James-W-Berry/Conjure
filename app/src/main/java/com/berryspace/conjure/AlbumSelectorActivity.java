package com.berryspace.conjure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.berryspace.conjure.adapters.AlbumResultsAdapter;
import com.berryspace.conjure.connectors.AlbumDownloadService;
import com.berryspace.conjure.connectors.AlbumSearchService;
 import com.berryspace.conjure.connectors.SelectedAlbumsInterface;
import com.berryspace.conjure.models.Album;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private Handler handler = new Handler();
    private CheckBox selectAllButton;
    private ConstraintLayout selectAllView;

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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        albumDownloadMessage = findViewById(R.id.album_download_message);
        selectAllView = findViewById(R.id.select_all_view);
        selectAllButton = findViewById(R.id.select_all_button);

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

    private void downloadAlbumImages(HashMap<String, String> images) throws IOException, JSONException {
        updateSearchStatus(View.VISIBLE);
        selectedAlbumCount.setVisibility(View.INVISIBLE);
        startRecognitionButton.setVisibility(View.INVISIBLE);

        AlbumDownloadService albumDownloadService = new AlbumDownloadService(images, searchResults, this);
        Boolean done = albumDownloadService.getAlbumImages();
        if(done){
            clearSearchResults();
            updateSearchStatus(View.INVISIBLE);
            String text = getString(R.string.album_download_message, images.size(), artistName);
            albumDownloadMessage.setVisibility(View.VISIBLE);
            albumDownloadMessage.setText(text);

            handler.postDelayed(this::finish, 3000);
        }
    }

}
