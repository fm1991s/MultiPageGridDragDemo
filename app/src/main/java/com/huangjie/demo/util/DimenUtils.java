package com.huangjie.demo.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.huangjie.demo.GApplication;

/**
 * Created by huangjie on 2017/6/15.
 */

public class DimenUtils {

    private static DisplayMetrics mMetrics = null;
    private static int mStatusBarHeight = 0;

    public static DisplayMetrics getDisplayMetrics() {
        if (mMetrics != null) {
            return mMetrics;
        }
        Context context = GApplication.getApplication().getApplicationContext();
        if (context != null && context.getResources() != null) {
            mMetrics = context.getResources().getDisplayMetrics();
        }
        return mMetrics;
    }

    public static int getStatusBarHeight() {
        if (mStatusBarHeight != 0) {
            return mStatusBarHeight;
        }
        Class<?> localClass;
        Context context = GApplication.getApplication().getApplicationContext();
        try {
            localClass = Class.forName("com.android.internal.R$dimen");
            Object localObject = localClass.newInstance();
            int resId = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
            mStatusBarHeight = context.getResources().getDimensionPixelSize(resId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mStatusBarHeight;
    }

    public static int getScreenWidthPixels() {
        if (mMetrics == null) {
            getDisplayMetrics();
        }
        return mMetrics.widthPixels;
    }

    public static int getScreenHeightPixels() {
        if (mMetrics == null) {
            getDisplayMetrics();
        }
        return mMetrics.heightPixels;
    }

    public static int dp2Px(float dp) {
        if (mMetrics == null) {
            getDisplayMetrics();
        }
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mMetrics);
    }

    public static int sp2Px(int sp) {
        if (mMetrics == null) {
            getDisplayMetrics();
        }
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, mMetrics);
    }

    public static int px2Dp(int px) {
        if (mMetrics == null) {
            getDisplayMetrics();
        }
        return (int)(px / mMetrics.densityDpi);
    }

    public static int px2Sp(int sp) {
        if (mMetrics == null) {
            getDisplayMetrics();
        }
        return (int)(sp / mMetrics.scaledDensity);
    }
}
