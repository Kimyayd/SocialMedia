package com.kimyayd.youpost.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Post implements Parcelable {
    private String caption;
    private String date_created;
    private String post_path;
    private String post_id;
    private String user_id;
    private String post_type;
    private List<Like> likes;
    private List<Comment> comments;

    protected Post(Parcel in) {
        caption = in.readString();
        date_created = in.readString();
        post_path = in.readString();
        post_id = in.readString();
        user_id = in.readString();
        post_type = in.readString();
    }
    public Post(String caption, String date_created, String post_path, String post_id, String user_id,String post_type, List<Like> likes, List<Comment> comments) {
        this.caption = caption;
        this.date_created = date_created;
        this.post_path = post_path;
        this.post_id = post_id;
        this.post_type=post_type;
        this.user_id = user_id;
        this.likes = likes;
        this.comments = comments;
    }
    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getPost_type() {
        return post_type;
    }

    public void setPost_type(String post_type) {
        this.post_type = post_type;
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

    public String getPost_path() {
        return post_path;
    }

    public void setPost_path(String post_path) {
        this.post_path = post_path;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public Post() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(caption);
        parcel.writeString(date_created);
        parcel.writeString(post_path);
        parcel.writeString(post_id);
        parcel.writeString(user_id);
        parcel.writeString(post_type);
    }

    @Override
    public String toString() {
        return "Post{" +
                "caption='" + caption + '\'' +
                ", date_created='" + date_created + '\'' +
                ", post_path='" + post_path + '\'' +
                ", post_id='" + post_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", post_type=" + post_type +
                ", likes=" + likes +
                ", comments=" + comments +
                '}';
    }
}
