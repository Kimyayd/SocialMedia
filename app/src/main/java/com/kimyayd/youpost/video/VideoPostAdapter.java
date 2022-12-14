package com.kimyayd.youpost.video;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.Heart;
import com.kimyayd.youpost.models.Like;
import com.kimyayd.youpost.models.User;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.models.Video;
import com.kimyayd.youpost.profile.ViewProfilActivity;
import com.kimyayd.youpost.utils.Comment_activity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sprylab.android.widget.TextureVideoView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;


public class VideoPostAdapter extends RecyclerView.Adapter<VideoPostAdapter.MyHolder>{

    private static final String TAG = "Adapter";
    Context mContext;
    List<Video> postList;
    private DatabaseReference mReference;
    private String currentUsername = "";
    private Boolean val=false;
    private Activity activity;
    public VideoPostAdapter(@NonNull Context context, @NonNull Activity activity, @NonNull List<Video> postList) {
        this.mContext = context;
        this.postList = postList;
        this.activity=activity;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_container_video, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        setVideoData(postList.get(position),holder);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


    static class MyHolder extends RecyclerView.ViewHolder{
        TextureVideoView videoView;
        TextView textVideoTitle, textVideoDate,textVideoUsername;
        TextView like_number,comment_number;
        ImageView image_plus,comment_image;
        CircleImageView mprofileImage;
        ProgressBar videoProgressBar;
        ImageView heartRed, heartWhite;
        String likesString;
        User user  = new User();
        StringBuilder users;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Video video;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            videoView=itemView.findViewById(R.id.videoView);
            textVideoTitle=itemView.findViewById(R.id.textVideoTitle);
            textVideoDate=itemView.findViewById(R.id.textVideoDate);
            textVideoUsername=itemView.findViewById(R.id.textVideoUsername);
            like_number=itemView.findViewById(R.id.like_number);
            comment_number=itemView.findViewById(R.id.comment_number);
            image_plus=itemView.findViewById(R.id.image_plus);
            videoProgressBar=itemView.findViewById(R.id.videoProgressBar);
            mprofileImage=itemView.findViewById(R.id.profile_image);
            comment_image =itemView.findViewById(R.id.comment_image);
            heartRed = itemView.findViewById(R.id.image_heart_red);
            heartWhite = itemView.findViewById(R.id.image_heart);
        }


    }

    void setVideoData(Video videoItem, MyHolder holder){
        holder.video=videoItem;
        if (videoItem.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.image_plus.setVisibility(View.GONE);
        } else {

            Query query = FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.dbname_following)).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                        Log.d(TAG,"User is :"+snapshot.toString());
                        UserAccountSettings user=snapshot.getValue(UserAccountSettings.class);
                        if(user.getUser_id().equals(videoItem.getUser_id())) {
                            val = true;
                        break;
                        }
                    }
                    if(val) {
                        holder.image_plus.setVisibility(View.GONE);
                    }else{
                        holder.image_plus.setVisibility(View.VISIBLE);
                        holder.image_plus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child(mContext.getString(R.string.dbname_following))
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(videoItem.getUser_id())
                                        .child(mContext.getString(R.string.field_user_id))
                                        .setValue(videoItem.getUser_id());

                                FirebaseDatabase.getInstance().getReference()
                                        .child(mContext.getString(R.string.dbname_followers))
                                        .child(videoItem.getUser_id())
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(mContext.getString(R.string.field_user_id))
                                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                holder.image_plus.setVisibility(View.GONE);

                            }
                        });
                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });


        }

        holder.detector = new GestureDetector(mContext, new GestureListener(holder));
        holder.users = new StringBuilder();
        holder.heart = new Heart(holder.heartWhite, holder.heartRed);
