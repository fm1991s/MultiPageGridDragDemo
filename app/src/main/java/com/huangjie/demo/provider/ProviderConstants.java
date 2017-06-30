package com.huangjie.demo.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by huangjie on 2017/6/12.
 */

public class ProviderConstants {

    public static final String AUTHORITY = "com.huangjie.demo.provider.gridprovider";

    public static final String CONTENTY_TYPE = "vnd.android.cursor.dir/vnd.gridprovider.demo";

    public static final String CONTENT_TYPE_ID = "vnd.android.cursor.item/vnd.gridprovider.demo";

    protected static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class GridColumns implements BaseColumns {

        public static final int CODE_GRID_ITEM = 1;

        public static final String TABLE_NAME = "grid_item";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String COLUMN_ICON_PATH = "grid_icon_path";
        public static final String COLUMN_TITLE = "grid_title";
        public static final String COLUMN_URL = "grid_url";
        public static final String COLUMN_POSITION = "grid_position";
        public static final String COLUMN_ICON_BITMAP = "grid_icon_bitmap";
    }
}
