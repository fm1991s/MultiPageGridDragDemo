package com.huangjie.demo.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.huangjie.demo.GApplication;
import com.huangjie.demo.data.GridItem;
import com.huangjie.demo.provider.ProviderConstants;
import com.huangjie.demo.util.BitmapUtils;
import com.huangjie.demo.util.IOUtils;
import com.huangjie.demo.util.NetworkUtils;
import com.huangjie.demo.util.UrlUtils;
import com.huangjie.demo.util.ThreadUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.huangjie.demo.provider.ProviderConstants.GridColumns.COLUMN_ICON_BITMAP;
import static com.huangjie.demo.provider.ProviderConstants.GridColumns.COLUMN_ICON_PATH;
import static com.huangjie.demo.provider.ProviderConstants.GridColumns.COLUMN_POSITION;
import static com.huangjie.demo.provider.ProviderConstants.GridColumns.COLUMN_TITLE;
import static com.huangjie.demo.provider.ProviderConstants.GridColumns.COLUMN_URL;

/**
 * Created by huangjie on 2017/6/4.
 */

public class GridManager {

    private static GridManager sInstance;
    private Context mContext;
    private static List<GridItem> mGridList;
    private static final String TAG = GridManager.class.getSimpleName();


    private GridManager() {
        mContext = GApplication.getApplication();
    }

    public synchronized static GridManager getInstance() {
        if (sInstance == null) {
            sInstance = new GridManager();
        }
        return sInstance;
    }

    public void loadGrid(LoadListener loadListener) {
        if (mGridList == null || mGridList.size() == 0) {
            loadGridFromDb(loadListener);
        } else {
            if (loadListener != null) {
                loadListener.onLoadFinished(mGridList);
            }
        }
    }

    public List<GridItem> getGridList() {
        return mGridList;
    }

    private void loadGridFromDb(final LoadListener loadListener) {
        ThreadUtils.runOnIoThread(new Runnable() {
            @Override
            public void run() {
                if ((mGridList = queryAll()) == null) {
                    loadGridFromAssset(loadListener);
                } else {
                    if (loadListener != null) {
                        loadListener.onLoadFinished(mGridList);
                    }
                }
            }
        });
    }

