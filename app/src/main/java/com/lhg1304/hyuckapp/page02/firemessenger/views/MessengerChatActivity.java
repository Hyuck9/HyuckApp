package com.lhg1304.hyuckapp.page02.firemessenger.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lhg1304.hyuckapp.R;
import com.lhg1304.hyuckapp.page02.firemessenger.models.Chat;
import com.lhg1304.hyuckapp.page02.firemessenger.models.Message;
import com.lhg1304.hyuckapp.page02.firemessenger.models.TextMessage;
import com.lhg1304.hyuckapp.page02.firemessenger.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessengerChatActivity extends AppCompatActivity {

    private String mChatId;

    @BindView(R.id.messenger_sender_btn)
    ImageView mSenderButton;

    @BindView(R.id.messenger_edt_content)
    EditText mMessageText;

    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mChatDBRef;
    private DatabaseReference mChatMemberDBRef;
    private DatabaseReference mChatMessageDBRef;
    private DatabaseReference mUserDBRef;

    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_02_messenger_content_chat);
        ButterKnife.bind(this);

        mChatId = getIntent().getStringExtra("chat_id");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserDBRef = mFirebaseDatabase.getReference("users");

    }

    @OnClick(R.id.messenger_sender_btn)
    public void onSendEvent(View v) {

        if ( mChatId != null ) {
            sendMessage();
        } else {
            createChatRoom();
        }
    }

    private void sendMessage() {
        // 메세지 키 생성
        mChatMessageDBRef = mFirebaseDatabase.getReference("chat_messages").child(mChatId);
        // chat_message > {chat_id} > {message_id} > messageInfo
        String messageId = mChatMessageDBRef.push().getKey();
        String messageText = mMessageText.getText().toString();

        if ( messageText.isEmpty() ) {
            return;
        }

        final TextMessage textMessage = new TextMessage();
        textMessage.setMessageText(messageText);
        textMessage.setMessageDate(new Date());
        textMessage.setChatId(mChatId);
        textMessage.setMessageId(messageId);
        textMessage.setMessageType(Message.MessageType.TEXT);
        textMessage.setReadUserList(Arrays.asList(new String[]{mFirebaseUser.getUid()}));
        String [] uids = getIntent().getStringArrayExtra("uids");
        if ( uids != null ) {
            textMessage.setUnreadCount(uids.length-1);
        }
        mMessageText.setText("");
        mChatMemberDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // unreadCount 셋팅하기 위한 대화 상대의 수를 가져온다.
                long memberCount = dataSnapshot.getChildrenCount();
                textMessage.setUnreadCount((int) memberCount - 1);
                mChatMessageDBRef.child(textMessage.getMessageId()).setValue(textMessage, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                        while( memberIterator.hasNext() ) {
                            User chatMember = memberIterator.next().getValue(User.class);
                            mUserDBRef.child(chatMember.getUid())
                                    .child("chats")
                                    .child(mChatId)
                                    .child("lastMessage")
                                    .setValue(textMessage);

                            if ( !chatMember.getUid().equals(mFirebaseUser.getUid()) ) {
                                mUserDBRef.child(chatMember.getUid())
                                        .child("chats")
                                        .child(mChatId)
                                        .child("totalUnreadCount")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                long totalUnreadCount = dataSnapshot.getValue(long.class);
                                                dataSnapshot.getRef().setValue(totalUnreadCount+1);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    private boolean isSentMessage = false;
    private void createChatRoom() {
        // <방 생성>
        // 방 정보 설정 <-- 기존 방이어야 가능
        // 대화 상대에 내가 선택한 사람 추가
        // 각 상대별 chats에 방 추가
        // 메시지 정보 중 읽은 사람에 내 정보를 추가
        // 첫 메시지 전송

        final Chat chat = new Chat();
        mChatDBRef = mFirebaseDatabase.getReference("users").child(mFirebaseUser.getUid()).child("chats");
        mChatId = mChatDBRef.push().getKey();    // users > {uid} > chats > {chat_uid} 의 Key값
        mChatMemberDBRef = mFirebaseDatabase.getReference("chat_members").child(mChatId);

        chat.setChatId(mChatId);
        chat.setCreateDate(new Date());

        String uid = getIntent().getStringExtra("uid");
        String[] uids = getIntent().getStringArrayExtra("uids");
        if ( uid != null ) {
            // 1:1
            uids = new String[]{uid};
        }

        List<String> uidList = new ArrayList<>(Arrays.asList(uids));
        uidList.add(mFirebaseUser.getUid());

        for ( String userId : uidList ) {
            // uid > userInfo
            mUserDBRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    User member = dataSnapshot.getValue(User.class);

                    mChatMemberDBRef.child(member.getUid())
                            .setValue(member, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    // users > {uid} > chats > {chat_id} > chatinfo
                                    dataSnapshot.getRef().child("chats").child(mChatId).setValue(chat);
                                    if ( !isSentMessage ) {
                                        sendMessage();
                                        isSentMessage = true;
                                    }
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


}
