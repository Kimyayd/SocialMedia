package com.kimyayd.youpost.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.home.SectionPagerAdapter;
import com.kimyayd.youpost.login.LoginActivity;
import com.kimyayd.youpost.models.Comment;
import com.kimyayd.youpost.models.Like;
import com.kimyayd.youpost.models.Photo;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.models.UserSettings;
import com.kimyayd.youpost.utils.FirebaseMethods;
import com.kimyayd.youpost.utils.GridImageAdapter;
import com.kimyayd.youpost.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment{
    private static final String TAG = "ProfileFragment";


    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener mOnGridImageSelectedListener;

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;


    //widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationView bottomNavigationView;
    private Context mContext;


    private ViewPager2 mViewPager;
    private FrameLayout mFrameLayout;
    private ArrayList<Fragment> arr;

    //vars
    private int mFollowersCount = 0;
    private int mFollowingCount = 0;
    private int mPostsCount = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUsername = (TextView) view.findViewById(R.id.username);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mPosts = (TextView) view.findViewById(R.id.tvPosts);
        mFollowers = (TextView) view.findViewById(R.id.tvFollowers);
        mFollowing = (TextView) view.findViewById(R.id.tvFollowing);
        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        gridView = (GridView) view.findViewById(R.id.gridView);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(getActivity());
        Log.d(TAG, "onCreateView: stared.");
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));

            arr=new ArrayList<>();
            arr.add(new TextsFragment());
            arr.add(new PhotopostsFragment());
            arr.add(new VideosFragment());

            SectionPagerAdapter adapter=new SectionPagerAdapter(getActivity(),arr);
            mViewPager=view.findViewById(R.id.viewpager_container);
            mViewPager.setAdapter(adapter);
            mFrameLayout = (FrameLayout) view.findViewById(R.id.containers);
            TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);

            new TabLayoutMediator(tabLayout, mViewPager,
                    (tab, position) -> tab.getCustomView()
            ).attach();

            tabLayout.getTabAt(0).setIcon(R.drawable.text_post);
            tabLayout.getTabAt(1).setIcon(R.drawable.photo_post);
            tabLayout.getTabAt(2).setIcon(R.drawable.video_post);

            setupToolbar();


        setupFirebaseAuth();
//        setupGridView();

        getFollowersCount();
        getFollowingCount();
        getPostsCount();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
        super.onAttach(context);
    }

//    private void setupGridView(){
//        Log.d(TAG, "setupGridView: Setting up image grid.");
//
//        final ArrayList<Photo> photos = new ArrayList<>();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        Query query = reference
//                .child(getString(R.string.dbname_user_photos))
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
//
//                    Photo photo = new Photo();
//                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
//
//                    try {
//                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
//                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
//                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
//                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
//                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
//                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
//
//                        ArrayList<Comment> comments = new ArrayList<Comment>();
//                        for (DataSnapshot dSnapshot : singleSnapshot
//                                .child(getString(R.string.field_comments)).getChildren()) {
//                            Comment comment = new Comment();
//                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
//                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
//                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
//                            comments.add(comment);
//                        }
//
//                        photo.setComments(comments);
//
//                        List<Like> likesList = new ArrayList<Like>();
//                        for (DataSnapshot dSnapshot : singleSnapshot
//                                .child(getString(R.string.field_likes)).getChildren()) {
//                            Like like = new Like();
//                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//                            likesList.add(like);
//                        }
//                        photo.setLikes(likesList);
//                        photos.add(photo);
//                    }catch(NullPointerException e){
//                        Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage() );
//                    }
//                }
//
//                //setup our image grid
//                int gridWidth = getResources().getDisplayMetrics().widthPixels;
//                int imageWidth = gridWidth/NUM_GRID_COLUMNS;
//                gridView.setColumnWidth(imageWidth);
//
//                ArrayList<String> imgUrls = new ArrayList<String>();
//                for(int i = 0; i < photos.size(); i++){
//                    imgUrls.add(photos.get(i).getImage_path());
//                }
//                GridImageAdapter adapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,
//                        "", imgUrls);
//                gridView.setAdapter(adapter);
//
//                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        mOnGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "onCancelled: query cancelled.");
//            }
//        });
//    }

    private void getFollowersCount(){
        mFollowersCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found follower:" + singleSnapshot.getValue());
                    mFollowersCount++;
                }
                mFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount(){
        mFollowingCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found following user:" + singleSnapshot.getValue());
                    mFollowingCount++;
                }
                mFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPostsCount(){
        mPostsCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_posts))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found post:" + singleSnapshot.getValue());
                    mPostsCount++;
                }
                mPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {

        UserAccountSettings settings = userSettings.getSettings();
        ImageLoader imageLoader = ImageLoader.getInstance();
        if(settings.getProfile_photo().equals("default")){
            mProfilePhoto.setImageResource(R.drawable.ic_user);
        }else {
            UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        }

//        Glide.with(getActivity())
//                .load(settings.getProfile_photo())
//                .into(mProfilePhoto);

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mProgressBar.setVisibility(View.GONE);
    }


    /**
     * Responsible for setting up the profile toolbar
     */
    private void setupToolbar(){

//        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account settings.");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     * BottomNavigationView setup
     */

      /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}