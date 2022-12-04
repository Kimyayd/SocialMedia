package com.kimyayd.youpost;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.models.Heart;
import com.kimyayd.youpost.models.User;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.models.Video;
import com.kimyayd.youpost.profile.ProfileActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sprylab.android.widget.TextureVideoView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SpecialAdapter extends RecyclerView.Adapter<SpecialAdapter.VideoViewHolder>{
    private List<Video> videos;


    public SpecialAdapter(List<Video> videoItems){
        this.videos =videoItems;
    }

    @NonNull
    @Override
    public SpecialAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SpecialAdapter.VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_video,parent
                ,false));
    }

    private static final String TAG = "VideoAdapter";

    @Override
    public void onBindViewHolder(@NonNull SpecialAdapter.VideoViewHolder holder, int position) {
        holder.setVideoData(videos.get(position));
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }
    static class VideoViewHolder extends RecyclerView.ViewHolder{
        TextureVideoView videoView;
        TextView textVideoTitle, textVideoDate,textVideoUsername;
        TextView like_number,comment_number,share_number;
        ImageView image_plus;
        CircleImageView mprofileImage;
        ProgressBar videoProgressBar;

        UserAccountSettings settings = new UserAccountSettings();
        User user  = new User();
        StringBuilder users;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Video video;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView=itemView.findViewById(R.id.videoView);
            textVideoTitle=itemView.findViewById(R.id.textVideoTitle);
            textVideoDate=itemView.findViewById(R.id.textVideoDate);
            textVideoUsername=itemView.findViewById(R.id.textVideoUsername);
            like_number=itemView.findViewById(R.id.like_number);
            comment_number=itemView.findViewById(R.id.comment_number);
            share_number=itemView.findViewById(R.id.share_number);
            image_plus=itemView.findViewById(R.id.image_plus);
            videoProgressBar=itemView.findViewById(R.id.videoProgressBar);
            mprofileImage=itemView.findViewById(R.id.profile_image);
        }

        void setVideoData(Video video){
            Context context=videoView.getContext();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child("user_account_settings")
                    .orderByChild("user_id")
                    .equalTo(video.getUser_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        Log.d(TAG,"IT's Okay..." +singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                        textVideoUsername.setText("@"+singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                        textVideoTitle.setText(video.getCaption());
                        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(mprofileImage.getContext()));
                        ImageLoader imageLoader = ImageLoader.getInstance();
                        imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                                mprofileImage);
                       textVideoUsername.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(context, ProfileActivity.class);
                                intent.putExtra(context.getString(R.string.calling_activity),
                                        context.getString(R.string.home_activity));
                                intent.putExtra(context.getString(R.string.intent_user), user);
                                context.startActivity(intent);
                            }
                        });

                        mprofileImage.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        Intent intent = new Intent(context, ProfileActivity.class);
                                                                        intent.putExtra(context.getString(R.string.calling_activity),
                                                                                context.getString(R.string.home_activity));
                                                                        intent.putExtra(context.getString(R.string.intent_user), user);
                                                                        context.startActivity(intent);
                                                                    }


                                                                }
                        );


//                        videoView.setVideoURI(Uri.parse(video.getVideo_path()));
//                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                            @Override
//                            public void onPrepared(MediaPlayer mediaPlayer) {
//                                videoProgressBar.setVisibility(View.GONE);
//                                mediaPlayer.start();
//
//                                float videoRatio = mediaPlayer.getVideoWidth() /(float) mediaPlayer.getVideoHeight();
//                                float screenRatio = videoView.getWidth() /(float) videoView.getHeight();
//
//                                float scale = videoRatio/screenRatio;
//
//                                if(scale >= 1f){
//                                    videoView.setScaleX(scale);
//                                }else {
//                                    videoView.setScaleY(1f / scale);
//                                }
//                            }
//
//
//                        });
//                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                            @Override
//                            public void onCompletion(MediaPlayer mediaPlayer) {
//                                mediaPlayer.start();
//                            }
//                        });
//                        videoView.setOnTouchListener(new View.OnTouchListener() {
//                            @Override
//                            public boolean onTouch(View view, MotionEvent motionEvent) {
//                                if(videoView.isPlaying()){
//                                    Toast.makeText(videoView.getContext(), "Video Paused", Toast.LENGTH_SHORT).show();
//                                    videoView.pause();
//
//                                }else{
//                                    Toast.makeText(videoView.getContext(), "Video Played", Toast.LENGTH_SHORT).show();
//
//                                    videoView.start();
//                                }
//                                return false;
//                            }
//                        });

                        settings = singleSnapshot.getValue(UserAccountSettings.class);
//                        holder.comment.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                ((MainActivity)context).onCommentThreadSelected(postList.get(holder.getAdapterPosition()),
//                                        context.getString(R.string.home_activity));
//
//                                //another thing?
////                            ((MainActivity)mContext).hideLayout();
//                            }
//                        });
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
                    .equalTo(video.getUser_id());
            userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        user = singleSnapshot.getValue(User.class);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }
}
