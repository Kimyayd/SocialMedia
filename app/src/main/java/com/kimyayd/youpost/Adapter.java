package com.kimyayd.youpost;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.kimyayd.youpost.models.Comment;
import com.kimyayd.youpost.models.Heart;
import com.kimyayd.youpost.models.Like;
import com.kimyayd.youpost.models.Post;
import com.kimyayd.youpost.models.User;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.profile.ProfileActivity;
import com.kimyayd.youpost.utils.MainFeedListAdapter;
import com.kimyayd.youpost.utils.SquareImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sprylab.android.widget.TextureVideoView;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter extends RecyclerView.Adapter<Adapter.MyHolder> {

    private static final String TAG = "Adapter";
    Context context;
    List<Post> postList;
    private LayoutInflater mInflater;
    private int mLayoutResource;
    private DatabaseReference mReference;
    private String currentUsername = "";

    public Adapter(@NonNull Context context, @NonNull List<Post> postList) {
        this.context = context;
        this.postList = postList;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout post_row.xml
        View view = LayoutInflater.from(context).inflate(R.layout.layout_mainfeed_listitem, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        holder.post = postList.get(position);
        Log.d(TAG,"Yeah: "+holder.post.toString());
        holder.detector = new GestureDetector(context, new Adapter.GestureListener(holder));
        holder.users = new StringBuilder();
        holder.heart = new Heart(holder.heartWhite, holder.heartRed);
        getCurrentUsername();
        getLikesString(holder);
        List<Comment> comments = postList.get(holder.getAdapterPosition()).getComments();
        holder.comments.setText("View all " + comments.size() + " comments");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: loading comment thread for " + postList.get(holder.getAdapterPosition()).getPost_id());
                ((MainActivity)context).onCommentThreadSelected(postList.get(holder.getAdapterPosition()),
                        context.getString(R.string.home_activity));
            }
        });


        String timestampDifference = getTimestampDifference(postList.get(holder.getAdapterPosition()));
        if(!timestampDifference.equals("0")){
            holder.timeDetla.setText(timestampDifference + " DAYS AGO");
        }else{
            holder.timeDetla.setText("TODAY");
        }
        Log.d(TAG, "getView: "+postList.get(holder.getAdapterPosition()).getPost_type());

        if(holder.post.getPost_type().equals("1")) {
            Log.d(TAG,"Image is on: 1");
            holder.caption.setText(postList.get(holder.getAdapterPosition()).getCaption());
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(holder.image.getContext()));
            final ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(postList.get(holder.getAdapterPosition()).getPost_path(), holder.image);
        }

        else if(holder.post.getPost_type().equals("2")) {
            Log.d(TAG,"Video is on: 1");
//            holder.image.setVisibility(View.GONE);
//            holder.text.setVisibility(View.GONE);
            holder.play.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            Uri uri=Uri.parse(postList.get(holder.getAdapterPosition()).getPost_path());
            holder.video.setVideoURI(uri);
            holder.video.start();
            holder.caption.setText(postList.get(holder.getAdapterPosition()).getCaption());
            holder.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: clique");
                    if (!holder.video.isPlaying()) {
                        Toast.makeText(context.getApplicationContext(), "Button clicked", Toast.LENGTH_SHORT).show();

                        holder.play.setVisibility(View.GONE);
                        holder.video.start();
                    } else {
                        holder.video.pause();
                        holder.play.setVisibility(View.VISIBLE);

                    }

                }
            });
            holder.video.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    Log.d(TAG, "onClick: clique");
                    Toast.makeText(context.getApplicationContext(), "Videoview clicked", Toast.LENGTH_SHORT).show();


                    if (! holder.video.isPlaying()) {
                        Toast.makeText(context.getApplicationContext(), "Button clicked", Toast.LENGTH_SHORT).show();

                        holder.play.setVisibility(View.GONE);
                        holder.video.start();
                    } else {
                        holder.video.pause();
                        holder.play.setVisibility(View.VISIBLE);

                    }

                    return false;

                }
            });


        }
        else if(holder.post.getPost_type().equals("0")){
            Log.d(TAG,"Text is on: 0");
//            holder.image.setVisibility(View.GONE);
//            holder.video.setVisibility(View.GONE);
//            holder.play.setVisibility(View.GONE);
//            holder.caption.setVisibility(View.GONE);
            holder.text.setText(postList.get(holder.getAdapterPosition()).getCaption());

        }
        //set the image
