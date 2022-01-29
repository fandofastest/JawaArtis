package com.satux.duax.tigax.utils;

import android.content.Context;
import android.widget.Toast;

import com.satux.duax.tigax.R;

public class CommonUtils {

    private Context context;

    public CommonUtils(Context context) {
        this.context = context;
    }

    public void showTheToast(String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }

    public int accentColor(SharedPrefsUtils sharedPrefsUtils) {
        switch (sharedPrefsUtils.readSharedPrefsString("accentColor", "pink")) {
            case "green":
                return R.color.green;
            case "orange":
                return R.color.orange;
            case "pink":
                return R.color.pink;
            case "cyan":
                return R.color.cyan;
            case "yellow":
                return R.color.yellow;
            case "purple":
                return R.color.purple;
            case "red":
                return R.color.red;
            case "grey":
                return R.color.grey;
            default:
                return R.color.pink;
        }
    }
}
