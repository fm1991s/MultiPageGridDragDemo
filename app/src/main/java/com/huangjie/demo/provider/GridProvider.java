package com.huangjie.demo.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.huangjie.demo.db.GridDbHelper;

/**
 * Created by huangjie on 2017/6/12.
 */

public class GridProvider extends ContentProvider {

    private GridDbHelper mDbHelper;
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String TAG = GridProvider.class.getSimpleName();

    static {
        mUriMatcher.addURI(ProviderConstants.AUTHORITY, ProviderConstants.GridColumns.TABLE_NAME, ProviderConstants.GridColumns.CODE_GRID_ITEM);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new GridDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String tableName = null;
        switch (mUriMatcher.match(uri)) {
            case ProviderConstants.GridColumns.CODE_GRID_ITEM:
                tableName = ProviderConstants.GridColumns.TABLE_NAME;
                break;
            default:
                Log.d(TAG, "no match uri");
                break;
        }

        if (tableName == null) {
            return null;
        }

        try {
            qb.setTables(tableName);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String tableName = null;
        switch (mUriMatcher.match(uri)) {
            case ProviderConstants.GridColumns.CODE_GRID_ITEM:
                tableName = ProviderConstants.GridColumns.TABLE_NAME;
                break;
            default:
                Log.d(TAG, "no match uri");
                break;

        }
        if (tableName == null) {
            return null;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long insertId = db.insert(tableName, BaseColumns._ID, values);
        if (insertId < 0) {
            return null;
        }
        Uri notiUri = ContentUris.withAppendedId(uri, insertId);
        getContext().getContentResolver().notifyChange(uri, null);

        return notiUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = null;
        switch (mUriMatcher.match(uri)) {
            case ProviderConstants.GridColumns.CODE_GRID_ITEM:
                tableName = ProviderConstants.GridColumns.TABLE_NAME;
                break;
            default:
                Log.d(TAG, "no match uri");
                break;

        }
        if (tableName == null) {
            return 0;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = null;
        switch (mUriMatcher.match(uri)) {
            case ProviderConstants.GridColumns.CODE_GRID_ITEM:
                tableName = ProviderConstants.GridColumns.TABLE_NAME;
                break;
            default:
                Log.d(TAG, "no match uri");
                break;

        }
        if (tableName == null) {
            return 0;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

}