    public synchronized List<GridItem> queryAll() {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(ProviderConstants.GridColumns.CONTENT_URI, null, null, null, COLUMN_POSITION);
            List<GridItem> gridItems = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    gridItems.add(cursorToGridItem(cursor));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public synchronized void queryAndReturnItem(final int position, final QueryResponseListener listener) {
        if (ThreadUtils.runningOnUiThread()) {
            ThreadUtils.runOnIoThread(new Runnable() {
                @Override
                public void run() {
                    queryAndReturnItem(position, listener);
                }
            });
        }
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    ProviderConstants.GridColumns.CONTENT_URI,
                    null,
                    ProviderConstants.GridColumns.COLUMN_POSITION + " = ?",
                    new String[]{String.valueOf(position)},
                    null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                listener.onResult(cursorToGridItem(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onError(e.getMessage());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public synchronized void queryByUrl(final String url, final QueryResponseListener listener) {
        if (ThreadUtils.runningOnUiThread()) {
            ThreadUtils.runOnIoThread(new Runnable() {
                @Override
                public void run() {
                    queryByUrl(url, listener);
                }
            });
        }
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    ProviderConstants.GridColumns.CONTENT_URI,
                    null,
                    ProviderConstants.GridColumns.COLUMN_URL + " = ?",
                    new String[]{String.valueOf(url)},
                    null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                listener.onResult(cursorToGridItem(cursor));
            } else {
                listener.onResult(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onError(e.getMessage());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public synchronized void deleteGridItem(final GridItem item, boolean syncToDb, boolean syncToList) {
        if (item == null) {
            return;
        }
        if (syncToDb) {
            ThreadUtils.runOnIoThread(new Runnable() {
                @Override
                public void run() {
                    mContext.getContentResolver().delete(ProviderConstants.GridColumns.CONTENT_URI, "grid_position = ?", new String[]{item.getPosition() + ""});
                }
            });
        }
        if (syncToList) {
            removeItemFromList(item);
        }
    }

    public synchronized void updateGridItem(final GridItem item, final int newPosition, boolean syncToDb, boolean syncToList) {
        if (item == null) {
            return;
        }
        if (syncToDb) {
            ThreadUtils.runOnIoThread(new Runnable() {
                @Override
                public void run() {
                    ContentValues cv = new ContentValues();
                    cv.put(ProviderConstants.GridColumns.COLUMN_POSITION, newPosition);
                    int rowId = mContext.getContentResolver().update(ProviderConstants.GridColumns.CONTENT_URI, cv, COLUMN_POSITION + " = ?", new String[]{String.valueOf(item.getPosition())});
                    Log.d(TAG, "update title:" + item.getTitle() + " rowId:" + rowId + " newPostion:" + newPosition);
                }
            });
        }
        if (syncToList) {
            item.setPosition(newPosition);
            item.setID(newPosition);
        }
    }

    public synchronized void insertGridItem(final GridItem item, boolean syncToDb, boolean syncToList) {
        if (item == null) {
            return;
        }
        if (syncToDb) {
            ThreadUtils.runOnIoThread(new Runnable() {
                @Override
                public void run() {
                    ContentValues cv = gridItemToContentValues(item);
                    Uri uri = mContext.getContentResolver().insert(ProviderConstants.GridColumns.CONTENT_URI, cv);
                    String rowId = uri.getPathSegments().get(1);
                    Log.d(TAG, "insertGridItem item:" + item.getPosition() + " rowId:" + rowId);
                    mContext.getContentResolver().notifyChange(ProviderConstants.GridColumns.CONTENT_URI, null);

                }
            });
        }
        if (syncToList) {
            putItemIntoList(item);
        }
    }

    private static final Object lockObj = new Object();

    public void putItemIntoList(GridItem item) {
        synchronized (lockObj) {
            mGridList.add(item);
        }
    }

    public void removeItemFromList(GridItem item) {
        synchronized (lockObj) {
            mGridList.remove(item);
        }
    }

    private ContentValues gridItemToContentValues(GridItem item) {
        ContentValues cv = new ContentValues();
        cv.put(ProviderConstants.GridColumns.COLUMN_URL, item.getUrl());
        cv.put(ProviderConstants.GridColumns.COLUMN_TITLE, item.getTitle());
        cv.put(ProviderConstants.GridColumns.COLUMN_ICON_PATH, item.getIconPath());
        cv.put(ProviderConstants.GridColumns.COLUMN_POSITION, item.getPosition());
        cv.put(ProviderConstants.GridColumns.COLUMN_ICON_BITMAP, BitmapUtils.bitmapToBytes(item.getBitmap()));
        return cv;
    }

    public GridItem cursorToGridItem(Cursor cursor) {
        GridItem item = new GridItem();
        item.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_POSITION)));
        item.setIconPath(cursor.getString(cursor.getColumnIndex(COLUMN_ICON_PATH)));
        item.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
        item.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
        item.setPosition(cursor.getInt(cursor.getColumnIndex(COLUMN_POSITION)));
        item.setBitmap(BitmapUtils.bytesToBitmap(cursor.getBlob(cursor.getColumnIndex(COLUMN_ICON_BITMAP))));
        return item;
    }

    private void insertListToDb(final List<GridItem> list) {
        if (ThreadUtils.runningOnUiThread()) {
            ThreadUtils.runOnIoThread(new Runnable() {
                @Override
                public void run() {
                    insertListToDb(list);
                }
            });
        }
        for (GridItem item : list) {
            insertGridItem(item, true, false);
        }
    }


    private void loadGridFromAssset(LoadListener loadListener) {
        try {
            InputStream in = mContext.getAssets().open("grid/grid.json");
            byte[] data = IOUtils.readInputStream(in);
            String jsonData = new String(data, "utf-8");
            mGridList = parseJsonData(jsonData);
            loadGridIcons(mGridList);
            insertListToDb(mGridList);
            if (loadListener != null) {
                loadListener.onLoadFinished(mGridList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGridIcons(List<GridItem> gridList) {
        for (GridItem item : gridList) {
            if (!TextUtils.isEmpty(item.getIconPath())) {
                item.setBitmap(loadIcon(item.getIconPath()));
            }
        }
    }

    private Bitmap loadIcon(String iconUrl) {
        Bitmap bitmap = null;
        if (iconUrl.startsWith(UrlUtils.SCHEME_HTTP)) {
            bitmap = BitmapUtils.bytesToBitmap(NetworkUtils.getDataSyncByOkhttp(iconUrl));
        } else if (iconUrl.startsWith(UrlUtils.SCHEME_ASSET)) {
            bitmap = loadIconFromLocal(iconUrl);
        }
        return bitmap;
    }

    private Bitmap loadIconFromLocal(String iconUrl) {
        try {
            InputStream in = mContext.getAssets().open(iconUrl.substring(9));
            byte[] data = IOUtils.readInputStream(in);
            return BitmapUtils.bytesToBitmap(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<GridItem> parseJsonData(String jsonData) {
        try {
            List<GridItem> resultItems = new ArrayList<GridItem>();
            JSONObject obj = new JSONObject(jsonData);
            JSONArray dataArray = obj.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject itemObj = dataArray.getJSONObject(i);
                GridItem item = new GridItem();
                item.setID(itemObj.optInt("id"));
                item.setPosition(itemObj.optInt("id"));
                item.setIconPath(itemObj.optString("iconpath", ""));
                item.setTitle(itemObj.optString("title", ""));
                item.setUrl(itemObj.optString("url", ""));
                item.setType(toGridType(itemObj.optString("grid_type")));
                resultItems.add(item);
            }
            Collections.sort(resultItems, new Comparator<GridItem>() {
                @Override
                public int compare(GridItem o1, GridItem o2) {
                    return o1.compareTo(o2);
                }
            });
            return resultItems;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface QueryResponseListener {
        void onResult(Object obj);

        void onError(String msg);
    }

    public interface LoadListener {
        void onLoadFinished(List<GridItem> itemList);
        void onLoadFailed(String failReason);
    }

    private GridItem.GRID_TYPE toGridType(String type) {
        if (type.equalsIgnoreCase("empty")) {
            return GridItem.GRID_TYPE.TYPE_EMPTY;
        } else {
            return GridItem.GRID_TYPE.TYPE_NORMAL;
        }
    }


}
