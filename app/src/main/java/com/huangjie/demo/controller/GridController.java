package com.huangjie.demo.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huangjie.demo.adapter.GridViewAdapter;
import com.huangjie.demo.adapter.GridViewPagerAdapter;
import com.huangjie.demo.data.GridItem;
import com.huangjie.demo.manager.GridManager;
import com.huangjie.demo.ui.R;
import com.huangjie.demo.ui.activity.AddGridActivity;
import com.huangjie.demo.ui.activity.BrowserActivity;
import com.huangjie.demo.ui.view.DragGridView;
import com.huangjie.demo.ui.view.GridEditLayout;
import com.huangjie.demo.ui.view.GridViewPager;
import com.huangjie.demo.util.DimenUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by huangjie on 2017/6/15.
 */

public class GridController implements ViewPager.OnPageChangeListener {

    public static final int GRID_COLUMN_NUMBERS = 5;
    private FrameLayout mGridContainer;
    private Context mContext;
    private boolean mDraging = false;
    private boolean mEditMode = false;
    private GridViewPager mViewPager;
    private List<GridItem> mGridList;
    private List<DragGridView> mGridViewList = new ArrayList<DragGridView>();
    private static final String TAG = GridController.class.getSimpleName();
    public static final int MAX_GRID_NUMBER = 60;
    public static final int PER_PAGE_NUMBER = 15;
    public static final int SCREEN_WIDTH = DimenUtils.getScreenWidthPixels();
    private int mGridViewLeftPadding;
    private int mGridViewTopPadding;
    private int mGridViewRightPadding;
    private int mGridViewBotttomPadding;
    private int mGridItemWidth;
    private int mHorizontalSpacing;
    private int mGridSectionHeight;
    private int mVerticalSpacing;
    private int mCurrentPageIndex = 0;
    private int mDragPosition = -1;   //绝对位置，是全局的
    private int mHidePosition = -1;
    private int mDragPage = -1;
    private boolean mIsDraging = false;
    public boolean eventTransferSuccess = false;
    private GridItem mDragingItem = null;
    private int mPagesCount = 0;
    private int mGridsCount = 0;
    private EditModeChangeListener mListener;
    private GridItem mTempEmptyItem;

    public GridController(Context context, FrameLayout gridContainer,GridViewPager viewPager) {
        mContext = context;
        mGridContainer = gridContainer;
        mViewPager = viewPager;
        mViewPager.setGridController(this);
        mViewPager.setAdapter(new GridViewPagerAdapter(mGridViewList));
        mViewPager.setOnPageChangeListener(this);
        mGridViewLeftPadding = mContext.getResources().getDimensionPixelSize(R.dimen.grid_view_left_padding);
        mGridViewTopPadding = mContext.getResources().getDimensionPixelSize(R.dimen.grid_view_top_padding);
        mGridViewRightPadding = mContext.getResources().getDimensionPixelSize(R.dimen.grid_view_right_padding);
        mGridViewBotttomPadding = mContext.getResources().getDimensionPixelSize(R.dimen.grid_view_bottom_padding);
        mGridItemWidth = mContext.getResources().getDimensionPixelSize(R.dimen.grid_item_width);
        mGridSectionHeight = mContext.getResources().getDimensionPixelSize(R.dimen.grid_section_height);
        mHorizontalSpacing = (SCREEN_WIDTH - (mGridViewLeftPadding + mGridViewRightPadding) - GRID_COLUMN_NUMBERS * mGridItemWidth) / (GRID_COLUMN_NUMBERS - 1);
        initGridData();
    }

    public boolean isDraging() {
        return mDraging;
    }

    public void setDraging(boolean draging) {
        this.mDraging = draging;
    }

    public boolean isEditMode() {
        return mEditMode;
    }

    public void setEditMode(boolean editMode) {
        this.mEditMode = editMode;
    }

    public void enterEditMode() {
        setEditMode(true);
        if (mListener != null) {
            mListener.editModeChange(true);
        }
        adjustViewPagerDataIfNeed();
        notifyAllAdapters(true);

    }

