package com.lhg1304.hyuckapp.page02.firemessenger.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lhg1304.hyuckapp.R;
import com.lhg1304.hyuckapp.page02.firemessenger.adapters.ChatListAdapter;
import com.lhg1304.hyuckapp.page02.firemessenger.customviews.RecyclerViewItemClickListener;
import com.lhg1304.hyuckapp.page02.firemessenger.models.Chat;
import com.lhg1304.hyuckapp.page02.firemessenger.models.ExitMessage;
import com.lhg1304.hyuckapp.page02.firemessenger.models.Message;
import com.lhg1304.hyuckapp.page02.firemessenger.models.Notification;
import com.lhg1304.hyuckapp.page02.firemessenger.models.User;

import java.util.Date;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessengerChatFragment extends Fragment {

    private FirebaseUser mFirebaseUser;

    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mChatDBRef;
    private DatabaseReference mChatMessageDBRef;
    private DatabaseReference mChatMemberDBRef;

    @BindView(R.id.messenger_chat_recyclerview)
    RecyclerView mChatRecyclerView;

    private ChatListAdapter mChatListAdapter;

    public static String JOINED_ROOM = "";

    public static final int JOIN_ROOM_REQUEST_CODE = 100;

    private Notification mNotification;

    private Context mContext;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View chatView = inflater.inflate(R.layout.page_02_fragment_messenger_chat, container, false);
        ButterKnife.bind(this, chatView);


        // 채팅방 리스너 부착
        // user > {my_uid} > chats
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mChatDBRef = mFirebaseDatabase.getReference("users").child(mFirebaseUser.getUid()).child("chats");
        mChatMemberDBRef = mFirebaseDatabase.getReference("chat_members");
        mChatMessageDBRef = mFirebaseDatabase.getReference("chat_messages");
        mChatListAdapter = new ChatListAdapter();
        mChatListAdapter.setFragment(this);
        mChatRecyclerView.setAdapter(mChatListAdapter);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        mChatRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Chat chat = mChatListAdapter.getItem(position);
                Intent chatIntent = new Intent(getActivity(), MessengerChatActivity.class);
                chatIntent.putExtra("chat_id", chat.getChatId());
                startActivityForResult(chatIntent, JOIN_ROOM_REQUEST_CODE);
            }
        }));
        mContext = getActivity();
        mNotification = new Notification(mContext);

        addChatListener();
        return chatView;
    }

    private void addChatListener() {
        mChatDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot chatDataSnapshot, String s) {
                // UI 갱신 시켜주는 메서드로 방의 정보를 전달.
                // 방에 대한 정보를 얻어오고
                // 기존의 방제목과 방 멤버의 이름들을 가져와서 타이틀화 시켰을 때 같지 않은 경우 방제목을 업데이트
                drawUI(chatDataSnapshot, DrawType.ADD);
            }

            @Override
            public void onChildChanged(DataSnapshot chatDataSnapshot, String s) {
                // 내가 보낸 메시지가 아닌 경우와 마지막 메시지가 수정이 되었다면 -> 노티출력

                // 변경된 방의 정보를 수신
                // 변경된 포지션 확인 (채팅방 아이디로 기존의 포지션 확인)
                // 그 포지션의 아이템 중 unreadCount가 변경이 되었다면 unreadCount 변경
                // 현재 Activity가 MessengerChatActivity 이고 chat_id가  같다면 노티는 해주지 않음
                // totalUnreadCount변경, title변경, lastMessage변경시 onChildChanged 호출됨
                drawUI(chatDataSnapshot, DrawType.UPDATE);
                Chat updatedChat = chatDataSnapshot.getValue(Chat.class);

                if ( updatedChat.getLastMessage() != null ) {
//                    if ( updatedChat.getLastMessage().getMessageType() ==Message.MessageType.EXIT ) {
//                        return;
//                    }
                    // 내가 보낸 메시지는 내가 노티 받지 않아야 하기 때문에
                    if ( !updatedChat.getLastMessage().getMessageUser().getUid().equals(mFirebaseUser.getUid()) ) {
                        // lastMessage의 시각과 변경된 메세지의 lastMessage 시간이 다르다면 -> 노티출력
                        if ( !updatedChat.getChatId().equals(JOINED_ROOM) ) {
                            // 노티 출력
                            Intent chatIntent = new Intent(mContext, MessengerChatActivity.class);
                            chatIntent.putExtra("chat_id", updatedChat.getChatId());
                            mNotification.setData(chatIntent)
                                    .setTitle(updatedChat.getLastMessage().getMessageUser().getName())
                                    .setText(updatedChat.getLastMessage().getMessageText())
                                    .notification();

                            Bundle bundle = new Bundle();
                            bundle.putString("friend", updatedChat.getLastMessage().getMessageUser().getEmail());
                            bundle.putString("me", mFirebaseUser.getEmail());
                            mFirebaseAnalytics.logEvent("notification", bundle);

                        }
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // 방의 실시간 삭제
                Chat item = dataSnapshot.getValue(Chat.class);
                mChatListAdapter.removeItem(item);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void drawUI(final DataSnapshot chatDataSnapshot, final DrawType drawType) {
        // 방에 대한 정보를 얻어오고
        final Chat chatRoom = chatDataSnapshot.getValue(Chat.class);
        mChatMemberDBRef.child(chatRoom.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long memberCount = dataSnapshot.getChildrenCount();
                Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                StringBuffer memberStringBuffer = new StringBuffer();

                if ( memberCount <= 1 ) {
                    chatRoom.setTitle("대화상대가 없는 방입니다.");
                    chatDataSnapshot.getRef().child("title").setValue(chatRoom.getTitle());
                    chatDataSnapshot.getRef().child("disabled").setValue(true);
                    if ( drawType == DrawType.ADD ) {
                        mChatListAdapter.addItem(chatRoom);
                    } else {
                        mChatListAdapter.updateItem(chatRoom);
                    }
                    return;
                }

                int loopCount = 1;
                while( memberIterator.hasNext() ) {
                    User member = memberIterator.next().getValue(User.class);

                    if ( !mFirebaseUser.getUid().equals(member.getUid()) ) {
                        memberStringBuffer.append(member.getName());
                        if ( memberCount - loopCount > 1 ) {
                            memberStringBuffer.append(", ");
                        }
                    }

                    if ( loopCount == memberCount ) {
                        // users > {uid} > chats > {chat_id} > title
                        String title = memberStringBuffer.toString();
                        if ( chatRoom.getTitle() == null ) {
                            chatDataSnapshot.getRef().child("title").setValue(title);
                        } else if ( !chatRoom.getTitle().equals(title) ) {
                            chatDataSnapshot.getRef().child("title").setValue(title);
                        }
                        chatRoom.setTitle(title);
                        if ( drawType == DrawType.ADD ) {
                            mChatListAdapter.addItem(chatRoom);
                        } else {
                            mChatListAdapter.updateItem(chatRoom);
                        }
                    }
                    loopCount++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void leaveChat(final Chat chat) {

        Snackbar.make(getView(), "선택된 대화방을 나가시겠습니까?", Snackbar.LENGTH_LONG).setAction("예", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 나의 대화방 목록에서 제거
                // users > {uid} > chats
                mChatDBRef.child(chat.getChatId()).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        // EXIT 메시지 발송
                        // chat_messages > {chat_id} > {message_id} > 내용
                        final ExitMessage exitMessage = new ExitMessage();
                        String messageId = mChatMessageDBRef.push().getKey();
                        exitMessage.setMessageUser(new User(mFirebaseUser.getUid(), mFirebaseUser.getEmail(), mFirebaseUser.getDisplayName(), mFirebaseUser.getPhotoUrl().toString()));
                        exitMessage.setMessageDate(new Date());
                        exitMessage.setMessageId(messageId);
                        exitMessage.setChatId(chat.getChatId());
                        mChatMessageDBRef.child(chat.getChatId()).push().setValue(exitMessage);

                        // 채팅 멤버 목록에서 제거
                        // chat_members > {chat_id} > {user_id} 제거
                        mChatMemberDBRef.child(chat.getChatId()).child(mFirebaseUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                // 메시지 unReadCount에서도 제거
                                // getReadUserList에 내가 있다면 읽어진거니까 pass
                                // 없다면 unReadCount -= 1
                                // messages > {chat_id} 모든 메시지를 가져온다.
                                // 가져와서 루프를 통해 내가 읽었는지 여부 판단

                                Bundle bundle = new Bundle();
                                bundle.putString("me", mFirebaseUser.getEmail());
                                bundle.putString("roomId", chat.getChatId());
                                mFirebaseAnalytics.logEvent("leaveChatRoom", bundle);

                                // 채팅방의 멤버정보를 받아와서
                                // 채팅방의 정보를 가져오고 (각각)
                                // 그 정보의 라스트 메시지를 수정
                                mChatMemberDBRef.child(chat.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterator<DataSnapshot> chatMemberIterator = dataSnapshot.getChildren().iterator();
                                        while ( chatMemberIterator.hasNext() ) {
                                            User chatMember = chatMemberIterator.next().getValue(User.class);

                                            // users > {uid} > chats > {chat_id} > lastMessage
                                            mFirebaseDatabase
                                                    .getReference("users")
                                                    .child(chatMember.getUid())
                                                    .child("chats")
                                                    .child(chat.getChatId())
                                                    .child("lastMessage")
                                                    .setValue(exitMessage);

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });



                                mFirebaseDatabase.getReference("messages").child(chat.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterator<DataSnapshot> messageIterator = dataSnapshot.getChildren().iterator();

                                        while( messageIterator.hasNext() ) {
                                            DataSnapshot messageSnapshot = messageIterator.next();
                                            Message currentMessage = messageSnapshot.getValue(Message.class);
                                            if ( !currentMessage.getReadUserList().contains(mFirebaseUser.getUid()) ) {
                                                // 모두 읽어도 1이 떠있는 것을 방지하기 위해
                                                messageSnapshot.child("unreadCount").getRef().setValue(currentMessage.getUnreadCount() - 1);
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });

                        /**
                         * 대화방의 타이틀이 변경됨을 알려주어 채팅방의 리스너가 감지하게 하여 방 이름을 업데이트
                         * 방의 제목은 방의 업데이트, 추가되었을때의 리스너가 처리하기때문에 방 제목만 변경시켜서 변경이 되었음만 알리면 됨
                         */
                        mChatMemberDBRef.child(chat.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();

                                while ( memberIterator.hasNext() ) {
                                    // 방 참여자의 UID를 가져오기 위해 user 정보 조회
                                    User chatMember = memberIterator.next().getValue(User.class);
                                    // 해당 참여자의 방 정보의 업데이트를 위하여 방이름을 임의로 업데이트
                                    mFirebaseDatabase.getReference("users")
                                            .child(chatMember.getUid())
                                            .child("chats")
                                            .child(chat.getChatId())
                                            .child("title")
                                            .setValue("");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == JOIN_ROOM_REQUEST_CODE ) {
            JOINED_ROOM = "";
        }
    }

    private enum DrawType {
        ADD, UPDATE
    }
}
