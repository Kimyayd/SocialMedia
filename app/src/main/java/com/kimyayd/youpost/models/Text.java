package com.kimyayd.youpost.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class Text implements Parcelable {  private String caption;
    private String date_created;
    private String text_id;
    private String user_id;
    private String tags;
    private List<Like> likes;
    private List<Comment> comments;

    public static final Creator<Text> CREATOR = new Creator<Text>() {
        @Override
        public Text createFromParcel(Parcel in) {
            return new Text(in);
        }

        @Override
        public Text[] newArray(int size) {
            return new Text[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "Text{" +
                "caption='" + caption + '\'' +
                ", date_created='" + date_created + '\'' +
                ", text_id='" + text_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", tags='" + tags + '\'' +
                ", likes=" + likes +
                '}';
    }

    public Text() {
    }

    public Text(String caption, String date_created,String text_id, String user_id, String tags, List<Like> likes, List<Comment> comments) {
        this.caption = caption;
        this.date_created = date_created;
        this.text_id = text_id;
        this.user_id = user_id;
        this.tags = tags;
        this.likes = likes;
        this.comments = comments;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getText_id() {
        return text_id;
    }

    public void setText_id(String text_id) {
        this.text_id = text_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    protected Text(Parcel in) {
        caption = in.readString();
        date_created = in.readString();
        text_id = in.readString();
        user_id = in.readString();
        tags = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(caption);
        dest.writeString(date_created);
        dest.writeString(text_id);
        dest.writeString(user_id);
        dest.writeString(tags);
    }


    @Override
    public int describeContents() {
        return 0;
    }
}
