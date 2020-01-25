
package com.ggwolf.audioplayer.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class UiUtils {


    public static int getScreenWidthPixels(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenHeightPixels(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(dm);
        return dm.heightPixels;
    }


}
