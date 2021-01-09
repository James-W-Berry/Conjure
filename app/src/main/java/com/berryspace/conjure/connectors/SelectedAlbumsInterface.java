package com.berryspace.conjure.connectors;

import com.berryspace.conjure.models.Album;

import java.util.HashMap;

public interface SelectedAlbumsInterface {
    void transferSelectedAlbumCount(Integer count);
    void transferSelectedAlbumImages(HashMap<String, String> selectedAlbums);
 }
