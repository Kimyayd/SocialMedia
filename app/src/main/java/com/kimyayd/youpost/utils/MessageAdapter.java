package com.kimyayd.youpost.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.kimyayd.youpost.models.Chat;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TITLE_LEFT=0;
    public static final int MSG_TITLE_RIGHT=1;


    private Context mContext;
    private List<Chat> mChats;
    private String imageurl;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChats,String imageurl) {
        this.mChats = mChats;
        this.mContext = mContext;
        this.imageurl=imageurl;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TITLE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        }else{
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);

        }
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
             Chat chat=mChats.get(position);

             holder.show_message.setText(chat.getMessage());
        if(imageurl.equals("default")){
            holder.profile_image.setImageResource(R.drawable.ic_user);
        }else {
            UniversalImageLoader.setImage(imageurl, holder.profile_image, null, "");
        }

             if(position == mChats.size()-1){
                 if(chat.isIsseen()){
                     holder.txt_seen.setText("Seen");
                 }
                 else{
                     holder.txt_seen.setText("Sent");
                 }
             }
             else{
                     holder.txt_seen.setVisibility(View.GONE);
             }



    }


    @Override
    public int getItemCount() {
        return mChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
       fuser= FirebaseAuth.getInstance().getCurrentUser();
       if(mChats.get(position).getSender().equals(fuser.getUid())){
           return MSG_TITLE_RIGHT;
       }else{
           return MSG_TITLE_LEFT;
       }
    }
}