//            setupLikesString(holder);
        getCurrentUsername();
        Log.d(TAG,"Entering getLikesString");
        getLikesString(holder);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child("user_account_settings")
                .orderByChild("user_id")
                .equalTo(videoItem.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG,"IT's Okay..." +singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    String timestampDifference = getTimestampDifference(videoItem);
                    if(!timestampDifference.equals("0")) {
                        if (timestampDifference.equals("1")) {
                            holder.textVideoDate.setText("YESTERDAY");
                        } else{
                            holder.textVideoDate.setText(timestampDifference + " DAYS AGO");
                        }
                    }
                    else{
                        holder.textVideoDate.setText("TODAY");
                    }
                    holder.textVideoUsername.setText("@"+singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.textVideoTitle.setText(videoItem.getCaption());
                    ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(holder.mprofileImage.getContext()));
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    if(!(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo().equals("default"))){

                        imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                                holder.mprofileImage);
                    }else{
                        holder.mprofileImage.setImageResource(R.drawable.person);
                    }


                    final int[] l = {0};

                    DatabaseReference refs=FirebaseDatabase.getInstance().getReference("user_videos").child(videoItem.getUser_id()).child("comments").child(videoItem.getVideo_id());
                    refs.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot1 :snapshot.getChildren()){
                                if(dataSnapshot1.exists()){
                                    l[0]++;
                                }
                            }
                            holder.comment_number.setText(""+l[0]);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    holder.textVideoUsername.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(mContext, ViewProfilActivity.class);
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });
                    holder.comment_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(mContext, Comment_activity.class);
                            intent.putExtra("id",holder.video.getVideo_id());
                            intent.putExtra("user_id",holder.video.getUser_id());
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.video_activity));
                            mContext.startActivity(intent);
                        }
                    });

                    holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Intent intent = new Intent(mContext, ViewProfilActivity.class);
                                                                    intent.putExtra(mContext.getString(R.string.calling_activity),
                                                                            mContext.getString(R.string.video_activity));
                                                                    intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                                                                    mContext.startActivity(intent);
                                                                }


                                                            }
                    );
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        Query userQuery = mReference
                .child("users")
                .orderByChild("user_id")
                .equalTo(videoItem.getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    holder.user = singleSnapshot.getValue(User.class);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.textVideoTitle.setText(videoItem.getCaption());
        holder.videoView.setVideoURI(Uri.parse(videoItem.getVideo_path()));
        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                holder.videoProgressBar.setVisibility(View.GONE);
                mediaPlayer.start();

                    float videoRatio = mediaPlayer.getVideoWidth() /(float) mediaPlayer.getVideoHeight();
                    float screenRatio = holder.videoView.getWidth() /(float) holder.videoView.getHeight();

                    float scale = videoRatio/screenRatio;

                    if(scale >= 1f){
                        holder.videoView.setScaleX(scale);
                    }else {
                       holder.videoView.setScaleY(1f / scale);
                    }
            }


        });
        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
        holder.videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(holder.videoView.isPlaying()){
                    holder.videoView.pause();
                    Toast.makeText(mContext, "Video paused!", Toast.LENGTH_SHORT).show();

                }else{
                    holder.videoView.start();
                    Toast.makeText(mContext, "Video played!", Toast.LENGTH_SHORT).show();

                }
                return false;
            }
        });

    }
