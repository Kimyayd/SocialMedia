package com.kimyayd.youpost.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.message.MessageActivity;
import com.kimyayd.youpost.models.Chat;
import com.kimyayd.youpost.models.UserAccountSettings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<UserAccountSettings> mUsers;
    private boolean ischat;
    String theLastMessage;
    String theLastMessageDate;
    String theLastMessageHour;
    public UserAdapter(Context mContext,List<UserAccountSettings> mUsers,boolean ischat){
        this.mUsers=mUsers;
        this.mContext=mContext;
        this.ischat=ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new ViewHolder(view);
    }

    private static final String TAG = "UserAdapter";
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

       final UserAccountSettings user= mUsers.get(position);
       holder.username.setText(user.getUsername());
        if(user.getProfile_photo().equals("default")){
            holder.profile_image.setImageResource(R.drawable.ic_user);
        }else {
            UniversalImageLoader.setImage(user.getProfile_photo(), holder.profile_image, null, "");
        }

    if(ischat){
        lastMessage(user.getUser_id(), holder.last_msg, holder.last_msg_date);
    }else{
        holder.last_msg.setVisibility(View.GONE);
    }
    holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(mContext, MessageActivity.class);
            intent.putExtra("userid",user.getUser_id());
            mContext.startActivity(intent);
        }
    });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView username;
        private ImageView profile_image;
        private TextView last_msg;
        private TextView last_msg_date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username=itemView.findViewById(R.id.username);
            profile_image=itemView.findViewById(R.id.profile_image);
            last_msg=itemView.findViewById(R.id.last_msg);
            last_msg_date=itemView.findViewById(R.id.last_msg_date);

        }
    }
    private void lastMessage(String userid, TextView last_msg,TextView last_msg_date){
        theLastMessageDate = "default";
        theLastMessageHour = "default";
        theLastMessage = "default";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (!chat.isDelete()){
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {

                            theLastMessage = chat.getMessage();
                            theLastMessageDate = chat.getDate();
                            theLastMessageHour = chat.getHour();


                        }
                }
            }
            switch (theLastMessage){
                case "default" :
                    last_msg.setText("No Message");
                    break;

                default:
                    last_msg.setText(theLastMessage);
                    String timestampDifference = getTimestampDifference(theLastMessageDate);
                    if(!timestampDifference.equals("0")){
                        if(timestampDifference.equals("1")) {
                            last_msg_date.setText("YESTERDAY");
                        }else{
                            last_msg_date.setText(timestampDifference + " DAYS AGO");
                        }
                    }else{
                        last_msg_date.setText(theLastMessageHour);
                    }

                    break;
            }
            theLastMessage = "default";


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private String getTimestampDifference(String chat){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        try{
            timestamp = sdf.parse(chat);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }
}
