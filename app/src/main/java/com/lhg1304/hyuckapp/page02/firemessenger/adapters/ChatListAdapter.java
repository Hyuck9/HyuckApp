package com.lhg1304.hyuckapp.page02.firemessenger.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lhg1304.hyuckapp.R;
import com.lhg1304.hyuckapp.page02.firemessenger.customviews.RoundedImageView;
import com.lhg1304.hyuckapp.page02.firemessenger.models.Chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lhg1304 on 2017-11-15.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatHolder> {

    private ArrayList<Chat> mChatList;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd\naa hh:mm");

    public ChatListAdapter() {
        mChatList = new ArrayList<>();
    }

    public void addItem(Chat chat) {
        mChatList.add(chat);
        notifyDataSetChanged();
    }

    public Chat getItem(int position) {
        return this.mChatList.get(position);
    }

    @Override
    public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_02_fragment_messenger_chat_item, parent, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatHolder holder, int position) {

        Chat item = getItem(position);

        // chatThumbnailView

        holder.lastMessageView.setText(item.getLastMessage().getMessageText());
        holder.titleView.setText(item.getTitle());
        holder.lastMessageDateView.setText(sdf.format(item.getCreateDate()));
        if ( item.getTotalUnreadCount() > 0 ) {
            holder.totalUnreadCountView.setText(String.valueOf(item.getTotalUnreadCount()));
        }

    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }



    public static class ChatHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.messenger_chat_item_thumb)
        RoundedImageView chatThumbnailView;

        @BindView(R.id.messenger_chat_item_title)
        TextView titleView;

        @BindView(R.id.messenger_chat_item_last_message)
        TextView lastMessageView;

        @BindView(R.id.messenger_chat_item_total_unread_count)
        TextView totalUnreadCountView;

        @BindView(R.id.messenger_chat_item_last_message_date)
        TextView lastMessageDateView;

        public ChatHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);
        }
    }

}
