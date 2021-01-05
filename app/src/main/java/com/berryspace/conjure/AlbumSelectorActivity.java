package com.berryspace.conjure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.berryspace.Connectors.AlbumSearchService;
import org.json.JSONException;
import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumSelectorActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SearchView searchView;
    private static String TAG = "AlbumSelector";
    private RequestQueue queue;
    private String token;
    private ArrayList<Album> searchResults;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);

        SharedPreferences sharedPref = this.getSharedPreferences("SPOTIFYAUTH", Context.MODE_PRIVATE);
        token = sharedPref.getString("spotifyToken", null);

        setContentView(R.layout.activity_album_selector);
        String id = getIntent().getStringExtra("id");

        progressBar = findViewById(R.id.indeterminateBar);
        recyclerView = findViewById(R.id.artist_album_results);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        try {
            search(id);
        } catch (JSONException e) {
            e.printStackTrace();
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
        mAdapter = new AlbumSearchResultsAdapter(searchResults);
        recyclerView.setAdapter(mAdapter);
    }

    private void updateSearchStatus(Integer visibility){
        progressBar.setVisibility(visibility);
    }

}
