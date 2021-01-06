package com.berryspace.conjure;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.berryspace.Connectors.SelectedAlbumCountInterface;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


public class AlbumSearchResultsAdapter extends RecyclerView.Adapter<AlbumSearchResultsAdapter.MyViewHolder>{
    private ArrayList<Album> mDataset;
    public Context context;
    private static final String TAG="AlbumSearchResultsAdapter";
    private HashMap<Integer, Boolean> selectedAlbums = new HashMap<>();
    SelectedAlbumCountInterface selectedAlbumCountInterface;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView albumName;
        public ImageView imageView;
        public TextView year;
        public CheckBox selectorButton;
        private static String TAG = "AlbumSearchResultsAdapter";

        public MyViewHolder(ConstraintLayout view) {
            super(view);
            cardView = (CardView) view.getViewById(R.id.search_album_view);
            imageView = (ImageView) cardView.getChildAt(0);
            albumName = (TextView) view.getViewById(R.id.search_album_name);
            year = (TextView) view.getViewById(R.id.search_album_year);
            selectorButton = (CheckBox) view.getViewById(R.id.album_selector_button);
        }
    }

    public AlbumSearchResultsAdapter(ArrayList<Album> dataset, SelectedAlbumCountInterface selectedAlbumCountInterface) {
        mDataset = dataset;
        for (int i = 0; i < mDataset.size(); i++) {
            selectedAlbums.put(i, false);
        }
        this.selectedAlbumCountInterface = selectedAlbumCountInterface;
      }

    @Override
    public AlbumSearchResultsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_album, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        context = view.getContext();
         return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.albumName.setText((CharSequence) mDataset.get(position).getName());
        holder.year.setText((CharSequence) (mDataset.get(position).getYear()));
        Picasso.with(context).load(mDataset.get(position).getImageUrl()).into(holder.imageView);
        holder.selectorButton.setChecked(selectedAlbums.get(position));

        holder.itemView.setOnClickListener(v -> {
            Log.i(TAG, "album selected: " + mDataset.get(position).getImageUrl());
            if (holder.selectorButton.isChecked()){
                selectedAlbums.put(position, false);
                holder.selectorButton.setChecked(false);
             } else {
                selectedAlbums.put(position, true);
                holder.selectorButton.setChecked(true);
             }
            updateSelectedAlbumCount();
         });

        holder.selectorButton.setOnClickListener(v -> {
            if (holder.selectorButton.isChecked()){
                selectedAlbums.put(position, true);
            } else {
                selectedAlbums.put(position, false);
            }
            updateSelectedAlbumCount();
        });
    }

    void updateSelectedAlbumCount(){
        Map<Object, Object> filteredMap = selectedAlbums.entrySet().stream()
                .filter(map -> map.getValue() == true)
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        selectedAlbumCountInterface.transferSelectedAlbumCount(filteredMap.size());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}