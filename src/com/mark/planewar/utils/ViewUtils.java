package com.mark.planewar.utils;

import android.content.Context;

/**
 * Created by zyf on 2015/5/24.
 */
public class ViewUtils {
    public static float density = -1;

    public static int dpToPx(Context context, int dp) {
        if (density < 0) {
            density = context.getResources().getDisplayMetrics().density;
        }
        return (int) (density * dp + 0.5f);
    }

    public static boolean isOverLapping(float x1, float y1, int width1, int height1,
                                        float x2, float y2, int width2, int height2) {
        if (x1 <= x2 && x1 + width1 <= x2) {
            return false;
        } else if (x2 <= x1 && x2 + width2 <= x1) {
            return false;
        } else if (y1 <= y2 && y1 + height1 <= y2) {
            return false;
        } else if (y2 <= y1 && y2 + height2 <= y1) {
            return false;
        }
        return true;
    }
}