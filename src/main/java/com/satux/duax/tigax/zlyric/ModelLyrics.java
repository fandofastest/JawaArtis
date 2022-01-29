package com.satux.duax.tigax.zlyric;

import com.google.gson.annotations.SerializedName;

public class ModelLyrics {

    @SerializedName("time")
    private String time;

    @SerializedName("text")
    private String text;

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }
    public void getText(String text) {
        this.text = text;
    }
}
