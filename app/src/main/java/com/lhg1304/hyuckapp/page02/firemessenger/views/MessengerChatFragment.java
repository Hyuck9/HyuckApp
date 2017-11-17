package com.lhg1304.hyuckapp.page02.firemessenger.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.lhg1304.hyuckapp.page02.firemessenger.models.Chat;
import com.lhg1304.hyuckapp.page02.firemessenger.models.User;

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

    private DatabaseReference mChatMemberDBRef;

    @BindView(R.id.messenger_chat_recyclerview)
    RecyclerView mChatRecyclerView;

    private ChatListAdapter mChatListAdapter;

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
        mChatListAdapter = new ChatListAdapter();
        mChatRecyclerView.setAdapter(mChatListAdapter);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        addChatListener();

        return chatView;
    }

    private void addChatListener() {
        mChatDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot chatDataSnapshot, String s) {

                // 방에 대한 정보를 얻어오고
                final Chat chatRoom = chatDataSnapshot.getValue(Chat.class);
                mChatMemberDBRef.child(chatRoom.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long memberCount = dataSnapshot.getChildrenCount();
                        Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                        StringBuffer memberStringBuffer = new StringBuffer();

                        int loopCount = 1;
                        while( memberIterator.hasNext() ) {
                            User member = memberIterator.next().getValue(User.class);
                            memberStringBuffer.append(member.getName());

                            if ( loopCount < memberCount ) {
                                memberStringBuffer.append(", ");
                            }

                            if ( loopCount == memberCount ) {
                                String title = memberStringBuffer.toString();
                                if ( chatRoom.getTitle() != null ) {
                                    if ( !chatRoom.getTitle().equals(title) ) {
                                        // users > {uid} > chats > {chat_id} > title
                                        chatDataSnapshot.getRef().child("title").setValue(title);
                                    }
                                }
                                // UI 갱신 시켜주는 메서드로 방의 정보를 전달
                                drawUI( chatRoom );
                            }
                            loopCount++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // 기존의 방제목과 방 멤버의 이름들을 가져와서 타이틀화 시켰을 때 같지 않은 경우 방제목을 업데이트


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // 변경된 방의 정보를 수신
                // 내가 보낸 메시지가 아닌 경우와 마지막 메시지가 수정이 되었다면 -> 노티출력
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
        });
    }

    private void drawUI(Chat chat) {
        mChatListAdapter.addItem(chat);
    }

}
