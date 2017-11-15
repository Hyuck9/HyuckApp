package com.lhg1304.hyuckapp.page02.firemessenger.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lhg1304.hyuckapp.R;
import com.lhg1304.hyuckapp.page02.firemessenger.customviews.RoundedImageView;
import com.lhg1304.hyuckapp.page02.firemessenger.models.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lhg1304 on 2017-11-15.
 */

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendHolder> {


    public static final int UNSELECTION_MODE = 1;
    public static final int SELECTION_MODE = 2;

    private ArrayList<User> friendList;

    public FriendListAdapter() {
        friendList = new ArrayList<>();
    }

    public void addItem(User friend) {
        friendList.add(friend);
        notifyDataSetChanged();
    }

    public User getItem(int position) {
        return this.friendList.get(position);
    }

    @Override
    public FriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.page_02_fragment_messenger_friend_item, parent, false);
        FriendHolder friendHolder = new FriendHolder(view);
        return friendHolder;
    }

    @Override
    public void onBindViewHolder(FriendHolder holder, int position) {
        User friend = getItem(position);
        holder.mEmailView.setText(friend.getEmail());
        holder.mNameView.setText(friend.getName());
        if ( friend.getProfileUrl() != null ) {
            Glide.with(holder.itemView)
                    .load(friend.getProfileUrl())
                    .into(holder.mProfileView);
        }
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }


    public static class FriendHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.messenger_friend_item_thumb)
        RoundedImageView mProfileView;

        @BindView(R.id.messenger_friend_item_name)
        TextView mNameView;

        @BindView(R.id.messenger_friend_item_email)
        TextView mEmailView;

        private FriendHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }
}
