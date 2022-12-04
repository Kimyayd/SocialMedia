package com.kimyayd.youpost.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.MainActivity;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.Comment;
import com.kimyayd.youpost.models.Heart;
import com.kimyayd.youpost.models.Like;
import com.kimyayd.youpost.models.Post;
import com.kimyayd.youpost.models.User;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.profile.ProfileActivity;
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

public class MainFeedListAdapter extends ArrayAdapter<Post> {

    public interface OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    private static final String TAG = "MainFeedListAdapter";

    private LayoutInflater mInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";

    public MainFeedListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Post> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();

    }

    static class ViewHolder{
        CircleImageView mprofileImage;
        String likesString;
        TextView username, timeDetla, caption, likes, comments;
        SquareImageView image;
        TextView text;
        ImageView heartRed, heartWhite, comment,share;
        ImageButton play;

        TextureVideoView videoView;
        UserAccountSettings settings = new UserAccountSettings();
        User user  = new User();
        StringBuilder users;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Post post;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getContext()));
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.text = (TextView) convertView.findViewById(R.id.post_text);
            holder.image = (SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.share = (ImageView) convertView.findViewById(R.id.share);
            holder.comment = (ImageView) convertView.findViewById(R.id.speech_bubble);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comments_link);
            holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.timeDetla = (TextView) convertView.findViewById(R.id.image_time_posted);
            holder.mprofileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.play = (ImageButton) convertView.findViewById(R.id.play);
            holder.videoView = (TextureVideoView) convertView.findViewById(R.id.post_video);



            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

            holder.post = getItem(position);
        Log.d(TAG,"Yeah: "+holder.post.toString());
        holder.detector = new GestureDetector(mContext, new GestureListener(holder));
        holder.users = new StringBuilder();
        holder.heart = new Heart(holder.heartWhite, holder.heartRed);

        getCurrentUsername();
        getLikesString(holder);
        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.post.getPost_type().equals("1")) {
                    Uri imageUri = Uri.parse(holder.post.getPost_path());
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, holder.post.getCaption());
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setType("image/*");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        mContext.startActivity(shareIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }else if(holder.post.getPost_type().equals("0")) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, holder.post.getCaption());
                    shareIntent.setType("text/plain");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        mContext.startActivity(shareIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
                else if(holder.post.getPost_type().equals("2")) {
                    Intent shareIntent = new Intent();
                    Uri videoUri = Uri.parse(holder.post.getPost_path());
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, holder.post.getCaption());
                    shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
                    shareIntent.setType("video/*");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        mContext.startActivity(shareIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
//        List<Comment> comments = getItem(position).getComments();
        //" + comments.size() + "
        holder.comments.setText("View all comments");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: loading comment thread for " + getItem(position).getPost_id());
                ((MainActivity)mContext).onCommentThreadSelected(getItem(position),
                        mContext.getString(R.string.home_activity));
            }
        });


        String timestampDifference = getTimestampDifference(getItem(position));
        if(!timestampDifference.equals("0")){
            holder.timeDetla.setText(timestampDifference + " DAYS AGO");
        }else{
            holder.timeDetla.setText("TODAY");
        }
        Log.d(TAG, "getView: "+getItem(position).getPost_type());

        if(holder.post.getPost_type().equals("1")) {
            Log.d(TAG,"Image is on: 1");
            holder.text.setVisibility(View.GONE);
            holder.caption.setText(getItem(position).getCaption());
            final ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(getItem(position).getPost_path(), holder.image);
        }
        else if(holder.post.getPost_type().equals("0")){
            Log.d(TAG,"Text is on: 0");
            holder.image.setVisibility(View.GONE);
            holder.caption.setVisibility(View.GONE);
            holder.text.setText(getItem(position).getCaption());

        }else if(holder.post.getPost_type().equals("2")){
            holder.text.setVisibility(View.GONE);
            holder.image.setVisibility(View.GONE);
            holder.caption.setText(getItem(position).getCaption());
            holder.play.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            holder.videoView.setVideoPath(getItem(position).getPost_path());
            holder.videoView.start();
            holder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.videoView.isPlaying()){
                        holder.play.setVisibility(View.VISIBLE);
                        holder.videoView.pause();
                    }else{
                        holder.videoView.start();
                        holder.play.setVisibility(View.GONE);
                    }
                }
            });
            holder.videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.videoView.isPlaying()){
                        holder.play.setVisibility(View.VISIBLE);
                        holder.videoView.pause();
                    }else{
                        holder.videoView.start();
                        holder.play.setVisibility(View.GONE);
                    }
                }
            });
        }
        //set the image
//        if(!getItem(position).getPost_type().equals(R.string.text)) {
//            final ImageLoader imageLoader = ImageLoader.getInstance();
//            imageLoader.displayImage(getItem(position).getPost_path(), holder.image);
//        }

        //get the profile image and username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    // currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    Log.d(TAG, "onDataChange: found user: "
                            + singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getContext()));
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.mprofileImage);
                    Log.d(TAG, "onDataChange: found photo: "+singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo());

                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " +
                                    holder.user.getUsername());
                           Intent intent = new Intent(mContext, ProfileActivity.class);
                           intent.putExtra(mContext.getString(R.string.calling_activity),
                           mContext.getString(R.string.home_activity));
                           intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                           mContext.startActivity(intent);
                        }
                    });

                    holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " +
                                    holder.user.getUsername());
                        }