//    private void shareVideo(String title,String path){
//        Uri uri=saveVideo(path);
//        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//        sharingIntent.setType("video/mp4");
//        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Hey this is the video subject");
//        sharingIntent.putExtra(Intent.EXTRA_TEXT, title);
//        sharingIntent.putExtra(Intent.EXTRA_STREAM,uri);
//        mContext.startActivity(Intent.createChooser(sharingIntent,"Share Video"));
//    }
//    private Uri saveVideo(String path){
//        ContentValues content = new ContentValues(4);
//        content.put(MediaStore.Video.VideoColumns.DATE_ADDED,
//                System.currentTimeMillis() / 1000);
//        content.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
//        content.put(MediaStore.Video.Media.DATA, path);
//
//        ContentResolver resolver = mContext.getContentResolver();
//        Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, content);
//        return uri;
//    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        MyHolder mHolder;
        public GestureListener(MyHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Context mContext=mHolder.videoView.getContext();
            Log.d(TAG, "onDoubleTap: double tap detected.");

            Log.d(TAG, "onDoubleTap: clicked on post: " + mHolder.video.getVideo_id());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.dbname_user_videos));
            Query query = reference
                    .child(mHolder.video.getUser_id())
                    .child("likes")
                    .child(mHolder.video.getVideo_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        String keyID = singleSnapshot.getKey();

                        if(mHolder.likeByCurrentUser){
                            DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
                            mReference.child(mContext.getString(R.string.dbname_user_videos))
                                    .child(mHolder.video.getUser_id())
                                    .child("likes")
                                    .child(mHolder.video.getVideo_id())
                                    .child(keyID)
                                    .removeValue();

                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }
                        //case2: The user has not liked the photo
                        else if(!mHolder.likeByCurrentUser){
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }


    }

    private void addNewLike(final MyHolder holder){
        Log.d(TAG, "addNewLike: adding new like");
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.dbname_user_videos));
        String newLikeID = mReference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());

        mReference.child(holder.video.getUser_id())
                .child("likes")
                .child(holder.video.getVideo_id())
                .child(newLikeID)
                .setValue(hashMap);

        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void setupLikesString(final MyHolder holder, String likesString){
        Log.d(TAG, "setupLikesString: likes string:" + holder.likesString);
        Log.d(TAG, "setupLikesString: post id: " + holder.video.getVideo_id());
        if(holder.likeByCurrentUser){
            Log.d(TAG, "setupLikesString: post is liked by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }else{
            Log.d(TAG, "setupLikesString: photo is not liked by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.like_number.setText(likesString);
    }

    private void getLikesString(final MyHolder holder){

        Log.d(TAG, "getLikesString: getting likes string");

        Log.d(TAG, "getLikesString: post id: " + holder.video.getVideo_id());
        try{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.dbname_user_videos));
            Log.d(TAG,"Database connection");
            Query query = reference
                    .child(holder.video.getUser_id())
                    .child("likes")
                    .child(holder.video.getVideo_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG,"Not Database connection problem");
                    //                    holder.likesString="0";
                    holder.users = new StringBuilder();
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.dbname_users));
                        Query query = reference
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                    Log.d(TAG, "onDataChange: found like: " +
                                            singleSnapshot.getValue(User.class).getUsername());

                                    holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                    holder.users.append(",");
                                }

                                String[] splitUsers = holder.users.toString().split(",");

                                if(holder.users.toString().contains(currentUsername + ",")){
                                    holder.likeByCurrentUser = true;
                                }else{
                                    holder.likeByCurrentUser = false;
                                }

                                int length = splitUsers.length;

                                holder.likesString=""+length;

//                                if(length ==0){
//                                    holder.likesString ="0";
//                                }
//                                if(length == 1){
//                                    holder.likesString ="1";
//                                }
//                                else if(length == 2){
//                                    holder.likesString ="2";
//                                }
//                                else if(length == 3){
//                                    holder.likesString = "Liked by " + splitUsers[0]
//                                            + ", " + splitUsers[1]
//                                            + " and " + splitUsers[2];
//
//                                }
//                                else if(length == 4){
//                                    holder.likesString = "Liked by " + splitUsers[0]
//                                            + ", " + splitUsers[1]
//                                            + ", " + splitUsers[2]
//                                            + " and " + splitUsers[3];
//                                }
//                                else if(length > 4){
//                                    holder.likesString = "Liked by " + splitUsers[0]
//                                            + ", " + splitUsers[1]
//                                            + ", " + splitUsers[2]
//                                            + " and " + (splitUsers.length - 3) + " others";
//                                }
                                Log.d(TAG, "onDataChange: likes string: " + holder.likesString);
                                //setup likes string
                                setupLikesString(holder, holder.likesString);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    if(!dataSnapshot.exists()){
                        holder.likesString = "0";
                        holder.likeByCurrentUser = false;
                        //setup likes string
                        setupLikesString(holder, holder.likesString);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "getLikesString: NullPointerException: " + e.getMessage() );
            holder.likesString = "";
            holder.likeByCurrentUser = false;
            //setup likes string
            setupLikesString(holder, holder.likesString);
        }
    }

    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: retrieving user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.dbname_users));
        Query query = reference
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getTimestampDifference(Video post){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRENCH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = post.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }


}
