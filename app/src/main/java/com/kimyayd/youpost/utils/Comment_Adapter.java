package com.kimyayd.youpost.utils;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.Comment;
import com.kimyayd.youpost.models.Heart;
import com.kimyayd.youpost.models.User;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class Comment_Adapter extends ArrayAdapter<Comment> {

    private static final String TAG = "Comment_Adapter";
    Context context;
    List<Comment> commentList;
    String postId;
    private LayoutInflater mInflater;
    private int layoutResource;
    String use_id;

    public Comment_Adapter(@NonNull Context context, @LayoutRes int resource,@NonNull List<Comment> commentList,@NonNull String use_id,@NonNull String postId) {
        super(context,resource,commentList);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.context = context;
        this.commentList = commentList;
        this.postId = postId;
        this.use_id = use_id;
    }
    private static class ViewHolder{
        TextView comment, username, timestamp, reply, likes;
        CircleImageView profileImage;
        ImageView heartRed, heartWhite;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.comment = (TextView) convertView.findViewById(R.id.comment);
            holder.username = (TextView) convertView.findViewById(R.id.comment_username);
            holder.timestamp = (TextView) convertView.findViewById(R.id.comment_time_posted);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.likes = (TextView) convertView.findViewById(R.id.comment_likes);
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.comment_profile_image);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        String user_id = commentList.get(position).getUser_id();
        String comment = commentList.get(position).getComment();
        //set data
        String timestampDifference = getTimestampDifference(commentList.get(position));
        if(!timestampDifference.equals("0")){
            holder.timestamp.setText(timestampDifference + " d");
        }else{
            holder.timestamp.setText("today");
        }
        holder.comment.setText(comment);
        //set user dp
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("user_account_settings");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                    UserAccountSettings user = dataSnapshot.getValue(UserAccountSettings.class);

                    if(user.getUser_id().equals(user_id)) {
                        holder.username.setText(user.getUsername());
                        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));
                        ImageLoader imageLoader = ImageLoader.getInstance();
                        imageLoader.displayImage(user.getProfile_photo(),
                                holder.profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return convertView;
    }


    private String getTimestampDifference(Comment comment){
        Log.d(TAG, "getTimestampDifference: "+comment.toString());

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRENCH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDate_created();
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