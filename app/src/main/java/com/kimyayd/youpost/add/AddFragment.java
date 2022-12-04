package com.kimyayd.youpost.add;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kimyayd.youpost.R;

import java.util.ArrayList;

public class AddFragment extends Fragment {
    private static final String TAG = "AddFragment";
    private static final int ACTIVITY_NUM = 2;
    private Context mContext=getContext();
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    private ViewPager2 mViewPager;
    private BottomNavigationView view;
    private ArrayList<Fragment> arr;
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_add, container, false);
        Log.d(TAG, "onCreate: starting.");

        LinearLayout textLayout = view.findViewById(R.id.layoutText);
        LinearLayout photoLayout = view.findViewById(R.id.layoutPhoto);
        LinearLayout videoLayout = view.findViewById(R.id.layoutVideo);

        textLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getContext(),AddTextActivity.class));

            }
        });

        photoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getContext(),AddActivity.class));

            }
        });

        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),AddVideoActivity.class));
            }
        });




        return view;
    }


}