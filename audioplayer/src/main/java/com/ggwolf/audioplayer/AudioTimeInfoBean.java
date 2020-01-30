package com.ggwolf.audioplayer;

public class AudioTimeInfoBean {
    private int currentTime;
    private int totalTime;

    public int getCurrentTime() {
        return currentTime;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        return "AudioTimeInfoBean{" +
                "currentTime=" + currentTime +
                ", totalTime=" + totalTime +
                '}';
    }
}
