package com.lhg1304.hyuckapp.page02.firemessenger.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lhg1304.hyuckapp.R;
import com.lhg1304.hyuckapp.page02.firemessenger.models.User;

public class MessengerLoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private ProgressBar mProgressView;

    private SignInButton mSignInButton;

    private GoogleApiClient mGoogleAPIClient;

    private GoogleSignInOptions mGoogleSignInOptions;

    private FirebaseAuth mAuth;

    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mUserRef;

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final int GOOGLE_LOGIN_OPEN = 100;

    private static final String TAG = "MessengerLoginActivity";

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_02_messenger_login);

        mProgressView = (ProgressBar) findViewById(R.id.messenger_login_progress);
        mSignInButton = (SignInButton) findViewById(R.id.btn_messenger_sign_in);
        mAuth = FirebaseAuth.getInstance();
        if ( mAuth.getCurrentUser() != null ) {
            startActivity(new Intent(this, MessengerMainActivity.class));
            finish();
            return;
        }
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserRef = mFirebaseDatabase.getReference("users");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleAPIClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // 실패 시 처리 하는 부분
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_LOGIN_OPEN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle : " + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete : " + task.isSuccessful());

                        if ( task.isComplete() ) {
                            if ( task.isSuccessful() ) {
                                FirebaseUser firebaseUser = task.getResult().getUser();
                                final User user = new User();
                                user.setEmail(firebaseUser.getEmail());
                                user.setName(firebaseUser.getDisplayName());
                                user.setUid(firebaseUser.getUid());
                                if ( firebaseUser.getPhotoUrl() != null ) {
                                    user.setProfileUrl(firebaseUser.getPhotoUrl().toString());
                                }
                                mUserRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if ( !dataSnapshot.exists() ) {
                                            // 데이터가 존재하지 않을때 친구 생성
                                            mUserRef.child(user.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if ( databaseError == null ) {
                                                        startActivity(new Intent(MessengerLoginActivity.this, MessengerMainActivity.class));
                                                        finish();
                                                    }
                                                }
                                            });
                                        } else {
                                            // 존재한다면 메신저 메인 액티비티 호출
                                            startActivity(new Intent(MessengerLoginActivity.this, MessengerMainActivity.class));
                                            finish();
                                        }

                                        Bundle eventBundle = new Bundle();
                                        eventBundle.putString("email", user.getEmail());
                                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, eventBundle);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });



                            } else {
                                Log.w(TAG, "signInWithCredential", task.getException());
                                Snackbar.make(mProgressView, "Authentication failed.", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                });

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleAPIClient);
        startActivityForResult(signInIntent, GOOGLE_LOGIN_OPEN);
    }
}
