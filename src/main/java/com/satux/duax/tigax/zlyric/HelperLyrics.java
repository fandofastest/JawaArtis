package com.satux.duax.tigax.zlyric;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class HelperLyrics {

    public static List<ModelLyrics> retrieveShoutcasts(String s) {

        String json = s.replace("[", "\"},{\"time\":\"").replace("]", "\",\"text\":\"").replace("#\"},", "[").replace("#", "\"}]");

        return (new Gson()).fromJson(json, new TypeToken<List<ModelLyrics>>() {
        }.getType());
    }
}
