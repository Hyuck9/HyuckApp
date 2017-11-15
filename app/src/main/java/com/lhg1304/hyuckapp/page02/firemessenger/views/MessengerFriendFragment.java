package com.lhg1304.hyuckapp.page02.firemessenger.views;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lhg1304.hyuckapp.R;
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

    private FirebaseUser mFirebaseUser;

    private FirebaseAuth mFirebaseAuth;

    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mFriendsDBRef;
    private DatabaseReference mUserDBRef;

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

        return friendView;
    }

    public void toggleSearchBar() {
        mSearchArea.setVisibility( mSearchArea.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE );
    }

    @OnClick(R.id.messenger_btn_find)
    private void addFriend() {

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

}