    public void exitEditMode() {
        setEditMode(false);
        if (mListener != null) {
            mListener.editModeChange(false);
        }
        notifyAllAdapters(false);
        adjustViewPagerDataIfNeed();
    }

    public int getCurrentPageIndex() {
        return mCurrentPageIndex;
    }

    public void setHidePosition(int position) {
        mHidePosition = position;
    }

    public int getHidePosition() {
        return mHidePosition;
    }

    public void setDragPosition(int pageIndex, int dragPosition) {
        if(dragPosition == -1){
            this.mDragPosition = -1;
            return;
        }
        mDragPage = pageIndex;
        mDragPosition = dragPosition + pageIndex * PER_PAGE_NUMBER;
    }

    /**
     *
     * @return 返回拖动宫格的绝对位置
     */
    public int getDragPosition() {
        return mDragPosition;
    }

    /**
     *
     * @param pageIndex 拖动的宫格所在的页面索引
     * @return 相对所在页面的相对位置
     */
    public int getDragPosition(int pageIndex) {
        return mDragPosition - pageIndex * PER_PAGE_NUMBER;
    }

    public int getDragPage() {
        return mDragPage;
    }

    /**
     * 拖动宫格对应的GridItem
     * @return
     */
    public GridItem getDragingItem() {
        return mDragingItem;
    }

    public void setDragingItem(GridItem dragingItem) {
        this.mDragingItem = dragingItem;
    }

    /**
     * 获得当前所在页面的GridView
     * @return
     */
    public DragGridView getCurrentPageGridView()
    {
        int pageIndex = mCurrentPageIndex;
        if(pageIndex > -1 && pageIndex < mGridViewList.size()){
            return mGridViewList.get(pageIndex);
        }
        return null;
    }