//                            Intent intent = new Intent(mContext, ProfileActivity.class);
//                            intent.putExtra(mContext.getString(R.string.calling_activity),
//                                    mContext.getString(R.string.home_activity));
//                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
//                            mContext.startActivity(intent);
                        }
                    );



                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((MainActivity)mContext).onCommentThreadSelected(getItem(position),
                                    mContext.getString(R.string.home_activity));

                            //another thing?
//                            ((MainActivity)mContext).hideLayout();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //get the user object
        Query userQuery = mReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.getValue(User.class).getUsername());

                    holder.user = singleSnapshot.getValue(User.class);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(reachedEndOfList(position)){
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfList(int position){
        return position == getCount() - 1;
    }

    private void loadMoreData(){

        try{
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();
        }catch (ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }

        try{
            mOnLoadMoreItemsListener.onLoadMoreItems();
        }catch (NullPointerException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        ViewHolder mHolder;
        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            Log.d(TAG, "onDoubleTap: clicked on post: " + mHolder.post.getPost_id());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_user_posts))
                    .child(mHolder.post.getUser_id())
                    .child("likes")
                    .child(mHolder.post.getPost_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        String keyID = singleSnapshot.getKey();

                        //case1: Then user already liked the photo
                        if (mHolder.likeByCurrentUser
//                                && singleSnapshot.getValue(Like.class).getUser_id()
//                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        ) {

                            if (mHolder.post.getPost_type().equals("0")) {

                                mReference.child(mContext.getString(R.string.dbname_user_texts))
                                        .child(mHolder.post.getUser_id())
                                        .child("likes")
                                        .child(mHolder.post.getPost_id())
                                        .child(keyID)
                                        .removeValue();


                                mReference.child(mContext.getString(R.string.dbname_user_posts))
                                        .child(mHolder.post.getUser_id())
                                        .child("likes")
//                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(mHolder.post.getPost_id())
                                        .child(keyID)
                                        .removeValue();


                            } else if (mHolder.post.getPost_type().equals("1")) {

                                mReference.child(mContext.getString(R.string.dbname_user_photos))
                                        .child(mHolder.post.getUser_id())
                                        .child("likes")
                                        .child(mHolder.post.getPost_id())
                                        .child(keyID)
                                        .removeValue();


                                mReference.child(mContext.getString(R.string.dbname_user_posts))
                                        .child(mHolder.post.getUser_id())
                                        .child("likes")
//                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(mHolder.post.getPost_id())
                                        .child(keyID)
                                        .removeValue();

                            } else if (mHolder.post.getPost_type().equals("2")) {

                                mReference.child(mContext.getString(R.string.dbname_user_videos))
                                        .child(mHolder.post.getUser_id())
                                        .child("likes")
                                        .child(mHolder.post.getPost_id())
                                        .child(keyID)
                                        .removeValue();

                            }

///
                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }
                        //case2: The user has not liked the photo
                        else if (!mHolder.likeByCurrentUser) {
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if (!dataSnapshot.exists()) {
                        addNewLike(mHolder);
                    }

                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewLike(final ViewHolder holder){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = mReference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        if(holder.post.getPost_type().equals("0")){

            mReference.child(mContext.getString(R.string.dbname_user_texts))
                    .child(holder.post.getUser_id())
                    .child("likes")
                    .child(holder.post.getPost_id())
                    .child(newLikeID)
                    .setValue(hashMap);

        }else if(holder.post.getPost_type().equals("1")){

            mReference.child(mContext.getString(R.string.dbname_user_photos))
                    .child(holder.post.getUser_id())
                    .child("likes")
                    .child(holder.post.getPost_id())
                    .child(newLikeID)
                    .setValue(hashMap);

        }else if(holder.post.getPost_type().equals("2")){

            mReference.child(mContext.getString(R.string.dbname_user_videos))
                    .child(holder.post.getUser_id())
                    .child("likes")
                    .child(holder.post.getPost_id())
                    .child(newLikeID)
                    .setValue(hashMap);

        }
        mReference.child(mContext.getString(R.string.dbname_user_posts))
                .child(holder.post.getUser_id())
                .child(mContext.getString(R.string.field_likes))
                .child(holder.post.getPost_id())
                .child(newLikeID)
                .setValue(hashMap);

        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: retrieving user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
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

    private void getLikesString(final ViewHolder holder){
        Log.d(TAG, "getLikesString: getting likes string");

        Log.d(TAG, "getLikesString: post id: " + holder.post.getPost_id());
        try{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_posts))
                    .child(holder.post.getPost_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(mContext.getString(R.string.dbname_users))
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

                                if(holder.users.toString().contains(currentUsername + ",")){//mitch, mitchell.tabian
                                    holder.likeByCurrentUser = true;
                                }else{
                                    holder.likeByCurrentUser = false;
                                }

                                int length = splitUsers.length;
                                if(length == 1){
                                    holder.likesString = "Liked by " + splitUsers[0];
                                }
                                else if(length == 2){
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + " and " + splitUsers[1];
                                }
                                else if(length == 3){
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + " and " + splitUsers[2];

                                }
                                else if(length == 4){
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + splitUsers[3];
                                }
                                else if(length > 4){
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + (splitUsers.length - 3) + " others";
                                }
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
                        holder.likesString = "";
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

    private void setupLikesString(final ViewHolder holder, String likesString){
        Log.d(TAG, "setupLikesString: likes string:" + holder.likesString);

        Log.d(TAG, "setupLikesString: post id: " + holder.post.getPost_id());
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
        holder.likes.setText(likesString);
    }

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(Post post){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
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
