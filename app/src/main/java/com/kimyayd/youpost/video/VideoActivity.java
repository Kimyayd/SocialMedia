package com.kimyayd.youpost.video;

import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.models.Video;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoActivity extends AppCompatActivity {
    private ArrayList<Video> mPaginatedVideos;
    private ArrayList<String> mFollowing;
    private ArrayList<UserAccountSettings> mUserAccountSettings;
    private List<Video> videos;
    private static final String TAG = "VideoActivity";
    private int resultsCount = 0;
    ViewPager2 recyclerView;
    VideoPostAdapter postsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        recyclerView =findViewById(R.id.videosViewPager);
        recyclerView.setHorizontalFadingEdgeEnabled(true);

        getFollowing();
        loadVideos();
    }
    private void getFollowing() {
        Log.d(TAG, "getFollowing: searching for following");

        clearAll();
        //also add your own id to the list
        mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Query query = FirebaseDatabase.getInstance().getReference(getString(R.string.dbname_users));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "getFollowing: found user: " + singleSnapshot
                            .child(getString(R.string.field_user_id)).getValue());

                    mFollowing.add(singleSnapshot
                            .child(getString(R.string.field_user_id)).getValue().toString());
                }

                loadVideos();
//                getMyUserAccountSettings();
                getFriendsAccountSettings();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }


    private void clearAll(){
        if(mFollowing != null){
            mFollowing.clear();
        }
        if(videos != null){
            videos.clear();
            if(postsAdapter != null){
                postsAdapter.notifyDataSetChanged();
            }
        }
        if(mUserAccountSettings != null){
            mUserAccountSettings.clear();
        }
        if(mPaginatedVideos != null){
            mPaginatedVideos.clear();
        }
        mFollowing = new ArrayList<>();
        videos = new ArrayList<>();
        mPaginatedVideos = new ArrayList<>();
        mUserAccountSettings = new ArrayList<>();
    }

    private void loadVideos() {
        //path of all posts

        for (int i = 0; i < mFollowing.size(); i++) {
            final int count = i;
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_user_videos))
                    .child(mFollowing.get(i))
                    .child("video")
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Video post = new Video();
                        Map<String, Object> objectMap = (HashMap<String, Object>) ds.getValue();

                        post.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        post.setVideo_id(objectMap.get(getString(R.string.field_video_id)).toString());
                        post.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        post.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        post.setVideo_path(objectMap.get(getString(R.string.field_video_path)).toString());
                        Log.d(TAG, "getPhotos: photo: " + post.getVideo_id());
                        videos.add(post);
                    }


                        if (count >=mFollowing.size()-1) {

                            displayVideos();
                        }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //in case of error
                    Toast.makeText(VideoActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void displayVideos(){

        if(videos != null){
            try {
                //sort for newest to oldest
                Collections.sort(videos, new Comparator<Video>() {
                    public int compare(Video o1, Video o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });
                Log.d(TAG,"Size is null");

                int iterations = videos.size();
                if (iterations > 10) {
                    iterations = 10;
                }
//
                resultsCount = 0;
                for (int i = 0; i < iterations; i++) {
                    mPaginatedVideos.add(videos.get(i));
                    resultsCount++;
                    Log.d(TAG, "displayPhotos: adding a photo to paginated list: " + videos.get(i).getVideo_id());
                }

                postsAdapter = new VideoPostAdapter(this,VideoActivity.this,videos);
                recyclerView.setAdapter(postsAdapter);
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException:" + e.getMessage() );
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException:" + e.getMessage() );
            }
        }
    }

    private void getFriendsAccountSettings(){
        Log.d(TAG, "getFriendsAccountSettings: getting friends account settings.");

        for(int i = 0; i < mFollowing.size(); i++) {
            Log.d(TAG, "getFriendsAccountSettings: user: " + mFollowing.get(i));
            final int count = i;
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_user_account_settings))
                    .orderByKey()
                    .equalTo(mFollowing.get(i));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "getFriendsAccountSettings: got a user: " + snapshot.getValue(UserAccountSettings.class).getDisplay_name());
                        mUserAccountSettings.add(snapshot.getValue(UserAccountSettings.class));

                        if(count == 0){
                            JSONObject userObject = new JSONObject();
                            try {
                                userObject.put(getString(R.string.field_display_name), mUserAccountSettings.get(count).getDisplay_name());
                                userObject.put(getString(R.string.field_username), mUserAccountSettings.get(count).getUsername());
                                userObject.put(getString(R.string.field_profile_photo), mUserAccountSettings.get(count).getProfile_photo());
                                userObject.put(getString(R.string.field_user_id), mUserAccountSettings.get(count).getUser_id());
                                JSONObject userSettingsStoryObject = new JSONObject();
                                userSettingsStoryObject.put(getString(R.string.user_account_settings), userObject);
                            } catch ( JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
}
