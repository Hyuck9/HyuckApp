package com.lhg1304.hyuckapp.page02.firemessenger.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lhg1304.hyuckapp.R;

public class MessengerChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_02_messenger_content_chat);

        String uid = getIntent().getStringExtra("uid");
        String[] uids = getIntent().getStringArrayExtra("uids");
    }
}
