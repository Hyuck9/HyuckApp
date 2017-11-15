package com.lhg1304.hyuckapp.page02.firemessenger.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lhg1304.hyuckapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessengerChatFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.page_02_fragment_messenger_chat, container, false);
    }
}
