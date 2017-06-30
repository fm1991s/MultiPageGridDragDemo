package com.huangjie.demo.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.huangjie.demo.adapter.GridViewAdapter;
import com.huangjie.demo.controller.GridController;
import com.huangjie.demo.data.GridItem;
import com.huangjie.demo.util.DimenUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by huangjie on 2017/6/15.
 */

public class DragGridView extends GridView {

    private int mPageIndex;
    private Context mContext;
    private GridController mGridController;
    private int mNumberColumns = 5;
    private int mColumnWidth;
    private int mHorizontalSpacing;
    private boolean mVerticalSpacingSet = false;
    private GridViewAdapter mAdapter;
    private Matrix mDragViewMatrix;
    private boolean mAnimationEnd = true;
    private static final String TAG = DragGridView.class.getSimpleName();
    /**
     * 开始拖拽的item对应的View
     */
    private View mStartDragItemView = null;

    public DragGridView(Context context, int pageIndex, GridController controller) {
        super(context);
        mContext = context;
        mPageIndex = pageIndex;
        mGridController = controller;
        mDragViewMatrix = new Matrix();
        mDragViewMatrix.postScale(1.2f, 1.2f);          //图标放大倍数1.2。
        setOnItemClickListener(itemClickListener);
        setOnItemLongClickListener(itemLongClickListener);
    }

    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!mGridController.isEditMode()) {
                GridItem item = (GridItem)mAdapter.getItem(position);
                mGridController.onItemClick(item);
            }
        }
    };

    OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            GridItem item = (GridItem)mAdapter.getItem(position);
            if (item.getType() == GridItem.GRID_TYPE.TYPE_EMPTY) {
                Log.d(TAG, "OnItemLongClickListener getType empty return");
                return true;
            }
            if (mGridController.isDraging()) {
                Log.d(TAG, "OnItemLongClickListener isGridDraging return");
                return true;
            }
            if (!mGridController.isEditMode()) {
                Log.d(TAG, "OnItemLongClickListener enterEditMode");
                mGridController.enterEditMode();
            }
            mGridController.setDragPosition(mPageIndex, position);
            mGridController.setDragingItem(item);
            Log.d(TAG, "OnItemLongClickListener mPageIndex:" + mPageIndex + " position:" + position + " item position:" + item.getPosition());
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mStartDragItemView = getChildAt(mGridController.getDragPosition(mPageIndex) - getFirstVisiblePosition());
                    ClipData data = ClipData.newPlainText("", "");
                    // 实例化drag shadow builder.
                    View.DragShadowBuilder shadowBuilder = new DragShadowBuilder(mStartDragItemView);
                    // 开始拖动
                    mStartDragItemView.startDrag(data,  // 要拖动的数据
                            shadowBuilder,  // drag shadow builder
                            null,      // 不需要用到本地数据
                            0          // 标志位（目前未启用，设为0）
                    );
                    Log.d(TAG, "OnItemLongClickListener mStartDragItemView.setVisibility(INVISIBLE) position:" + (mGridController.getDragPosition(mPageIndex) - getFirstVisiblePosition()));
                    mStartDragItemView.setVisibility(INVISIBLE);
                    mGridController.eventTransferSuccess = false;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!mGridController.eventTransferSuccess) {
                                onStopDrag(-1, -1);
                            }
                        }
                    }, 100);
                }
            }, 10);
            return true;
        }
    };

    private static class DragShadowBuilder extends View.DragShadowBuilder {

        // 拖动阴影图像，定义为一个drawable
        private static Drawable shadow;

        // 定义myDragShadowBuilder的构造方法
        public DragShadowBuilder(View v) {

            // 保存传给myDragShadowBuilder的View参数
            super(v);

            // 创建一个可拖动的图像，用于填满系统给出的Canvas
//            开启mDragItemView绘图缓存
            v.setDrawingCacheEnabled(true);
            //获取mDragItemView在缓存中的Bitmap对象
            Bitmap cacheBitmap = v.getDrawingCache();
            Bitmap mDragBitmap = Bitmap.createBitmap(cacheBitmap, 0, 0, cacheBitmap.getWidth(), cacheBitmap.getHeight());
            //这一步很关键，释放绘图缓存，避免出现重复的镜像
            v.destroyDrawingCache();
            shadow = new BitmapDrawable(mDragBitmap);

        }

        // 定义一个回调方法，用于把拖动阴影的大小和触摸点位置返回给系统
        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            // 定义本地变量
            int width, height;

            // 把阴影的宽度设为原始View的一半
            width = (int)(getView().getWidth() * 1.2);

            // 把阴影的高度设为原始View的一半
            height = (int)(getView().getHeight() * 1.2);

            // 拖动阴影是一个ColorDrawable对象。
            // 下面把它设为与系统给出的Canvas一样大小。这样，拖动阴影将会填满整个Canvas。
            shadow.setBounds(0, 0, width, height);

            // 设置长宽值，通过size参数返回给系统。
            size.set(width, height);

            // 把触摸点的位置设为拖动阴影的中心
            touch.set(width / 2, height / 2);
        }

        // 定义回调方法，用于在Canvas上绘制拖动阴影，Canvas由系统根据onProvideShadowMetrics()传入的尺寸参数创建。
        @Override
        public void onDrawShadow(Canvas canvas) {

            // 在系统传入的Canvas上绘制ColorDrawable
            shadow.draw(canvas);
        }

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View view = getChildAt(0);
        if (view != null && !mVerticalSpacingSet) {
            int childHeight = view.getMeasuredHeight();
            int rowNum = GridController.PER_PAGE_NUMBER / mNumberColumns;
            if (rowNum == 1) {
                return;
            }
            int height = getMeasuredHeight();
            setVerticalSpacing((height - (rowNum * childHeight) - getPaddingTop() - getPaddingBottom()) / (rowNum - 1));
            mVerticalSpacingSet = true;
        }
    }


    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (GridViewAdapter) adapter;
    }

    @Override
    public void setNumColumns(int numColumns) {
        super.setNumColumns(numColumns);
        this.mNumberColumns = numColumns;
    }

    @Override
    public void setColumnWidth(int columnWidth) {
        super.setColumnWidth(columnWidth);
        this.mColumnWidth = columnWidth;
    }

    @Override
    public void setHorizontalSpacing(int horizontalSpacing) {
        super.setHorizontalSpacing(horizontalSpacing);
        this.mHorizontalSpacing = horizontalSpacing;
    }

    @Override
    public void setVerticalSpacing(int verticalSpacing) {
        super.setVerticalSpacing(verticalSpacing);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onTouchEvent action:" + ev.getAction());
        if(mGridController.isDraging()){
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            Log.d(TAG, "ev.getAction() == MotionEvent.ACTION_MOVE");
            return false;
        } else {
            Log.d(TAG, "} else {");
            return super.onTouchEvent(ev);
        }
    }

    private boolean pageChange = false;
    /**
     * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
     * @param rawX
     * @param moveY
     */
    public void onDragItem(int rawX, int moveY){

        if(!pageChange) {
            onSwapItem(rawX, moveY, false);
        }

        if(!pageChange){
            int duration = 400;
            if (rawX < 30) {
                pageChange = true;
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pageChange = false;
                    }
                }, 2 * duration);
                mGridController.prePage();
            } else if (rawX > GridController.SCREEN_WIDTH - 30) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pageChange = false;
                    }
                }, 2 * duration);
                mGridController.nextPage();
                pageChange = true;
            }
        }
    }

    private Rect mTouchFrame;
    @Override
    public int pointToPosition(int x, int y) {
        Rect frame = mTouchFrame;
        if (frame == null) {
            mTouchFrame = new Rect();
            frame = mTouchFrame;
        }

        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return i;
                }
            }
        }
        return INVALID_POSITION;
    }

    private int lastAbsOldPosition = -1;   //上一次交换的绝对旧位置
    private int lastAbsNewPosition = -1;   //上一次交换的绝对新位置
    private boolean specialCondition = false;
    private boolean delayShowView = false;
    /**
     * 交换item,并且控制item之间的显示与隐藏效果
     * @param moveX
     * @param moveY
     */
    private void onSwapItem(int moveX, int moveY, boolean isSpecialCondition) {

        final DragGridView curGridView;
        if (mGridController != null) {
            curGridView =  mGridController.getCurrentPageGridView();
        } else {
            curGridView = this;
        }
        if (curGridView == null) {
            Log.d(TAG, "current grid view is null, pls check it.");
            return;
        }
        //获取我们手指移动到的那个item的position
        int tempPosition = curGridView.pointToPosition(moveX, moveY);
        Log.d(TAG, "------------------onSwapItem---------------start");
        Log.d(TAG, "tempPosition:" + tempPosition + "  curGridView.mDragPosition =" + mGridController.getDragPosition(mPageIndex) + " curGridView:" + curGridView.hashCode());
        Log.d(TAG, "moveX:" + moveX + " moveY:" + moveY + " curGridView.screenID:" + curGridView.mPageIndex);
        //假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
        if (tempPosition + curGridView.mPageIndex * GridController.PER_PAGE_NUMBER > mGridController.getGridsCount() - 1) {
            return;
        }
        if (isSpecialCondition && tempPosition == AdapterView.INVALID_POSITION && curGridView.mPageIndex == mGridController.getPagesCount()) {
            specialCondition = true;
            tempPosition = mGridController.getGridsCount() - 1 - (curGridView.mPageIndex * GridController.PER_PAGE_NUMBER);
        }
        if (tempPosition != mGridController.getDragPosition(mPageIndex) && tempPosition != AdapterView.INVALID_POSITION && mAnimationEnd) {
            int newAbsPosition = tempPosition + curGridView.mPageIndex * GridController.PER_PAGE_NUMBER;
            if (lastAbsOldPosition != mGridController.getDragPosition() || lastAbsNewPosition != newAbsPosition) {
                lastAbsOldPosition = mGridController.getDragPosition();
                lastAbsNewPosition = newAbsPosition;
            } else if (!specialCondition) {
                Log.d(TAG, "in short time occur twice swap is unbelivable");
                return;
            }
            if (specialCondition) {
                specialCondition = false;
                delayShowView = true;
            }
            Log.d(TAG, "oldPosition:" + mGridController.getDragPosition() + " newPosition:" + newAbsPosition);
            mGridController.reorderItems(mGridController.getDragPosition(), mGridController.getDragPage(), newAbsPosition, curGridView.mPageIndex);
            ((GridViewAdapter)curGridView.getAdapter()).setHideItem(tempPosition);
            final int animTempPosition = tempPosition;
            final ViewTreeObserver observer = curGridView.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);
                    if (delayShowView && animTempPosition != 0) {
                        animateReorder(mGridController.getDragPosition(curGridView.mPageIndex), animTempPosition, curGridView, curGridView.getChildAt(animTempPosition));
                    } else {
                        animateReorder(mGridController.getDragPosition(curGridView.mPageIndex), animTempPosition, curGridView, null);
                    }
                    mGridController.setDragPosition(curGridView.mPageIndex, animTempPosition);
                    return true;
                }
            });
        }
        Log.d(TAG, "------------------onSwapItem---------------end");
    }

    /**
     * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
     */
    public void onStopDrag(int moveX, int moveY){
        if (moveY >=0 && mGridController.getCurrentPageIndex() == mGridController.getPagesCount()) {
            onSwapItem(moveX, moveY, true);
        }
        int stopPage = mGridController.getDragPosition() / GridController.PER_PAGE_NUMBER;
        DragGridView stopGridView = mGridController.getDragGridView(stopPage);
        Log.d(TAG, "$$$$$$$$$$$$$$$$$onStopDrag$$$$$$$$$$$$$$$$$$$start");
        Log.d(TAG, "dragPosition:" + mGridController.getDragPosition() + " stopPage:" + stopPage + " stopGridView:" + stopGridView + " hidePosition:" + mGridController.getHidePosition());
        if (stopGridView != null) {
            View view = stopGridView.getChildAt(mGridController.getDragPosition(stopPage) - stopGridView.getFirstVisiblePosition());
            Log.d(TAG, "firstposition:" + stopGridView.getFirstVisiblePosition() + " viewposition:" + mGridController.getDragPosition(stopPage));
            Log.d(TAG, "view:" + view + " visibility:" + view.getVisibility() + " stopPage:" + stopPage);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
                Log.d(TAG, "view.setVisibility(View.VISIBLE)");
            }
        }
        mGridController.setHidePosition(-1);
        mGridController.setDragingItem(null);
        mGridController.setDraging(false);
        Log.d(TAG, "$$$$$$$$$$$$$$$$$onStopDrag$$$$$$$$$$$$$$$$$$$end");
    }

    /**
     * 创建移动动画
     * @param view
     * @param startX
     * @param endX
     * @param startY
     * @param endY
     * @return
     */
    private AnimatorSet createTranslationAnimations(View view, float startX,
                                                    float endX, float startY, float endY) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",
                startX, endX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",
                startY, endY);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        return animSetXY;
    }


    /**
     * item的交换动画效果
     * @param oldPosition
     * @param newPosition
     */
    private void animateReorder(int oldPosition, final int newPosition, DragGridView gridView, final View delayView) {
        if (oldPosition < 0) {
            oldPosition = 0;
        } else if (oldPosition > GridController.PER_PAGE_NUMBER - 1) {
            oldPosition = GridController.PER_PAGE_NUMBER - 1;
        }
        if (newPosition == oldPosition) {
            return;
        }
        boolean isForward = newPosition > oldPosition;
        List<Animator> resultList = new LinkedList<Animator>();
        Log.d(TAG, "animateReorder oldPosition:" + oldPosition + " newPosition:" + newPosition);
        if (isForward) {
            for (int pos = oldPosition; pos < newPosition; pos++) {
                View view = gridView.getChildAt(pos - gridView.getFirstVisiblePosition());
                if (view == null) {
                    continue;
                }
                if ((pos + 1) % mNumberColumns == 0) {
                    resultList.add(createTranslationAnimations(view,
                            - view.getWidth() * (mNumberColumns - 1), 0,
                            view.getHeight(), 0));
                } else {
                    resultList.add(createTranslationAnimations(view,
                            view.getWidth(), 0, 0, 0));
                }
            }
        } else {
            for (int pos = oldPosition; pos > newPosition; pos--) {
                View view = gridView.getChildAt(pos - gridView.getFirstVisiblePosition());
                if ((pos + mNumberColumns) % mNumberColumns == 0) {
                    resultList.add(createTranslationAnimations(view,
                            view.getWidth() * (mNumberColumns - 1), 0,
                            -view.getHeight(), 0));
                } else {
                    resultList.add(createTranslationAnimations(view,
                            -view.getWidth(), 0, 0, 0));
                }
            }
        }
        if (delayShowView && delayView != null) {
            Log.d(TAG, "delayShowView && delayView != null");
            delayShowView = false;
            resultList.add(createTranslationAnimations(delayView, delayView.getWidth(), 0, 0, 0));
        }
        AnimatorSet resultSet = new AnimatorSet();
        resultSet.playTogether(resultList);
        resultSet.setDuration(250);
        resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
        resultSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "onAnimationStart");
                mAnimationEnd = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "onAnimationEnd");
                mAnimationEnd = true;
            }
        });
        resultSet.start();
    }

    public void updateGridItem(GridItem item) {
        View view = getChildAt(item.getPosition() - mAdapter.startPositionIndex);
        if (view != null) {
            mAdapter.getView(item.getPosition() - mAdapter.startPositionIndex, view, this).invalidate();
        }
    }
}
