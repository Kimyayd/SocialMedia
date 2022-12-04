package com.kimyayd.youpost.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

public class RealVideo implements Parcelable {
    private Video video;
    private List<Like> likes;
    private List<Comment> comments;

    public RealVideo(Video video, List<Like> likes, List<Comment> comments) {
        this.video = video;
        this.likes = likes;
        this.comments = comments;
    }
    public static final Parcelable.Creator<RealVideo> CREATOR = new Parcelable.Creator<RealVideo>() {
        @Override
        public RealVideo createFromParcel(Parcel in) {
            return new RealVideo(in);
        }

        @Override
        public RealVideo[] newArray(int size) {
            return new RealVideo[size];
        }
    };

    public RealVideo(Parcel in) {
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public RealVideo() {
    }

    @Override
    public String toString() {
        return "RealVideo{" +
                "video=" + video +
                ", likes=" + likes +
                '}';
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
