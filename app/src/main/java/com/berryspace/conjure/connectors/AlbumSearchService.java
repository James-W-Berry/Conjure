package com.berryspace.conjure.connectors;

import android.content.Intent;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.berryspace.conjure.models.Album;
import com.berryspace.conjure.SpotifyAuth;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class AlbumSearchService  extends AppCompatActivity {
    private static final String TAG = "AlbumSearchService";
    private static final String ENDPOINT = "https://api.spotify.com/v1/artists/";
    private String mToken;
    private RequestQueue mQueue;
    private String mId;
    private ArrayList<Album> albums = new ArrayList<>();
    private String mArtist;

    public AlbumSearchService(RequestQueue queue, String token, String id, String artist) {
        mQueue = queue;
        mToken = token;
        mId = id;
        mArtist = artist;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public void getSearchResultArtists(final VolleyCallback callBack) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, ENDPOINT + mId + "/albums/?include_groups=album,single,compilation,appears_on&limit=50", null, response -> {
                    try {
                        JSONArray items = (JSONArray) response.get("items");
                        for (int n = 0; n < items.length(); n++) {
                            try {
                                JSONObject object = items.getJSONObject(n);
                                Album album = new Album();
                                JSONArray images = (JSONArray) object.get("images");
                                JSONObject image = (JSONObject) images.get(0);

                                album.setArtist(mArtist);
                                album.setImageUrl(image.get("url").toString());
                                album.setName((String) object.get("name"));
                                album.setYear((String) object.get("release_date"));
                                album.setId((String) object.get("id"));
                                albums.add(album);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callBack.onSuccess();
                }, error -> {
                    Log.i(TAG, error.toString());
                    if (error instanceof AuthFailureError){
                        // get new token from local Spotify client
                        authenticateWithSpotify();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String auth = "Bearer " + mToken;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        mQueue.add(jsonObjectRequest);
    }

    private void authenticateWithSpotify(){
        Intent intent = new Intent(this.getParent(), SpotifyAuth.class);
        startActivity(intent);
    };
}
