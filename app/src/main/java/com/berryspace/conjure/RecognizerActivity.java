package com.berryspace.conjure;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.berryspace.Connectors.ArtistSearchService;
import org.json.JSONException;
import java.util.ArrayList;


public class RecognizerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
     private SearchView searchView;
    private static String TAG = "RecognizerActivity";
    private RequestQueue queue;
    private String token;
    private ArrayList<Artist> searchResults;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        token = getIntent().getStringExtra("token");
        queue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_recognizer);

        recyclerView = findViewById(R.id.search_results);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        searchView = findViewById(R.id.searchbar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                 return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    search(query);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
         });
        searchView.requestFocus();
        searchView.setIconified(false);

        progressBar = findViewById(R.id.indeterminateBar);
    }

    private void search(String query) throws JSONException {
        updateSearchStatus(View.VISIBLE);
        ArtistSearchService artistSearchService = new ArtistSearchService(queue, token, query);
        artistSearchService.getSearchResultArtists(() -> {
            searchResults = artistSearchService.getArtists();
            Log.d(TAG, searchResults.get(0).getName() + searchResults.get(0).getImageUrl());
            updateSearchResult();
            updateSearchStatus(View.INVISIBLE);
        });
    }

    private void updateSearchResult() {
        mAdapter = new SearchResultsAdapter(searchResults);
        recyclerView.setAdapter(mAdapter);
    }

    private void updateSearchStatus(Integer visibility){
        progressBar.setVisibility(visibility);
    }

}
