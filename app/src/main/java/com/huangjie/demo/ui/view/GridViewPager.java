package com.huangjie.demo.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.huangjie.demo.controller.GridController;

/**
 * Created by huangjie on 2017/6/15.
 */

public class GridViewPager extends ViewPager {

    private static final String TAG = "GridViewPager";
    private GridController mGridController;
    public GridViewPager(Context context) {
        super(context);
    }

    public GridViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setGridController(GridController controller) {
        this.mGridController = controller;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mGridController.isDraging()) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mGridController.isDraging()) {
            return false;
        }
        return super.onTouchEvent(ev);
    }
}
