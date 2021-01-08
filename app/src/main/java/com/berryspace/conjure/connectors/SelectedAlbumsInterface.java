package com.berryspace.conjure.connectors;

import java.util.HashMap;

public interface SelectedAlbumsInterface {
    void transferSelectedAlbumCount(Integer count);
    void transferSelectedAlbumImages(HashMap<String, String> selectedAlbums);
 }
