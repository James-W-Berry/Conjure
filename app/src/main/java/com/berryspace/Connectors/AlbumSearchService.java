package com.berryspace.Connectors;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.berryspace.conjure.Album;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlbumSearchService {
    private static final String TAG = "AlbumSearchService";
    private static final String ENDPOINT = "https://api.spotify.com/v1/artists/";
    private String mToken;
    private RequestQueue mQueue;
    private String mId;
    private ArrayList<Album> albums = new ArrayList<>();

    public AlbumSearchService(RequestQueue queue, String token, String id) {
        mQueue = queue;
        mToken = token;
        mId = id;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public void getSearchResultArtists(final VolleyCallback callBack) throws JSONException {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, ENDPOINT + mId + "/albums/?include_groups=album&limit=50", null, response -> {
                    try {
                        Log.d(TAG, response.toString());
                        JSONArray items = (JSONArray) response.get("items");
                        for (int n = 0; n < items.length(); n++) {
                            try {
                                JSONObject object = items.getJSONObject(n);
                                Album album = new Album();
                                JSONArray images = (JSONArray) object.get("images");
                                JSONObject image = (JSONObject) images.get(0);

                                album.setImageUrl(image.get("url").toString());
                                album.setName((String) object.get("name"));
                                album.setYear((String) object.get("release_date"));
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
                    // TODO: Handle error

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
}