//        if(!getItem(position).getPost_type().equals(R.string.text)) {
//            final ImageLoader imageLoader = ImageLoader.getInstance();
//            imageLoader.displayImage(getItem(position).getPost_path(), holder.image);
//        }

        //get the profile image and username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(context.getString(R.string.dbname_user_account_settings))
                .orderByChild(context.getString(R.string.field_user_id))
                .equalTo(postList.get(holder.getAdapterPosition()).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    // currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    Log.d(TAG, "onDataChange: found user: "
                            + singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(holder.image.getContext()));
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.mprofileImage);
                    Log.d(TAG, "onDataChange: found photo: "+singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo());

                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of: " +
                                    holder.user.getUsername());
                            Intent intent = new Intent(context, ProfileActivity.class);
                            intent.putExtra(context.getString(R.string.calling_activity),
                                    context.getString(R.string.home_activity));
                            intent.putExtra(context.getString(R.string.intent_user), holder.user);
                            context.startActivity(intent);
                        }
                    });

                    holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Log.d(TAG, "onClick: navigating to profile of: " +
                                                                            holder.user.getUsername());
                                                                    Intent intent = new Intent(context, ProfileActivity.class);
                                                                    intent.putExtra(context.getString(R.string.calling_activity),
                                                                            context.getString(R.string.home_activity));
                                                                    intent.putExtra(context.getString(R.string.intent_user), holder.user);
                                                                    context.startActivity(intent);
                                                                }


                                                            }
                    );



                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((MainActivity)context).onCommentThreadSelected(postList.get(holder.getAdapterPosition()),
                                    context.getString(R.string.home_activity));

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
                .child(context.getString(R.string.dbname_users))
                .orderByChild(context.getString(R.string.field_user_id))
                .equalTo(postList.get(holder.getAdapterPosition()).getUser_id());
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
     }
    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        Adapter.MyHolder mHolder;
        public GestureListener(Adapter.MyHolder holder) {
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
                    .child(context.getString(R.string.dbname_posts))
                    .child(mHolder.post.getPost_id())
                    .child(context.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        String keyID = singleSnapshot.getKey();

                        //case1: Then user already liked the photo
                        if(mHolder.likeByCurrentUser
//                                && singleSnapshot.getValue(Like.class).getUser_id()
//                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        ){

                            mReference.child(context.getString(R.string.dbname_posts))
                                    .child(mHolder.post.getPost_id())
                                    .child(context.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
///
                            mReference.child(context.getString(R.string.dbname_user_posts))
//                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.post.getUser_id())
                                    .child(mHolder.post.getPost_id())
                                    .child(context.getString(R.string.field_likes))
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
    }

    private void addNewLike(final Adapter.MyHolder holder){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mReference.child(context.getString(R.string.dbname_posts))
                .child(holder.post.getPost_id())
                .child(context.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mReference.child(context.getString(R.string.dbname_user_posts))
                .child(holder.post.getUser_id())
                .child(holder.post.getPost_id())
                .child(context.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: retrieving user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(context.getString(R.string.dbname_users))
                .orderByChild(context.getString(R.string.field_user_id))
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

    private void getLikesString(final Adapter.MyHolder holder){
        Log.d(TAG, "getLikesString: getting likes string");

        Log.d(TAG, "getLikesString: post id: " + holder.post.getPost_id());
        try{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(context.getString(R.string.dbname_posts))
                    .child(holder.post.getPost_id())
                    .child(context.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(context.getString(R.string.dbname_users))
                                .orderByChild(context.getString(R.string.field_user_id))
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
                                            + "and " + splitUsers[1];
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

    private void setupLikesString(final Adapter.MyHolder holder, String likesString){
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

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
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


    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder
    class MyHolder extends RecyclerView.ViewHolder {

        //views from post_row.xml
        CircleImageView mprofileImage;
        String likesString;
        TextView username, timeDetla, caption, likes, comments;
        SquareImageView image;
        TextView text;
        TextureVideoView video;
        ImageView heartRed, heartWhite, comment;
        ImageButton play;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Post post;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            text = (TextView) itemView.findViewById(R.id.post_text);
            video = (TextureVideoView) itemView.findViewById(R.id.post_video);
            image = (SquareImageView) itemView.findViewById(R.id.post_image);
            heartRed = (ImageView) itemView.findViewById(R.id.image_heart_red);
            heartWhite = (ImageView) itemView.findViewById(R.id.image_heart);
            comment = (ImageView) itemView.findViewById(R.id.speech_bubble);
            likes = (TextView) itemView.findViewById(R.id.image_likes);
            comments = (TextView) itemView.findViewById(R.id.image_comments_link);
            caption = (TextView) itemView.findViewById(R.id.image_caption);
            timeDetla = (TextView) itemView.findViewById(R.id.image_time_posted);
            mprofileImage = (CircleImageView) itemView.findViewById(R.id.profile_photo);
            play = (ImageButton) itemView.findViewById(R.id.play);

        }

    }

}

