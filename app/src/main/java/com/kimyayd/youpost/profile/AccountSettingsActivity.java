package com.kimyayd.youpost.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.utils.FirebaseMethods;
import com.kimyayd.youpost.utils.SectionsStatePagerAdapter;

import java.util.ArrayList;

public class AccountSettingsActivity extends AppCompatActivity {
    private static final String TAG="AccountSettingsActivity";
    private static final int ACTIVITY_NUM = 4;

    private Context mContext;
    private RelativeLayout mRelativeLayout;
    public SectionsStatePagerAdapter pagerAdapter;
    private ViewPager2 viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        mContext=AccountSettingsActivity.this;
        Log.d(TAG,"onCreate: started");
        viewPager=(ViewPager2) findViewById(R.id.viewpager_container);
        mRelativeLayout=(RelativeLayout) findViewById(R.id.relLayout1);

        setupSettingsList();
//        setupBottomNavigationView();
        getIncomingIntent();

        ImageView backArrow=(ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: navigation back to ProfileActivity");
                finish();
            }
        });
    }

    private void getIncomingIntent(){
        Intent intent = getIntent();

        if(intent.hasExtra(getString(R.string.selected_image))
                || intent.hasExtra(getString(R.string.selected_bitmap))){

            //if there is an imageUrl attached as an extra, then it was chosen from the gallery/photo fragment
            Log.d(TAG, "getIncomingIntent: New incoming imgUrl");

            if(intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment))){

                if(intent.hasExtra(getString(R.string.selected_image))){
                    //set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0,
                            intent.getStringExtra(getString(R.string.selected_image)), null);
                }
                else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    //set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0,
                            null,(Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap)));
                }

            }

        }

        if(intent.hasExtra(getString(R.string.calling_activity))){
            SectionsStatePagerAdapter pagerAdapter=new SectionsStatePagerAdapter(this);
            pagerAdapter.addFragment(new EditProfileFragment(),getString(R.string.edit_your_profile));
            pagerAdapter.addFragment(new SignOutFragment(),getString(R.string.sign_out));
            Log.d(TAG, "getIncomingIntent: received incoming intent from "+pagerAdapter.getFragmentName(0)+" " + getString(R.string.profile_activity));
            Toast.makeText(mContext, pagerAdapter.getFragmentName(0), Toast.LENGTH_SHORT).show();
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(pagerAdapter.getFragmentNumber(getString(R.string.edit_your_profile)));
            Log.d(TAG,"Here's it come: ");
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }
    }

    public void setViewPager(int fragmentNumber){
        mRelativeLayout.setVisibility(View.GONE);
        SectionsStatePagerAdapter pagerAdapter=new SectionsStatePagerAdapter(this);
        pagerAdapter.addFragment(new EditProfileFragment(),getString(R.string.edit_profile_fragment));
        pagerAdapter.addFragment(new SignOutFragment(),getString(R.string.sign_out));
        Toast.makeText(AccountSettingsActivity.this, "It's okayyyy izzoudine "+ fragmentNumber+"edddddddd", Toast.LENGTH_SHORT).show();

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(fragmentNumber);
        Toast.makeText(AccountSettingsActivity.this, "It's okayyyy izzoudine "+ fragmentNumber, Toast.LENGTH_SHORT).show();
    }
    private void setupSettingsList(){
        Log.d(TAG, "setupSettingsList: initializing 'Account Settings' list.");
        ListView listView = (ListView) findViewById(R.id.lvAccountSettings);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment)); //fragment 0
        options.add(getString(R.string.sign_out_fragment)); //fragement 1

        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(TAG, "onItemClick: navigating to fragment#: " + position);
            setViewPager(position);
        });

    }

}