    /**
     * 获得拖拽宫格对应的GridView
     * @param pageIndex  页面索引
     * @return
     */
    public DragGridView getDragGridView(int pageIndex) {
        if (pageIndex >= 0 && pageIndex <= mPagesCount) {
            return mGridViewList.get(pageIndex);
        }
        return null;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_SETTLING) {
            if (mCurrentPageIndex != mViewPager.getCurrentItem()) {
                Log.d(TAG, "onPageScrollStateChanged currentPageIndex:" + mCurrentPageIndex + " nextPage:" + mViewPager.getCurrentItem());
                mCurrentPageIndex = mViewPager.getCurrentItem();
            }
        }
    }

    public void prePage() {
        if (mCurrentPageIndex >= 1) {
            Log.d(TAG, "currentPage:" + mCurrentPageIndex + " prePage:" + (mCurrentPageIndex - 1));
            mViewPager.setCurrentItem(mCurrentPageIndex - 1,true);
        }
    }

    public void nextPage() {
        if (mCurrentPageIndex <= mPagesCount - 1) {
            Log.d(TAG, "currentPage:" + mCurrentPageIndex + " nextPage:" + (mCurrentPageIndex + 1));
            mViewPager.setCurrentItem(mCurrentPageIndex + 1,true);
        }
    }

    public int getPagesCount() {
        if (mGridViewList.size() - 1 != mPagesCount) {
            mPagesCount = mGridViewList.size() - 1;
        }
        return mPagesCount;
    }

    public int getGridsCount() {
        if (mGridList.size() != mGridsCount) {
            mGridsCount = mGridList.size();
        }
        return mGridsCount;
    }

    public void reorderItems(int oldPosition, int oldPageIndex, int newPosition, int newPageIndex) {
        Log.d(TAG, "reorderItems oldPosition:" + oldPosition + " newPosition:" + newPosition);
        if (oldPosition >= 0 && newPosition >= 0 && (oldPosition) < getGridsCount() && newPosition < getGridsCount()) {
            GridItem item = mGridList.get(oldPosition);
            swapDataAndResetViewPosition(oldPosition, newPosition);
            mGridList.set(newPosition, item);
            item.setPosition(newPosition);
            GridManager.getInstance().updateGridItem(item, newPosition, true, true);
            Log.d(TAG, "reorderItems oldPageIndex:" + oldPageIndex + " newPageIndex:" + newPageIndex);
            int startIndex, endIndex;
            if (oldPageIndex < newPageIndex) {
                startIndex = oldPageIndex;
                endIndex = newPageIndex;
            } else {
                startIndex = newPageIndex;
                endIndex = oldPageIndex;
            }
            if (startIndex != endIndex) {
                for (int i = startIndex; i <= endIndex; i++) {
                    if (mGridViewList.get(i) != null) {
                        ((GridViewAdapter) mGridViewList.get(i).getAdapter()).notifyDataSetChanged();
                    }
                }
            }
        }
    }

    public void swapDataAndResetViewPosition(int oldPosition, int newPosition) {
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(mGridList, i, i + 1);
                mGridList.get(i).setPosition(i);
                if (mGridList.get(i).getType() != GridItem.GRID_TYPE.TYPE_EMPTY) {
                    GridManager.getInstance().updateGridItem(mGridList.get(i), i, true, true);
                }
            }
        } else if (oldPosition > newPosition) {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(mGridList, i, i - 1);
                mGridList.get(i).setPosition(i);
                if (mGridList.get(i).getType() != GridItem.GRID_TYPE.TYPE_EMPTY) {
                    GridManager.getInstance().updateGridItem(mGridList.get(i), i, true, true);
                }
            }
        }
    }

    private void initGridData() {
        List<GridItem> gridList = GridManager.getInstance().getGridList();
        if (gridList == null) {
            GridManager.getInstance().loadGrid(new GridManager.LoadListener() {
                @Override
                public void onLoadFinished(final List<GridItem> itemList) {
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setData(itemList);
                        }
                    });
                }

                @Override
                public void onLoadFailed(String failReason) {
                    Log.d(TAG, "fail msg:" + failReason);
                }
            });
        } else {
            setData(gridList);
        }
    }

    private void setData(List<GridItem> items) {
        mGridsCount = items.size();
        mGridList = items;
        mPagesCount = (mGridsCount - 1) / PER_PAGE_NUMBER;
        for (int i = 0; i <= mPagesCount; i++) {
            DragGridView gridView = createGridView(i);
            mGridViewList.add(gridView);
        }
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    private void notifyAllAdapters(boolean editMode) {
        if (editMode) {
            if (mGridList.get(mGridsCount - 1).getType() == GridItem.GRID_TYPE.TYPE_EMPTY) {
                mTempEmptyItem = mGridList.get(mGridsCount - 1);
                GridManager.getInstance().deleteGridItem(mGridList.get(mGridsCount - 1), false, true);
                mGridsCount--;
            }
        } else {
            if (mTempEmptyItem != null) {
                GridManager.getInstance().insertGridItem(mTempEmptyItem, false, true);
                mTempEmptyItem = null;
                mGridsCount++;
            }
        }

        for (DragGridView gridView : mGridViewList) {
            ((GridViewAdapter)gridView.getAdapter()).notifyDataChanged(mGridList);
        }
    }

    private void adjustViewPagerDataIfNeed() {
        if (mEditMode) {     //进入编辑模式
            if (mGridsCount % PER_PAGE_NUMBER == 1) {
                mGridViewList.remove(mPagesCount);
                mPagesCount--;
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        } else {             //退出编辑模式
            if (mGridsCount % PER_PAGE_NUMBER == 1) {
                mGridViewList.add(createGridView(++mPagesCount));
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    public void handleOnTouch(MotionEvent event, int visibleSectionTop) {
        if (mViewPager != null) {
            try {
                Log.d(TAG, "-----before:" + event.getAction() + " getY:" + event.getY());
                MotionEvent ev = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getX(), event.getY() - visibleSectionTop, 0);
                Log.d(TAG, "=====after:" + ev.getAction() + " getY:" + ev.getY());
                mViewPager.dispatchTouchEvent(ev);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private DragGridView createGridView(int pageIndex) {
        DragGridView gridView = new DragGridView(mContext, pageIndex, this);
        gridView.setPadding(mGridViewLeftPadding, mGridViewTopPadding, mGridViewRightPadding, mGridViewBotttomPadding);
        gridView.setHorizontalSpacing(mHorizontalSpacing);
        gridView.setColumnWidth(mGridItemWidth);
        gridView.setNumColumns(GRID_COLUMN_NUMBERS);
        gridView.setSelector(R.color.transparent);
        gridView.setHorizontalScrollBarEnabled(false);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.setCacheColorHint(0);
        gridView.setClipChildren(false);
        gridView.setClipToPadding(false);
        gridView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        GridViewAdapter adapter = new GridViewAdapter(mContext, pageIndex, this, mGridList, gridView);
        gridView.setAdapter(adapter);
        return gridView;
    }

    public void handleDelGrid(final GridItem item, int pageIndex) {
        Log.d(TAG, "handleDelGrid item viewPosition:" + item.getPosition());
        int position = item.getPosition();
        for (int i = position + 1; i <= mGridsCount - 1; i++) {
            GridItem gridItem = mGridList.get(i);
            GridManager.getInstance().updateGridItem(gridItem, gridItem.getPosition() - 1, true, true);
        }
        GridManager.getInstance().deleteGridItem(item, true, true);
        for (int i = pageIndex; i <= mPagesCount; i++) {
            ((GridViewAdapter)(mGridViewList.get(i).getAdapter())).notifyDataChanged(mGridList);
        }
        mGridsCount--;
        int currentPagesCount = mPagesCount;
        mPagesCount = (mGridsCount - 1) / PER_PAGE_NUMBER;
        if (mPagesCount < currentPagesCount) {
            mGridViewList.remove(currentPagesCount);
            mViewPager.getAdapter().notifyDataSetChanged();
        }
    }

    public void handleAddGrid(String title, String url) {
        GridItem gridItem = new GridItem();
        gridItem.setTitle(title);
        gridItem.setUrl(url);
        gridItem.setType(GridItem.GRID_TYPE.TYPE_NORMAL);
        gridItem.setBitmap(BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.ic_launcher));
        gridItem.setIconPath("");
        gridItem.setPosition(mGridsCount - 1);
        gridItem.setID(mGridsCount - 1);
        GridManager.getInstance().updateGridItem(mGridList.get(mGridsCount - 1), mGridsCount, true, true);
        mGridList.add(mGridList.get(mGridsCount - 1));
        mGridList.set(mGridsCount - 1, gridItem);
        mGridsCount++;
        int currentPages = mPagesCount;
        mPagesCount = (mGridsCount - 1) / PER_PAGE_NUMBER;
        if (mPagesCount > currentPages) {
            mGridViewList.add(createGridView(mPagesCount));
            mViewPager.getAdapter().notifyDataSetChanged();

        }
        for (int i = mCurrentPageIndex; i <= mPagesCount; i++) {
            ((GridViewAdapter) mGridViewList.get(i).getAdapter()).notifyDataChanged(mGridList);
        }

    }

    public void onItemClick(GridItem item) {
        if(item != null) {
            if (item.getType() == GridItem.GRID_TYPE.TYPE_NORMAL) {
                if (!TextUtils.isEmpty(item.getUrl())) {
                    Intent intent = new Intent(mContext, BrowserActivity.class);
                    intent.putExtra("url", item.getUrl());
                    mContext.startActivity(intent);
                }
            } else {
                Intent intent = new Intent(mContext, AddGridActivity.class);
                ((Activity)mContext).startActivityForResult(intent, 0);
                ((Activity)mContext).overridePendingTransition(R.anim.activity_zoom_in, R.anim.activity_zoom_out);
            }
        }
    }

    public void onAddActivityResult(Intent intent) {
        String title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
            Toast.makeText(mContext, "网址链接或标题为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        handleAddGrid(title, url);
    }

    public void registerEditModeChangeListener(EditModeChangeListener listener) {
        this.mListener = listener;
    }

    public void unregisterEditModeChangeListener() {
        this.mListener = null;
    }

    public interface EditModeChangeListener {
        void editModeChange(boolean editMode);
    }
}
