package com.huangjie.demo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.huangjie.demo.ui.R;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;

/**
 * Created by huangjie on 2017/6/20.
 */

public class ImageLoader {


    private static final String TAG = ImageLoader.class.getSimpleName();

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;

    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2 + 1;

    private static final int KEEP_ALIVE = 10;

    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024;

    private static final int TAG_RES_ID = R.id.img_tag;

    private static final int DISK_CACHE_INDEX = 0;

    private static final int MESSAGE_LOAD_FINISH = 1;

    private static final ThreadFactory mThreadFactory = new ThreadFactory() {

        private final AtomicInteger mCount = new AtomicInteger(1);
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "ImageLoader #" + mCount.getAndIncrement());
        }
    };

    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAX_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), mThreadFactory);


    private Context mContext;
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;
    private boolean mDiskCacheAvailable = false;

    private ImageLoader(Context context) {
        mContext = context;
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
        File diskCache = getDiskCacheDir(context, "bitmap");
        if (!diskCache.exists()) {
            diskCache.mkdirs();
        }
        if (getUsableSpace(diskCache) > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCache, 1, 1, DISK_CACHE_SIZE);
                mDiskCacheAvailable = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ImageLoader build(Context context) {
        return new ImageLoader(context);
    }

    public File getDiskCacheDir(Context context, String dirName) {
        boolean externalMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        String cachePath;
        if (externalMounted) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + dirName);
    }

    public long getUsableSpace(File path) {
        return path.getUsableSpace();
    }

    public void addBitmapToMemoeryCache(String key, Bitmap bm) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bm);
        }
    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    private String hashUrlToKey(String url) {
        if (!TextUtils.isEmpty(url)) {
            return "";
        }
        String key;
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(url.getBytes());
            key = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            key = String.valueOf(url.hashCode());
        }
        return key;

    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    private Bitmap loadBitmapFromMemoryCache(String url) {
        String key = hashUrlToKey(url);
        return getBitmapFromMemoryCache(key);
    }

    private boolean downloadUrlToStream(String url, OutputStream outputStream) {
        boolean result = false;
        ResponseBody responseBody = OkHttpHelper.getInstance().getRequestSync(url, true);
        if (responseBody != null) {
            BufferedInputStream is = new BufferedInputStream(responseBody.byteStream());
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                while ((len = is.read(buffer, 0, 1024)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                is.close();
                outputStream.close();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("loadBitmapFromHttp cannot run in main thread");
        }

        if (mDiskLruCache == null) {
            return null;
        }

        String key = hashUrlToKey(url);
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
                if (downloadUrlToStream(url, outputStream)) {
                    editor.commit();
                } else {
                    editor.abort();
                }
                mDiskLruCache.flush();
            }
            return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("loadBitmapFromDiskCache cannot run in main thread");
        }
        if (mDiskLruCache == null) {
            return null;
        }

        String key = hashUrlToKey(url);
        Bitmap bitmap = null;

        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                FileInputStream inputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                bitmap = BitmapUtils.decodeBitmapFromFd(inputStream.getFD(), reqWidth, reqHeight);
                if (bitmap != null) {
                    addBitmapToMemoeryCache(key, bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    private Bitmap loadBitmapFromUrl(String url) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("loadBitmapFromUrl cannot run in main thread");
        }
        Bitmap bitmap = null;
        ResponseBody responseBody = OkHttpHelper.getInstance().getRequestSync(url, true);
        if (responseBody != null) {
            bitmap = BitmapFactory.decodeStream(responseBody.byteStream());
        }
        return bitmap;
    }


    public Bitmap loadBitmap(String url, int reqWidth, int reqHeight) {
        if (!TextUtils.isEmpty(url)) {
            return null;
        }
        Bitmap bitmap;
        bitmap = loadBitmapFromMemoryCache(url);
        if (bitmap != null) {
            return bitmap;
        }
        bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
        if (bitmap != null) {
            return bitmap;
        }
        bitmap = loadBitmapFromHttp(url, reqWidth, reqHeight);
        if (bitmap != null) {
            return bitmap;
        }

        return loadBitmapFromUrl(url);

    }

    public void loadBitmapIntoImageView(String url, ImageView imageView) {
        loadBitmapIntoImageView(url, imageView, 0, 0);
    }

    public void loadBitmapIntoImageView(final String url, final ImageView imageView, final int reqWidth, final int reqHeight) {
        imageView.setTag(TAG_RES_ID, url);
        Bitmap bitmap = loadBitmapFromMemoryCache(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bm = loadBitmap(url, reqWidth, reqHeight);
                if (bm != null) {
                    LoadResult loadResult = new LoadResult(imageView, url, bm);
                    Message msg = mMainHandler.obtainMessage(MESSAGE_LOAD_FINISH, loadResult);
                    msg.sendToTarget();
                }
            }
        });
    }

    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_LOAD_FINISH) {
                LoadResult loadResult = (LoadResult) msg.obj;
                if (loadResult != null) {
                    ImageView imageView = loadResult.mImageView;
                    String url = loadResult.mUrl;
                    Bitmap bitmap = loadResult.mBitmap;
                    if (imageView.getTag().equals(url)) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        Log.i(TAG, "imageview tag is not equal url");
                    }
                }
            }
        }
    };

    class LoadResult {
        public ImageView mImageView;
        public String mUrl;
        public Bitmap mBitmap;

        public LoadResult(ImageView imageView, String url, Bitmap bitmap) {
            this.mImageView = imageView;
            this.mUrl = url;
            this.mBitmap = bitmap;
        }
    }

}
