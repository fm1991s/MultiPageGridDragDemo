package com.huangjie.demo.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.huangjie.demo.data.GridItem;
import com.huangjie.demo.ui.view.DragGridView;

import java.util.List;

/**
 * Created by huangjie on 2017/6/15.
 */

public class GridViewPagerAdapter extends PagerAdapter {

    private List<DragGridView> mGridViewList;

    public GridViewPagerAdapter(List<DragGridView> gridViews) {
        mGridViewList = gridViews;
    }

    @Override
    public int getCount() {
        return mGridViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object == view;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mGridViewList.get(position));
        return mGridViewList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);

    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
