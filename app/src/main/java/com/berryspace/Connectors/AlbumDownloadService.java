package com.berryspace.Connectors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import androidx.appcompat.app.AppCompatActivity;

public class AlbumDownloadService  extends AppCompatActivity {
    private static final String TAG = "AlbumDownloadService";
      private HashMap<String, String> mImages;
      private Context mContext;

    public AlbumDownloadService(HashMap<String, String> images, Context context) {
        mImages = images;
        mContext = context;
     }

     public void getAlbumImages() {
         mImages.forEach((k, v)->{
             String uri = getFilename(k);
              try {
                  Picasso.with(this).load(v).into(getTarget(uri));
             } catch (Exception e) {
                 e.printStackTrace();
             }
          });
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

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }

    public String getFilename(String fileName) {
         File file = new File(mContext.getFilesDir(), "images");
        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + fileName);
    }
}
