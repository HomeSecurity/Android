package com.hosec.homesecurity.activities;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.ListItemInformation;

/**
 * Created by D062572 on 23.05.2017.
 */
public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ViewHolder> {
    private ListItemInformation[] mListItemInformation;
    private int mLastPosition;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public ConstraintLayout layout;
        public ViewHolder(View v) {
            super(v);
            layout = (ConstraintLayout) v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListItemAdapter(ListItemInformation[] listItemInformation) {
        mListItemInformation = listItemInformation;
        mLastPosition = -1;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ListItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        ConstraintLayout layout= (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final ListItemInformation info = mListItemInformation[position];
        final ViewHolder constHolder = holder;

        TextView textView = ((TextView)holder.layout.findViewById(R.id.itemHeader));
        textView.setText(info.getTitle());
        textView = ((TextView)holder.layout.findViewById(R.id.itemSubHead));
        textView.setText(info.getSubtitle());
        ImageView imageView = ((ImageView)holder.layout.findViewById(R.id.itemImage));
        imageView.setImageResource(info.getImageId());
        info.customizeListItemTemplate(holder.layout);
        holder.layout.setOnClickListener(info);
        if(info.animate()) {
            setAnimation(holder.itemView, position);
        }
    }

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated

        if(mLastPosition < position) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            mLastPosition = position;
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mListItemInformation.length;
    }
}


