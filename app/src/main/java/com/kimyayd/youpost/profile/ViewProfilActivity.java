package com.kimyayd.youpost.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.kimyayd.youpost.message.MessageActivity;
import com.kimyayd.youpost.models.User;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.models.UserSettings;
import com.kimyayd.youpost.utils.FirebaseMethods;
import com.kimyayd.youpost.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfilActivity extends AppCompatActivity {
    private static final String TAG = "ProfileFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;



    //widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription,
            mFollow, mUnfollow ;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private ImageView mBackArrow,message;
    private Context mContext;


    private ViewPager2 mViewPager;
    private ArrayList<Fragment> arr;

    //vars
    private User mUser;
    private int mFollowersCount = 0;
    private int mFollowingCount = 0;
    private int mPostsCount = 0;
    private int mVideosCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profil);
        mDisplayName =  findViewById(R.id.display_name);
        mUsername =  findViewById(R.id.username);
        message =  findViewById(R.id.user_message);
        mWebsite =  findViewById(R.id.website);
        mDescription =  findViewById(R.id.description);
        mProfilePhoto =  findViewById(R.id.profile_photo);
        mPosts =  findViewById(R.id.tvPosts);
        mFollowers =  findViewById(R.id.tvFollowers);
        mFollowing =  findViewById(R.id.tvFollowing);
        mProgressBar =  findViewById(R.id.profileProgressBar);
        mFollow =  findViewById(R.id.follow);
        mUnfollow =  findViewById(R.id.unfollow);
        mBackArrow =  findViewById(R.id.backArrow);
        mContext = ViewProfilActivity.this;
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(mContext));
        Log.d(TAG, "onCreateView: stared.");
        arr=new ArrayList<>();
        arr.add(new TextFragment());
        arr.add(new PhotopostFragment());
        arr.add(new VideoFragment());

        SectionPagerAdapter adapter=new SectionPagerAdapter(ViewProfilActivity.this,arr);
        mViewPager=findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout =  findViewById(R.id.tabs);
        new TabLayoutMediator(tabLayout, mViewPager,
                (tab, position) -> tab.getCustomView()
        ).attach();
        tabLayout.getTabAt(0).setIcon(R.drawable.text_post);
        tabLayout.getTabAt(1).setIcon(R.drawable.photo_post);
        tabLayout.getTabAt(2).setIcon(R.drawable.video_post);

        try {
            mUser = getUserFromBundle();
            init();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
            getSupportFragmentManager().popBackStack();
        }

   message.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent intent=new Intent(mContext, MessageActivity.class);
        intent.putExtra("userid",mUser.getUser_id());
        mContext.startActivity(intent);
    }
    });
        setupFirebaseAuth();

        isFollowing();
        getFollowingCount();
        getFollowersCount();
        getPostsCount();

        if (!mUser.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            mFollow.setOnClickListener(v -> {
                Log.d(TAG, "onClick: now following: " + mUser.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .setValue(mUser.getUser_id());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                setFollowing();
                recreate();
            });


            mUnfollow.setOnClickListener(v -> {
                Log.d(TAG, "onClick: now unfollowing: " + mUser.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();
                setUnfollowing();
                recreate();
            });
        } else {
            mFollow.setVisibility(View.GONE);
            mUnfollow.setVisibility(View.GONE);
        }
    }
    public User getUserFromBundle(){
        Log.d(TAG, "getUserFromBundle: arguments: " + getIntent().getParcelableExtra("intent_user"));
        Intent intent=getIntent();
        if(intent != null){
            return intent.getParcelableExtra(getString(R.string.intent_user));
        }else{
            return null;
        }
    }
    private void init(){

        //set the profile widgets
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
        Query query1 = reference1.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue(UserAccountSettings.class).toString());

                    UserSettings settings = new UserSettings();
                    settings.setUser(mUser);
                    settings.setSettings(singleSnapshot.getValue(UserAccountSettings.class));
                    setProfileWidgets(settings);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //get the users profile photos

//        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
//        Query query2 = reference2
//                .child(getString(R.string.dbname_user_photos))
//                .child(mUser.getUser_id());
//        query2.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                ArrayList<Photo> photos = new ArrayList<Photo>();
//                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
//
//                    Photo photo = new Photo();
//                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
//
//                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
//                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
//                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
//                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
//                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
//
//                    ArrayList<Comment> comments = new ArrayList<Comment>();
//                    for (DataSnapshot dSnapshot : singleSnapshot
//                            .child(getString(R.string.field_comments)).getChildren()){
//                        Comment comment = new Comment();
//                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
//                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
//                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
//                        comments.add(comment);
//                    }
//
//                    photo.setComments(comments);
//
//                    List<Like> likesList = new ArrayList<Like>();
//                    for (DataSnapshot dSnapshot : singleSnapshot
//                            .child(getString(R.string.field_likes)).getChildren()){
//                        Like like = new Like();
//                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//                        likesList.add(like);
//                    }
//                    photo.setLikes(likesList);
//                    photos.add(photo);
//                }
//                setupImageGrid(photos);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "onCancelled: query cancelled.");
//            }
//        });
    }

    private void isFollowing(){
        Log.d(TAG, "isFollowing: checking if following this users.");
        setUnfollowing();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue());

                    setFollowing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowersCount(){
        mFollowersCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(mUser.getUser_id());
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
                .child(mUser.getUser_id());
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
        mVideosCount = 0;


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_posts))
                .child(mUser.getUser_id()).child("post");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found post:" + singleSnapshot.getValue());
                    mPostsCount++;
                }
                Query querys = reference.child(getString(R.string.dbname_user_videos))
                        .child(mUser.getUser_id()).child("video");
                querys.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                            Log.d(TAG, "onDataChange: found post:" + singleSnapshot.getValue());
                            mVideosCount++;
                        }
                        mPosts.setText(String.valueOf(mPostsCount+mVideosCount));

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

    private void setFollowing(){
        Log.d(TAG, "setFollowing: updating UI for following this user");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);

    }

    private void setUnfollowing(){
        Log.d(TAG, "setFollowing: updating UI for unfollowing this user");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
    }

    private void setProfileWidgets(UserSettings userSettings){
        UserAccountSettings settings = userSettings.getSettings();
        if(settings.getProfile_photo().equals("default")){
            mProfilePhoto.setImageResource(R.drawable.ic_user);
        }else {
            Log.d(TAG,"Profile_photo: "+settings.getProfile_photo());
            UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        }

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mProgressBar.setVisibility(View.GONE);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getSupportFragmentManager().popBackStack();
                finish();
            }
        });

    }

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