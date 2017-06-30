package com.huangjie.demo.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.nio.ByteBuffer;

/**
 * Created by huangjie on 2017/6/4.
 */

public class BitmapUtils {

    public static Bitmap bytesToBitmap(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        if (bitmap == null || bitmap.getByteCount() == 0) {
            return null;
        }
        int count = bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(count);
        return buffer.array();
    }

    public static byte[] bitmap2Bytes(Bitmap bitmap) {
        if (bitmap == null || bitmap.getByteCount() == 0) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    public static Bitmap drawableToBitmap(Drawable drawable, Bitmap.Config config) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config cfg = config == null ? Bitmap.Config.RGB_565 : config;
        Bitmap bitmap = Bitmap.createBitmap(width, height, cfg);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        if (bitmap == null) {
            return null;
        }
        if (reqHeight <= 0|| reqWidth <= 0) {
            return null;
        }
        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();
        float scaleWidth = (float) reqWidth / (float)oldWidth;
        float scaleHeight = (float)reqHeight / (float) oldHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight, matrix, true);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if (options == null) {
            return 1;
        }
        int inSampleSize = 1;
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;

        if ((outHeight > reqHeight || outWidth > reqWidth) && outHeight > 0 && outWidth > 0) {
            int halfWidth = outWidth / 2;
            int halfHeight = outHeight / 2;
            while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeBitmapFromResource(Resources rs, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(rs, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(rs, resId, options);
    }

    public static Bitmap decodeBitmapFromFd(FileDescriptor fd, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    public static Bitmap decodeBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }
}
