package com.example.george.sharedelementimplementation.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class Screens {

    private Screens() { }

    public static int getScreenWidth(WindowManager wm) {
        int screenWidth;
        Display display = wm.getDefaultDisplay();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            final Point point = new Point();
            display.getSize(point);
            screenWidth = point.x;
        } else { // For older version
            screenWidth = display.getWidth();
        }
        return screenWidth;
    }

    public static int getScreenHeight(WindowManager wm) {
        int screenHeight;
        Display display = wm.getDefaultDisplay();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            final Point point = new Point();
            display.getSize(point);
            screenHeight = point.y;
        } else { // For older version
            screenHeight = display.getHeight();
        }
        return screenHeight;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

}
