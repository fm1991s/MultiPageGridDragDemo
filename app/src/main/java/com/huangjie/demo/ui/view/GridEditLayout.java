package com.huangjie.demo.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.huangjie.demo.controller.GridController;
import com.huangjie.demo.ui.R;
import com.huangjie.demo.util.DimenUtils;

/**
 * Created by huangjie on 2017/5/30.
 */

public class GridEditLayout extends LinearLayout {
    private static final String TAG = "GridMaskLayout";

    private GridController mGridController;
    private int mGridSectionTopMargin;
    private int mGridContainerBottom;
    private Context mContext;
    private Button bottomButton;
    private LinearLayout topLinearLayout;
    private RelativeLayout bottomRelativeLayout;
    private int maskHeight;
    private int mStatusHeight = 0;
    private int mGridContainerHeight;
    private int bottomButtonHeight;
    public GridEditLayout(Context context) {
        super(context);
    }

    public GridEditLayout(Context context, GridController controller) {
        this(context);
        this.mContext = context;
        this.mGridContainerHeight = context.getResources().getDimensionPixelSize(R.dimen.grid_section_height);
        this.mGridSectionTopMargin = context.getResources().getDimensionPixelSize(R.dimen.grid_section_top_margin);
        this.mGridContainerBottom = mGridSectionTopMargin + mGridContainerHeight;
        this.mGridController = controller;
        this.bottomButtonHeight = getResources().getDimensionPixelSize(R.dimen.grid_bottom_button_height);
        setOrientation(VERTICAL);
        setBackgroundColor(context.getResources().getColor(R.color.transparent));
        mStatusHeight = DimenUtils.getStatusBarHeight();
        maskHeight = DimenUtils.getScreenHeightPixels() - mStatusHeight;
        initLayout();
        setOnDragListener(new DragEventListener());
    }

    private void initLayout() {
        topLinearLayout = new LinearLayout(mContext);
        topLinearLayout.setOrientation(VERTICAL);
        topLinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.blue_2f9bff));
        LinearLayout.LayoutParams topLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, mGridSectionTopMargin);
        addView(topLinearLayout, topLayoutParams);


        bottomRelativeLayout =  new RelativeLayout(mContext);

        bottomRelativeLayout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        LinearLayout.LayoutParams bottomLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, maskHeight - mGridContainerBottom);
        bottomLayoutParams.topMargin = mGridContainerBottom - mGridSectionTopMargin;
        addView(bottomRelativeLayout, bottomLayoutParams);

        bottomButton = new Button(mContext);
        bottomButton.setGravity(Gravity.CENTER);
        bottomButton.setText(mContext.getResources().getString(R.string.edit_done));

        bottomButton.setTextColor(mContext.getResources().getColorStateList(R.color.grid_edit_layout_bottom_button_selector));
        bottomButton.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.grid_edit_mode_text_size));

        bottomButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.edit_mode_background));
        bottomButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGridController.isDraging()) {
                    mGridController.exitEditMode();
                }
            }
        });
        RelativeLayout.LayoutParams bottomButtonLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, bottomButtonHeight);
        bottomButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bottomRelativeLayout.addView(bottomButton, bottomButtonLayoutParams);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int moveY = (int)event.getY();
        Log.d(TAG, "onTouchEvent action:" + event.getAction() + " rawY:" + event.getRawY() + " moveY:" + moveY);
        if (mGridController.isEditMode() && !mGridController.isDraging() && (moveY < maskHeight)) {
            mGridController.handleOnTouch(event, mGridSectionTopMargin);
            return true;
        }
        return super.onTouchEvent(event);
    }

    class DragEventListener implements View.OnDragListener {

        // 这是系统向侦听器发送拖动事件时将会调用的方法
        private DragGridView mDragGridView = null;
        private boolean invalid = false;
        int startPage = 0;
        int startPosition, endPosition;
        public boolean onDrag(View v, DragEvent event) {

            final int action = event.getAction();
            Log.d(TAG, "###############OnDragListener##################start");
            Log.d(TAG, "getX:" + event.getX() + " getY:" + event.getY() + " action:" + action);
            if (v != null) {
                Log.d(TAG, "v:" + v.hashCode());
            }
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d(TAG, "action: DragEvent.ACTION_DRAG_STARTED");
                    mGridController.eventTransferSuccess = true;
                    mGridController.setDraging(true); //设置可以拖拽
                    startPosition = mGridController.getDragPosition();
                    startPage = startPosition / GridController.PER_PAGE_NUMBER;
                    Log.d(TAG, "startPage:" + startPage + " dragPosition:" + mGridController.getDragPosition());
                    mDragGridView = mGridController.getDragGridView(startPage);
                    if (mDragGridView == null) {
                        invalid = true;
                    } else {
                        invalid = false;
                    }
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    Log.d(TAG, "action: DragEvent.ACTION_DRAG_LOCATION");
                    Log.d(TAG, "mGridSectionTopMargin:" + mGridSectionTopMargin + " gridviewY:" + (int) (event.getY() - mGridSectionTopMargin));
                    if (!invalid) {
                        mDragGridView.onDragItem((int) event.getX(), (int) (event.getY() - mGridSectionTopMargin));
                    }
                    break;
                case DragEvent.ACTION_DROP:
                    Log.d(TAG, "action: DragEvent.ACTION_DROP");
                    if (!invalid) {
                        int stopPage = mGridController.getDragPosition() / GridController.PER_PAGE_NUMBER;
                        endPosition = mGridController.getDragPosition();
                        Log.d(TAG, "stopPage:" + stopPage + " dragPosition:" + mGridController.getDragPosition());
                        mDragGridView.onStopDrag((int) event.getX(), (int) (event.getY() - mGridSectionTopMargin));
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    Log.d(TAG, "action: DragEvent.ACTION_DRAG_ENDED");
                    if (!invalid) {
                        Log.d(TAG, "invalid is false");
                        if (mGridController.isDraging()) {
                            Log.d(TAG, "isGridDraging");
                            mDragGridView.onStopDrag(-1, -1);
                        }
                    }
                    break;
                default:
                    Log.d(TAG, "action: default");
                    break;
            }
            Log.d(TAG, "###############OnDragListener##################end");
            return true;
        }
    }
}
