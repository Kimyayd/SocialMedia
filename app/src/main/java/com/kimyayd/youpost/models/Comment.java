package com.kimyayd.youpost.models;


import java.util.List;

public class Comment {

    private String comment;
    private String user_id;
    private String post_id;
    private String comment_id;
    private List<Like> likes;
    private String reply_user_id;
    private String date_created;

    public Comment() {

    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public Comment(String comment, String user_id, String post_id, String comment_id, List<Like> likes, String reply_user_id, String date_created) {
        this.comment = comment;
        this.user_id = user_id;
        this.post_id = post_id;
        this.comment_id = comment_id;
        this.likes = likes;
        this.reply_user_id = reply_user_id;
        this.date_created = date_created;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getReply_user_id() {
        return reply_user_id;
    }

    public void setReply_user_id(String reply_user_id) {
        this.reply_user_id = reply_user_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", likes=" + likes +
                ", reply_user_id='" + reply_user_id + '\'' +
                ", date_created='" + date_created + '\'' +
                '}';
    }
}