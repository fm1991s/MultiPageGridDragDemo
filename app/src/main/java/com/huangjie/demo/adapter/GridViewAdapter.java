package com.huangjie.demo.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huangjie.demo.controller.GridController;
import com.huangjie.demo.data.GridItem;
import com.huangjie.demo.ui.R;
import com.huangjie.demo.ui.view.DragGridView;
import com.huangjie.demo.ui.view.ShapeImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangjie on 2017/6/18.
 */

public class GridViewAdapter extends BaseAdapter {

    private static final String TAG = "GridViewAdapter";
    private int mCurrentPage;              //当前adapter管理的页，从0开始
    public int startPositionIndex;      //当前adapter管理的起始位置item的索引
    public int endPositionIndex;        //当前adapter管理的最后一位item的索引
    private List<GridItem> mItemList;
    private Context mContext;
    private LayoutInflater mInflater;
    private int mHidePosition = -1;
    private GridController mGridController;
    private DragGridView mCurrentGridView;
    public GridViewAdapter(Context context, int pageIndex, GridController gridController, List<GridItem> itemList, DragGridView currentGridView) {
        this.mContext = context;
        this.mItemList = itemList;
        this.mCurrentPage = pageIndex;
        this.mGridController = gridController;
        this.mCurrentGridView = currentGridView;
        startPositionIndex = GridController.PER_PAGE_NUMBER * pageIndex;
        this.endPositionIndex = (itemList.size() - 1 >= startPositionIndex + GridController.PER_PAGE_NUMBER ?
                startPositionIndex + GridController.PER_PAGE_NUMBER - 1 : itemList.size() - 1);
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return endPositionIndex - startPositionIndex + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position < 0 || position > GridController.PER_PAGE_NUMBER) {
            return null;
        }
        return mItemList.get(startPositionIndex + position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        GridItem item = (GridItem)getItem(position);
        if (item == null) {
            return null;
        }
        //因为某些情况下空白宫格会复用其他宫格的布局导致显示了tips，所以这时候重新创建一个新的吧。
        if (item.getType() == GridItem.GRID_TYPE.TYPE_EMPTY) {
            convertView = null;
        }
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.grid_item, null);
            holder.mGridTitle = (TextView) convertView.findViewById(R.id.grid_item_title);
            holder.mGridImage = (ShapeImageView) convertView.findViewById(R.id.grid_item_img);
            holder.mDelBtn = (ImageView) convertView.findViewById(R.id.grid_item_del_btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        initGridValue(item, holder);
        if (mGridController.isEditMode() && item.getType() == GridItem.GRID_TYPE.TYPE_EMPTY) {
            convertView.setVisibility(View.INVISIBLE);
        } else if (position != (mGridController.getHidePosition() - startPositionIndex) && convertView.getVisibility() != View.VISIBLE){
            convertView.setVisibility(View.VISIBLE);
        }
        if(position == mGridController.getHidePosition() - startPositionIndex){
            convertView.setVisibility(View.INVISIBLE);
        }
        final View view = convertView;
        view.post(new Runnable() {
            @Override
            public void run() {
                Rect rc = new Rect();
                holder.mDelBtn.getHitRect(rc); //如果直接在oncreate函数中执行本函数，会获取rect失败，因为此时UI界面尚未开始绘制，无法获得正确的坐标
                rc.bottom += 45;
                rc.top -= 45;
                rc.left -= 45;
                rc.right += 45;

                view.setTouchDelegate(new TouchDelegate(rc, holder.mDelBtn));
            }
        });
        return convertView;

    }

    private void initGridValue(final GridItem item, final ViewHolder viewHolder) {
        viewHolder.mGridTitle.setText(item.getTitle());
        if (item.getBitmap() != null) {
            viewHolder.mGridImage.setImageBitmap(item.getBitmap());
        } else {
            viewHolder.mGridImage.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.ic_launcher));
        }
        if (mGridController.isEditMode()) {
            if (item.getType() == GridItem.GRID_TYPE.TYPE_NORMAL) {
                if (viewHolder.mDelBtn.getVisibility() != View.VISIBLE) {
                    viewHolder.mDelBtn.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (viewHolder.mDelBtn.getVisibility() != View.GONE) {
                viewHolder.mDelBtn.setVisibility(View.GONE);
            }
        }
        viewHolder.mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGridController.handleDelGrid(item, mCurrentPage);
            }
        });
    }


    public void notifyDataChanged(List<GridItem> newDataSet) {
        mItemList = newDataSet;
        endPositionIndex = (mItemList.size() - 1 >= startPositionIndex + GridController.PER_PAGE_NUMBER ?
                startPositionIndex + GridController.PER_PAGE_NUMBER - 1 : mItemList.size() - 1);
        notifyDataSetChanged();
    }

    public void setHideItem(int hidePosition) {
        mGridController.setHidePosition(hidePosition + startPositionIndex);
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView mGridTitle;
        ShapeImageView mGridImage;
        ImageView mDelBtn;
    }
}
