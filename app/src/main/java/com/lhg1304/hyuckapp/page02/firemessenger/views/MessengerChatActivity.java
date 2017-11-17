package com.lhg1304.hyuckapp.page02.firemessenger.views;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.lhg1304.hyuckapp.R;
import com.lhg1304.hyuckapp.page02.firemessenger.adapters.MessageListAdapter;
import com.lhg1304.hyuckapp.page02.firemessenger.models.Chat;
import com.lhg1304.hyuckapp.page02.firemessenger.models.Message;
import com.lhg1304.hyuckapp.page02.firemessenger.models.PhotoMessage;
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

    @BindView(R.id.messenger_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.messenger_chat_rec_view)
    RecyclerView mChatRecyclerView;

    private MessageListAdapter mMessageListAdapter;

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
        mToolbar.setTitleTextColor(Color.WHITE);

        if ( mChatId != null ) {
            mChatDBRef = mFirebaseDatabase.getReference("users").child(mFirebaseUser.getUid()).child("chats").child(mChatId);
            mChatMemberDBRef = mFirebaseDatabase.getReference("chat_members").child(mChatId);
            mChatMessageDBRef = mFirebaseDatabase.getReference("chat_messages").child(mChatId);
            mChatDBRef.child("title").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String title = dataSnapshot.getValue(String.class);
                    mToolbar.setTitle(title);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            initTotalUnreadCount();
        } else {
            mChatDBRef = mFirebaseDatabase.getReference("users").child(mFirebaseUser.getUid()).child("chats");
        }

        mMessageListAdapter = new MessageListAdapter();
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatRecyclerView.setAdapter(mMessageListAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ( mChatId != null ) {
            removeMessageListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( mChatId != null ) {
            addMessageListener();
        }
    }

    private void initTotalUnreadCount() {
        mChatDBRef.child("totalUnreadCount").setValue(0);
    }

    ChildEventListener mMessageEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // 신규 메시지
            Message item = dataSnapshot.getValue(Message.class);

            // 읽음 처리
            // chat_messages > {chat_id} > {message_id} > readUserList
            // 내가 존재 하는지 확인
            List<String> readUserUidList = item.getReadUserList();
            if ( readUserUidList != null ) {
                // 존재 하지 않는다면
                if ( !readUserUidList.contains(mFirebaseUser.getUid()) ) {
                    // 공유된 자원을 업데이트 할 때에는 트랜젝션 처리
                    dataSnapshot.getRef().runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Message mutableMessage = mutableData.getValue(Message.class);
                            // readUserList에 내 uid 추가
                            // chat_messages > {chat_id} > {message_id} > unReadCount -= 1
                            List<String> mutableReadUserList = mutableMessage.getReadUserList();
                            mutableReadUserList.add(mFirebaseUser.getUid());
                            int mutableUnreadCount = mutableMessage.getUnreadCount() - 1;

                            if ( mutableMessage.getMessageType() == Message.MessageType.PHOTO ) {
                                PhotoMessage mutablePhotoMessage = mutableData.getValue(PhotoMessage.class);
                                mutablePhotoMessage.setReadUserList(mutableReadUserList);
                                mutablePhotoMessage.setUnreadCount(mutableUnreadCount);
                                mutableData.setValue(mutablePhotoMessage);
                            } else if ( mutableMessage.getMessageType() == Message.MessageType.TEXT ) {
                                TextMessage mutableTextMessage = mutableData.getValue(TextMessage.class);
                                mutableTextMessage.setReadUserList(mutableReadUserList);
                                mutableTextMessage.setUnreadCount(mutableUnreadCount);
                                mutableData.setValue(mutableTextMessage);
                            }

                            // Transaction.success()시 onComplete 호출됨
                            return Transaction.success(mutableData);

                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            initTotalUnreadCount();
                        }
                    });

                }

            }

            // UI 처리
            if ( item.getMessageType() == Message.MessageType.TEXT ) {
                TextMessage textMessage = dataSnapshot.getValue(TextMessage.class);
                mMessageListAdapter.addItem(textMessage);
            } else if ( item.getMessageType() == Message.MessageType.PHOTO ) {
                PhotoMessage photoMessage = dataSnapshot.getValue(PhotoMessage.class);
                mMessageListAdapter.addItem(photoMessage);
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            // 변경된 메시지 (unreadCount)
            // 아답터쪽에 변경된 메세지 데이터를 전달
            // 메세지 아이디 번호로 해당 메세지의 위치를 알아내서
            // 알아낸 위치값을 이용하여 메세지 리스트의 값을 변경
            Message item = dataSnapshot.getValue(Message.class);

            if ( item.getMessageType() == Message.MessageType.TEXT ) {
                TextMessage textMessage = dataSnapshot.getValue(TextMessage.class);
                mMessageListAdapter.addItem(textMessage);
            } else if ( item.getMessageType() == Message.MessageType.PHOTO ) {
                PhotoMessage photoMessage = dataSnapshot.getValue(PhotoMessage.class);
                mMessageListAdapter.addItem(photoMessage);
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void addMessageListener() {
        mChatMessageDBRef.addChildEventListener(mMessageEventListener);
    }

    private void removeMessageListener() {
        mChatMessageDBRef.removeEventListener(mMessageEventListener);
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
        textMessage.setMessageUser(new User(mFirebaseUser.getUid(), mFirebaseUser.getEmail(), mFirebaseUser.getDisplayName(), mFirebaseUser.getPhotoUrl().toString()));
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
