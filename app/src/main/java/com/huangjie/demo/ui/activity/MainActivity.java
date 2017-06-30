package com.huangjie.demo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.huangjie.demo.controller.GridController;
import com.huangjie.demo.ui.R;
import com.huangjie.demo.ui.view.GridEditLayout;
import com.huangjie.demo.ui.view.GridViewPager;

public class MainActivity extends Activity implements GridController.EditModeChangeListener{

    private FrameLayout mContainerLayout;
    private GridController mController;
    private GridViewPager mViewPager;
    private GridEditLayout mEditLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainerLayout = (FrameLayout) findViewById(R.id.main_layout);
        mViewPager = (GridViewPager) mContainerLayout.findViewById(R.id.grid_view_pager);
        mController = new GridController(this, mContainerLayout, mViewPager);
        mEditLayout = new GridEditLayout(this, mController);
        mEditLayout.setVisibility(View.GONE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainerLayout.addView(mEditLayout, params);
        mController.registerEditModeChangeListener(this);
    }

    @Override
    public void editModeChange(boolean editMode) {
        if (editMode) {
            mEditLayout.setVisibility(View.VISIBLE);
        } else {
            mEditLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mController != null) {
            mController.unregisterEditModeChangeListener();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mController.onAddActivityResult(data);
        }
    }
}
