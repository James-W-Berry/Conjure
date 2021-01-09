package com.berryspace.conjure;

import android.os.Bundle;
import android.widget.SearchView;
import com.berryspace.conjure.adapters.LibraryAdapter;
import com.berryspace.conjure.models.Artist;
import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LibraryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        Artist tester = new Artist();
        tester.setName("Artist 1");
        tester.setFollowers("1000");
        tester.setGenres("black metal");
        tester.setId("12345");
        tester.setImageUrl("https://i.scdn.co/image/ab67616d0000b27307e244f9c29b4341f17d0378");
        ArrayList<Artist> artists = new ArrayList<>();
        artists.add(tester);

        searchView = findViewById(R.id.searchbar);
        searchView.setOnClickListener(v -> searchView.setIconified(false));
        recyclerView = findViewById(R.id.library);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new LibraryAdapter(artists);
        recyclerView.setAdapter(mAdapter);
    }

}
