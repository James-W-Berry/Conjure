package com.berryspace.conjure;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.MyViewHolder>{
     private ArrayList<Artist> mDataset;
    public Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public ImageView imageView;

        public MyViewHolder(ConstraintLayout view) {
            super(view);
            textView = (TextView) view.getViewById(R.id.search_artist_name);
            imageView = (ImageView) view.getViewById(R.id.search_artist_image);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SearchResultsAdapter(ArrayList<Artist> dataset) {
        mDataset = dataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SearchResultsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        ConstraintLayout view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_artist, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        context = view.getContext();
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.i("SearchResultsAdapter", mDataset.get(position).getName());
        holder.textView.setText((CharSequence) mDataset.get(position).getName());
        Picasso.with(context).load(mDataset.get(position).getImageUrl()).into(holder.imageView);
     }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
         return mDataset.size();
    }
}