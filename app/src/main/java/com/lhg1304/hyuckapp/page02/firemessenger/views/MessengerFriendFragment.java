package com.lhg1304.hyuckapp.page02.firemessenger.views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lhg1304.hyuckapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessengerFriendFragment extends Fragment {

    @BindView(R.id.messenger_search_area)
    LinearLayout mSearchArea;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View friendView = inflater.inflate(R.layout.page_02_fragment_messenger_friend, container, false);
        ButterKnife.bind(this, friendView);

        return friendView;
    }

    public void toggleSearchBar() {
        mSearchArea.setVisibility( mSearchArea.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE );
    }

}
