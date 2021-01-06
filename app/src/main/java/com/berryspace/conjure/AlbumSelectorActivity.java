package com.berryspace.conjure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.berryspace.Connectors.AlbumSearchService;
import com.berryspace.Connectors.SelectedAlbumCountInterface;
import org.json.JSONException;
import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumSelectorActivity extends AppCompatActivity implements SelectedAlbumCountInterface {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);

        SharedPreferences sharedPref = this.getSharedPreferences("SPOTIFYAUTH", Context.MODE_PRIVATE);
        token = sharedPref.getString("spotifyToken", null);

        setContentView(R.layout.activity_album_selector);
        String id = getIntent().getStringExtra("id");

        selectedAlbumCount = findViewById(R.id.selected_album_count);
        progressBar = findViewById(R.id.indeterminateBar);
        recyclerView = findViewById(R.id.artist_album_results);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        startRecognitionButton = findViewById(R.id.start_recognition_button);

        try {
            search(id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void transferSelectedAlbumCount(Integer count){
        Log.i(TAG, String.valueOf(count));
        if(count>0){
            startRecognitionButton.setVisibility(View.VISIBLE);
            selectedAlbumCount.setText(count.toString() + " albums selected");
        } else if(count.equals((Integer)1)){
            startRecognitionButton.setVisibility(View.VISIBLE);
            selectedAlbumCount.setText(count.toString() + " album selected");
        } else {
            startRecognitionButton.setVisibility(View.INVISIBLE);
            selectedAlbumCount.setText("No albums selected");
        }
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

    private void updateSearchStatus(Integer visibility){
        progressBar.setVisibility(visibility);
    }



}
