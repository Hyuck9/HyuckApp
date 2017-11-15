package com.lhg1304.hyuckapp.page02.firemessenger.views;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lhg1304.hyuckapp.R;
import com.lhg1304.hyuckapp.page02.firemessenger.adapters.FriendListAdapter;
import com.lhg1304.hyuckapp.page02.firemessenger.customviews.RecyclerViewItemClickListener;
import com.lhg1304.hyuckapp.page02.firemessenger.models.User;

import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessengerFriendFragment extends Fragment {

    @BindView(R.id.messenger_search_area)
    LinearLayout mSearchArea;

    @BindView(R.id.messenger_et_content)
    EditText etEmail;

    @BindView(R.id.messenger_friend_recycleview)
    RecyclerView mRecyclerView;

    private FirebaseUser mFirebaseUser;

    private FirebaseAuth mFirebaseAuth;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFriendsDBRef;

    private DatabaseReference mUserDBRef;

    private FriendListAdapter mFriendListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View friendView = inflater.inflate(R.layout.page_02_fragment_messenger_friend, container, false);
        ButterKnife.bind(this, friendView);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mFriendsDBRef = mFirebaseDatabase.getReference("users").child(mFirebaseUser.getUid()).child("friends");
        mUserDBRef = mFirebaseDatabase.getReference("users");

        // RDB에서 나의 친구목록을 리스트를 통하여 데이터를 가져온다.
        addFriendListener();
        // 가져온 데이터를 통해 recyclerview의 아답터에 아이템을 추가시켜준다. (UI갱신)
        mFriendListAdapter = new FriendListAdapter();
        mRecyclerView.setAdapter(mFriendListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // 아이템별 (친구) 클릭이벤트를 주어서 선택한 친구와 대화를 할 수 있도록 한다.
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final User friend = mFriendListAdapter.getItem(position);
                if ( mFriendListAdapter.getSelectionMode() == FriendListAdapter.UNSELECTION_MODE ) {
                    // 1:1 채팅 체크 모드
                    Snackbar.make(view, friend.getName()+"님과 대화를 하시겠습니까?",Snackbar.LENGTH_LONG).setAction("예", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent chatIntent = new Intent(getActivity(), MessengerChatActivity.class);
                            chatIntent.putExtra("uids", mFriendListAdapter.getSelectedUids());
                            startActivity(chatIntent);
                        }
                    }).show();
                } else {
                    // 1:N 채팅 체크 모드
                    friend.setSelection(friend.isSelection() ? false : true);
                    int selectedUserCount = mFriendListAdapter.getSelectionUserCount();
                    Snackbar.make(view, selectedUserCount+"명과 대화를 하시겠습니까?",Snackbar.LENGTH_LONG).setAction("예", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent chatIntent = new Intent(getActivity(), MessengerChatActivity.class);
                            chatIntent.putExtra("uid", friend.getUid());
                            startActivity(chatIntent);
                        }
                    }).show();
                }

            }
        }));

        return friendView;
    }

    public void toggleSearchBar() {
        mSearchArea.setVisibility( mSearchArea.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE );
    }

    public void toggleSelectionMode() {
        mFriendListAdapter.setSelectionMode(
                mFriendListAdapter.getSelectionMode() == FriendListAdapter.SELECTION_MODE ? FriendListAdapter.UNSELECTION_MODE : FriendListAdapter.SELECTION_MODE
        );
    }

    @OnClick(R.id.messenger_btn_find)
    void addFriend() {

        // 입력된 이메일 가져옴
        final String inputEmail = etEmail.getText().toString();
        // 이메일이 입력되지 않았다면 이메일 입력하라는 메시지 띄움
        if ( inputEmail.isEmpty() ) {
            Snackbar.make(mSearchArea, "이메일을 입력해주세요.", Snackbar.LENGTH_LONG).show();
            return;
        }
        // 자기 자신을 친구로 등록할 수 없기 때문에 FirebaseUser의 이메일이 입력한 이메일과 같다면, 자기자신은 등록 불가 메시지를 띄움
        if ( inputEmail.equals(mFirebaseUser.getEmail()) ) {
            Snackbar.make(mSearchArea, "자기자신은 친구로 등록할 수 없습니다.", Snackbar.LENGTH_LONG).show();
            return;
        }
        // 이메일이 정상이라면 나의 정보를 조회하여 이미 등록된 친구인지를 판단하고
        mFriendsDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> friendsIterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> friendsIterator = friendsIterable.iterator();

                while ( friendsIterator.hasNext() ) {
                    User user = friendsIterator.next().getValue(User.class);

                    if ( user.getEmail().equals(inputEmail) ) {
                        Snackbar.make(mSearchArea, "이미 등록된 친구입니다.", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }

                // users db에 존재하지 않는 이메일이라면, 가입하지 않은 친구라는 메시지를 띄워주고
                mUserDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> userIterator = dataSnapshot.getChildren().iterator();
                        int userCount = (int) dataSnapshot.getChildrenCount();
                        int loopCount = 1;

                        while ( userIterator.hasNext() ) {
                            final User currentUser = userIterator.next().getValue(User.class);

                            if ( inputEmail.equals(currentUser.getEmail()) ) {
                                // 친구 등록 로직
                                // users/{my_uid}/friends/{someone_uid}/firebasePush/상대정보 등록
                                mFriendsDBRef.push().setValue(currentUser, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        // 나의 정보를 가져온다.
                                        mUserDBRef.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                User user = dataSnapshot.getValue(User.class);
                                                // users/{someone_uid}/friends/{my_uid}/내정보 등록
                                                mUserDBRef.child(currentUser.getUid()).child("friends").push().setValue(user);
                                                Snackbar.make(mSearchArea, "친구등록이 완료되었습니다.", Snackbar.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                });


                            } else {
                                // 총 사용자의 명수 == loopCount
                                if ( userCount <= loopCount++ ) {
                                    // 등록된 사용자가 없다는 메시지 출력
                                    Snackbar.make(mSearchArea, "가입을 하지 않은 친구입니다.", Snackbar.LENGTH_LONG).show();
                                    return;
                                }
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addFriendListener() {

        mFriendsDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User friend = dataSnapshot.getValue(User.class);
                drawUI(friend);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

    private void drawUI(User friend) {
        mFriendListAdapter.addItem(friend);
    }

}
