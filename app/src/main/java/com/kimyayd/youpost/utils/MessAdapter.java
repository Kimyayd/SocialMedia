package com.kimyayd.youpost.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.Chat;
import com.kimyayd.youpost.models.UserAccountSettings;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessAdapter extends ArrayAdapter<Chat> {

    public static final int MSG_TITLE_LEFT=0;
    public static final int MSG_TITLE_RIGHT=1;
    private static final String TAG = "UserListAdapter";


    private LayoutInflater mInflater;
    private List<Chat> mChats = null;
    private int layoutResource;
    private Context mContext;
    FirebaseUser fuser;
    private String imageurl;


    public MessAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Chat> objects,@NonNull String imageurl) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mChats = objects;
        this.imageurl=imageurl;
    }

    private static class ViewHolder{
        TextView show_message;
        ImageView profile_image;
        TextView txt_seen;

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
       if(getItemViewType(position)==MSG_TITLE_LEFT){

           if(convertView ==null){
               convertView = mInflater.inflate(R.layout.chat_item_left, parent, false);
               holder = new ViewHolder();
               holder.show_message = (TextView) convertView.findViewById(R.id.show_message);
               holder.txt_seen = (TextView) convertView.findViewById(R.id.txt_seen);
               convertView.setTag(holder);

               Chat chat = mChats.get(position);
//               if (!chat.isReceiver_delete()){
                   holder.show_message.setText(chat.getMessage());

                   if (position == mChats.size() - 1) {
                       if (chat.isIsseen()) {
                           holder.txt_seen.setText("Seen");
                       } else {
                           holder.txt_seen.setText("Sent");
                       }
                   } else {
                       holder.txt_seen.setVisibility(View.GONE);
                   }
//               }else{
////                   mChats.remove(position);
//               }

           }else{
               holder = (ViewHolder) convertView.getTag();
           }

       }else{

           if(convertView ==null) {
               convertView = mInflater.inflate(R.layout.chat_item_right, parent, false);
               holder = new ViewHolder();
               holder.show_message = (TextView) convertView.findViewById(R.id.show_message);
               holder.txt_seen = (TextView) convertView.findViewById(R.id.txt_seen);
               convertView.setTag(holder);
               Chat chat = mChats.get(position);
               if (!chat.isDelete()){
                   holder.show_message.setText(chat.getMessage());
                   if (position == mChats.size() - 1) {
                       if (chat.isIsseen()) {
                           holder.txt_seen.setText("Seen");
                       } else {
                           holder.txt_seen.setText("Sent");
                       }
                   } else {
                       holder.txt_seen.setVisibility(View.GONE);
                   }
               }else{
                   mChats.remove(position);
               }

           }
           else{
               holder = (ViewHolder) convertView.getTag();
           }

       }



        return convertView;
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
