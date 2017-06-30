package com.huangjie.demo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.huangjie.demo.data.GridItem;
import com.huangjie.demo.util.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

import static com.huangjie.demo.provider.ProviderConstants.GridColumns.COLUMN_ICON_BITMAP;
import static com.huangjie.demo.provider.ProviderConstants.GridColumns.COLUMN_ICON_PATH;
import static com.huangjie.demo.provider.ProviderConstants.GridColumns.COLUMN_POSITION;
import static com.huangjie.demo.provider.ProviderConstants.GridColumns.COLUMN_TITLE;
import static com.huangjie.demo.provider.ProviderConstants.GridColumns.COLUMN_URL;
import static com.huangjie.demo.provider.ProviderConstants.GridColumns.TABLE_NAME;

/**
 * Created by huangjie on 2017/5/30.
 */

public class GridDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "grid";
    private static final int DB_VERSION = 1;
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_INTEGER = "integer";
    public static final String TYPE_TIMESTAMP = "timestamp";
    public static final String TYPE_BLOB = "blob";
    private static final String CREATE_TABLE = "create table if not exists";
    private static final String TYPE_ID = "INTEGER PRIMARY KEY AUTOINCREMENT";



    private static final String CREATE_TABLE_SQL = String.format("%s %s(%s %s, %s %s, %s %s, %s %s, %s %s, %s %s);",
            CREATE_TABLE, TABLE_NAME, BaseColumns._ID, TYPE_ID, COLUMN_ICON_PATH, TYPE_TEXT, COLUMN_TITLE, TYPE_TEXT,
            COLUMN_URL, TYPE_TEXT, COLUMN_POSITION, TYPE_INTEGER, COLUMN_ICON_BITMAP, TYPE_BLOB);

    public GridDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION, errorHandler);
    }

    static DatabaseErrorHandler errorHandler = new DatabaseErrorHandler() {
        @Override
        public void onCorruption(SQLiteDatabase dbObj) {

        }
    };


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }





}
