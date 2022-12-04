package com.kimyayd.youpost.models;

public class VideoItem {
    public String videoURL,videoTitle,videoDescription;

    public VideoItem(String videoURL, String videoTitle, String videoDescription) {
        this.videoURL = videoURL;
        this.videoTitle = videoTitle;
        this.videoDescription = videoDescription;
    }

    public VideoItem() {
    }

    @Override
    public String toString() {
        return "VideoItem{" +
                "videoURL='" + videoURL + '\'' +
                ", videoTitle='" + videoTitle + '\'' +
                ", videoDescription='" + videoDescription + '\'' +
                '}';
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getVideoDescription() {
        return videoDescription;
    }

    public void setVideoDescription(String videoDescription) {
        this.videoDescription = videoDescription;
    }
}
