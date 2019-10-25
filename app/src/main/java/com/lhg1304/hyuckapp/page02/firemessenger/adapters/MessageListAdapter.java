package com.lhg1304.hyuckapp.page02.firemessenger.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.lhg1304.hyuckapp.R;
import com.lhg1304.hyuckapp.page02.firemessenger.customviews.RoundedImageView;
import com.lhg1304.hyuckapp.page02.firemessenger.models.Message;
import com.lhg1304.hyuckapp.page02.firemessenger.models.PhotoMessage;
import com.lhg1304.hyuckapp.page02.firemessenger.models.TextMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lhg1304 on 2017-11-17.
 */

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    private ArrayList<Message> mMessageList;

    private SimpleDateFormat messageDateFormat = new SimpleDateFormat("MM/dd\naa hh:mm");

    private String userId;

    public MessageListAdapter() {
        mMessageList = new ArrayList<>();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void addItem(Message item) {
        mMessageList.add(item);
        notifyDataSetChanged();
    }

    public void updateItem(Message item) {
        int position = getItemPosition(item.getMessageId());
        if ( position < 0 ) {
            return;
        }
        mMessageList.set(position, item);
        notifyItemChanged(position);
    }

    public void clearItem() {
        mMessageList.clear();
    }

    private int getItemPosition(String messageId) {
        int position = 0;
        for (Message message : mMessageList) {
            if ( message.getMessageId().equals(messageId) ) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public Message getItem(int position) {
        return mMessageList.get(position);
    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_02_fragment_messenger_message_item, parent, false);

        // view를 이용한 뷰홀더 리턴
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        // 전달받은 뷰홀더를 이용한 뷰 구현
        Message item = getItem(position);

        TextMessage textMessage = null;
        PhotoMessage photoMessage = null;

        if ( item instanceof TextMessage ) {
            textMessage = (TextMessage) item;
        } else if ( item instanceof PhotoMessage ) {
            photoMessage = (PhotoMessage) item;
        }

        // 내가 보낸 메세지 인지, 받은 메세지 인지 판별
        if ( userId.equals(item.getMessageUser().getUid()) ) {
            // 내가 보낸 메세지 구현
            // 텍스트 메세지 인지 포토 메세지 인지 구별
            if ( item.getMessageType() == Message.MessageType.TEXT ) {
                holder.sendText.setText(textMessage.getMessageText());

                holder.sendText.setVisibility(View.VISIBLE);
                holder.sendImage.setVisibility(View.GONE);

            } else if ( item.getMessageType() == Message.MessageType.PHOTO ) {
                Glide.with(holder.sendArea)
                        .load(photoMessage.getPhotoUrl())
                        .into(holder.sendImage);

                holder.sendText.setVisibility(View.GONE);
                holder.sendImage.setVisibility(View.VISIBLE);
            }

            if ( item.getUnreadCount() > 0 ) {
                holder.sendUnreadCount.setText(String.valueOf(item.getUnreadCount()));
            } else {
                holder.sendUnreadCount.setText("");
            }
            holder.sendDate.setText(messageDateFormat.format(item.getMessageDate()));
            holder.yourArea.setVisibility(View.GONE);
            holder.sendArea.setVisibility(View.VISIBLE);
            holder.exitArea.setVisibility(View.GONE);

        } else {
            // 상대방이 보낸 경우
            // 텍스트 메세지 인지 포토 메세지 인지 구별
            if ( item.getMessageType() == Message.MessageType.TEXT ) {
                holder.rcvText.setText(textMessage.getMessageText());
                holder.rcvText.setVisibility(View.VISIBLE);
                holder.rcvImage.setVisibility(View.GONE);

            } else if ( item.getMessageType() == Message.MessageType.PHOTO ) {
                Glide.with(holder.yourArea)
                        .load(photoMessage.getPhotoUrl())
                        .into(holder.rcvImage);

                holder.rcvText.setVisibility(View.GONE);
                holder.rcvImage.setVisibility(View.VISIBLE);
            } else if ( item.getMessageType() == Message.MessageType.EXIT ) {
                holder.exitText.setText(String.format("%s님이 방에서 나가셨습니다.", item.getMessageUser().getName()));
            }

            if ( item.getUnreadCount() > 0 ) {
                holder.rcvUnreadCount.setText(String.valueOf(item.getUnreadCount()));
            } else {
                holder.rcvUnreadCount.setText("");
            }

            if ( item.getMessageUser().getProfileUrl() != null ) {
                Glide.with(holder.yourArea)
                        .load(item.getMessageUser().getProfileUrl())
                        .into(holder.rcvProfileView);
            }

            if ( item.getMessageType() == Message.MessageType.EXIT ) {
                holder.sendArea.setVisibility(View.GONE);
                holder.yourArea.setVisibility(View.GONE);
                holder.exitArea.setVisibility(View.VISIBLE);

            } else {
                holder.rcvDate.setText(messageDateFormat.format(item.getMessageDate()));
                holder.sendArea.setVisibility(View.GONE);
                holder.yourArea.setVisibility(View.VISIBLE);

            }

        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }



    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.messenger_message_item_your_chat_area)
        LinearLayout yourArea;

        @BindView(R.id.messenger_message_item_my_chat_area)
        LinearLayout sendArea;

        @BindView(R.id.messenger_message_item_exit_area)
        LinearLayout exitArea;

        @BindView(R.id.messenger_message_item_rcv_profile)
        RoundedImageView rcvProfileView;

        @BindView(R.id.messenger_message_item_exit_txt)
        TextView exitText;

        @BindView(R.id.messenger_message_item_rcv_txt)
        TextView rcvText;

        @BindView(R.id.messenger_message_item_rcv_image)
        ImageView rcvImage;

        @BindView(R.id.messenger_message_item_rcv_unread_count)
        TextView rcvUnreadCount;

        @BindView(R.id.messenger_message_item_rcv_date)
        TextView rcvDate;

        @BindView(R.id.messenger_message_item_send_txt)
        TextView sendText;

        @BindView(R.id.messenger_message_item_send_image)
        ImageView sendImage;

        @BindView(R.id.messenger_message_item_send_unread_count)
        TextView sendUnreadCount;

        @BindView(R.id.messenger_message_item_send_date)
        TextView sendDate;



        public MessageViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }
}
