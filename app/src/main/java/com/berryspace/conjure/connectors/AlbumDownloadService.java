package com.berryspace.conjure.connectors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.berryspace.common.helpers.SnackbarHelper;
import com.berryspace.conjure.models.Album;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

public class AlbumDownloadService  extends AppCompatActivity {
    private static final String TAG = "AlbumDownloadService";
    private static Drawable downloadStatus;
    private HashMap<String, String> mImages;
    private Context mContext;
    private ArrayList<Album> mAlbumData;

    public AlbumDownloadService(HashMap<String, String> images, ArrayList<Album> albumData, Context context) {
        mImages = images;
        mAlbumData = albumData;
        mContext = context;
    }

     public Boolean getAlbumImages() throws IOException, JSONException {
         mImages.forEach((k, v)->{
             String uri = getFilename(k);
              try {
                  Picasso.with(this).load(v).into(getTarget(uri));
              } catch (Exception e) {
                 e.printStackTrace();
             }
          });
         saveDownloadedAlbumData();
         return true;
     }


    private static Target getTarget(final String fileName) {
        Target target = new Target() {
             @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(() -> {
                    try {
                        File file = new File(fileName);
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();
                        FileOutputStream fileoutputstream = new FileOutputStream(file);
                        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 60, bytearrayoutputstream);
                        fileoutputstream.write(bytearrayoutputstream.toByteArray());
                        fileoutputstream.close();

                    } catch (IOException e) {
                        Log.e("IOException", e.getLocalizedMessage());
                    }
                }).start();
             }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                downloadStatus = errorDrawable;
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                downloadStatus = placeHolderDrawable;
            }
        };
        return target;
    }

    public String getFilename(String fileName) {
        File file = new File(mContext.getFilesDir(), "images");
        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + fileName + ".png");
    }

    private void saveDownloadedAlbumData() throws IOException, JSONException {
         File dir = new File(mContext.getFilesDir(), "library");
        if(!dir.exists()){
            dir.mkdirs();
        }

        JSONObject libraryObject = new JSONObject();
        String path = mContext.getFilesDir().toString()+ "/library/unprocessed.json";
        File file = new File(path);
        if (!file.exists()){
            file.createNewFile();
        } else {
            libraryObject = readLibraryFile(file);
        }

        JSONObject updatedLibraryObject = addAlbumsToLibraryObject(libraryObject);
        writeNewLibraryFile(updatedLibraryObject, file);
    }

    private JSONObject readLibraryFile(File file) throws IOException, JSONException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while (line != null){
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        String response = stringBuilder.toString();

        return new JSONObject(response);
    }

    private JSONObject addAlbumsToLibraryObject(JSONObject library){
        mAlbumData.forEach(album -> {
            JSONObject data = new JSONObject();
            try {
                data.put("id", album.getId());
                data.put("name", album.getName());
                data.put("year", album.getYear());
                data.put("artist", album.getArtist());
                data.put("imageUrl",album.getImageUrl());
                library.put(album.getId(), data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        return library;
    }

    private void writeNewLibraryFile(JSONObject library, File file) throws IOException {
        String libraryString = library.toString();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(libraryString);
        bufferedWriter.close();
    }
}
