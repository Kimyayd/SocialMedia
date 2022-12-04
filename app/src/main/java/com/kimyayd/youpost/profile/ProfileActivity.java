package com.kimyayd.youpost.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.MainActivity;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.home.HomeFragment;
import com.kimyayd.youpost.home.SectionPagerAdapter;
import com.kimyayd.youpost.models.Photo;
import com.kimyayd.youpost.models.User;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.models.UserSettings;
import com.kimyayd.youpost.utils.FirebaseMethods;
import com.kimyayd.youpost.utils.UniversalImageLoader;
import com.kimyayd.youpost.utils.ViewProfilFragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
// implements
//
//    ViewProfilFragment.OnGridImageSelectedListener
//    ViewPostFragment.OnCommentThreadSelectedListener,
//    ProfileFragment.OnGridImageSelectedListener,
//    TextAdapter.OnLoadMoreItemsListener,
//    VideoAdapter.OnLoadMoreItemsListener,
//    TextFragment.OnCommentThreadSelectedListener,
//            VideoFragment.OnCommentThreadSelectedListener

    {
        private ArrayList<Fragment> arr;
        private static final String TAG = "ProfileActivity";
        private Context mContext = ProfileActivity.this;

        private RelativeLayout mRelativeLayout;

//        @Override
//        public void onLoadMoreItems () {
//        Log.d(TAG, "onLoadMoreItems: displaying more photos");
//        TextFragment fragment = (TextFragment) getSupportFragmentManager()
//                .findFragmentByTag("android:switcher:" + R.id.viewpager_container);
//        if (fragment != null) {
//            fragment.displayMoreTexts();
//        }
//    }
//        public void onLoadMoreVideos () {
//        Log.d(TAG, "onLoadMoreItems: displaying more photos");
//        VideoFragment fragment = (VideoFragment) getSupportFragmentManager()
//                .findFragmentByTag("android:switcher:" + R.id.viewpager_container);
//        if (fragment != null) {
//            fragment.displayMoreVideos();
//        }
//    }
//
//
//        @Override
//        public void onCommentThreadSelectedListener (Photo photo){
//        Log.d(TAG, "onCommentThreadSelectedListener:  selected a comment thread");
//
//        ViewCommentsFragment fragment = new ViewCommentsFragment();
//        Bundle args = new Bundle();
//        args.putParcelable(getString(R.string.photo), photo);
//        fragment.setArguments(args);
//
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.containers, fragment);
//        transaction.addToBackStack(getString(R.string.view_comments_fragment));
//        transaction.commit();
//    }
//        @Override
//        public void onCommentThreadSelectedListener (Video video){
//        Log.d(TAG, "onCommentThreadSelectedListener:  selected a comment thread");
//
//        ViewVideoCommentsFragment fragment = new ViewVideoCommentsFragment();
//        Bundle args = new Bundle();
//        args.putParcelable(getString(R.string.video), video);
//        fragment.setArguments(args);
//
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.containers, fragment);
//        transaction.addToBackStack(getString(R.string.view_comments_fragment));
//        transaction.commit();
//    }
//        @Override
//        public void onGridImageSelected (Photo photo,int activityNumber){
//        Log.d(TAG, "onGridImageSelected: selected an image gridview: " + photo.toString());
//
//        ViewPostFragment fragment = new ViewPostFragment();
//        Bundle args = new Bundle();
//        args.putParcelable(getString(R.string.photo), photo);
//        args.putInt(getString(R.string.activity_number), activityNumber);
//
//        fragment.setArguments(args);
//
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.containers, fragment);
//        transaction.addToBackStack(getString(R.string.view_post_fragment));
//        transaction.commit();
//    }
//
        @Override
        protected void onCreate (Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile);
            init();
        }
//
//    }
//        public void showLayout () {
//        Log.d(TAG, "hideLayout: showing layout");
//        mRelativeLayout.setVisibility(View.VISIBLE);
//        mFrameLayout.setVisibility(View.GONE);
//    }

        private void init(){
            Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));
                        Log.d(TAG, "init: inflating Profile");
                        ProFragment fragment = new ProFragment();
                        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.containers, fragment);
                        transaction.addToBackStack(getString(R.string.post_text_fragment));
                        transaction.commit();

        }
//
//        @Override
//        public void onCommentThreadSelectedListener (Text text){
//        Log.d(TAG, "onCommentThreadSelected: selected a comment thread");
//
//        ViewPostTextCommentsFragment fragment = new ViewPostTextCommentsFragment();
//        Bundle args = new Bundle();
//        args.putParcelable(getString(R.string.text), text);
//        args.putString(getString(R.string.profile_activity), getString(R.string.profile_activity));
//        fragment.setArguments(args);
//
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.containers, fragment);
//        transaction.addToBackStack(getString(R.string.view_comments_fragment));
//        transaction.commit();
//
//    }
//        public void onCommentThreadSelected (Text text, String callingActivity){
//        Log.d(TAG, "onCommentThreadSelected: selected a coemment thread");
//
//        ViewPostTextCommentsFragment fragment = new ViewPostTextCommentsFragment();
//        Bundle args = new Bundle();
//        args.putParcelable(getString(R.string.text), text);
//        args.putString(getString(R.string.profile_activity), getString(R.string.profile_activity));
//        fragment.setArguments(args);
//
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.containers, fragment);
//        transaction.addToBackStack(getString(R.string.view_comments_fragment));
//        transaction.commit();
//
//    }
//        public void onCommentThreadSelected (Video video, String callingActivity){
//        Log.d(TAG, "onCommentThreadSelected: selected a comment thread");
//
//        ViewVideoCommentsFragment fragment = new ViewVideoCommentsFragment();
//        Bundle args = new Bundle();
//        args.putParcelable(getString(R.string.video), video);
//        args.putString(getString(R.string.profile_activity), getString(R.string.profile_activity));
//        fragment.setArguments(args);
//
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.containers, fragment);
//        transaction.addToBackStack(getString(R.string.view_comments_fragment));
//        transaction.commit();
//
//    }

        public void hideLayout () {
        Log.d(TAG, "hideLayout: hiding layout");
    }



    }



