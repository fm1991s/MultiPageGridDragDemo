package com.huangjie.demo.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;


/**
 * Created by huangjie on 2017/5/30.
 */

public class ThreadUtils {

    private static Handler mUiHandler = new Handler(Looper.getMainLooper());
    private static Handler mIoHandler;

    private static HandlerThread mIoHandlerThread;

    static {
        mIoHandlerThread = new HandlerThread("IoTask", Process.THREAD_PRIORITY_BACKGROUND);
        mIoHandlerThread.start();
        mIoHandler = new Handler(mIoHandlerThread.getLooper());
    }

    public static synchronized void postOnUiThread(Runnable runnable) {
        if (mUiHandler == null) {
            mUiHandler = new Handler(Looper.getMainLooper());
        }
        mUiHandler.post(runnable);
    }

    public static synchronized void postOnUiThreadDelay(Runnable runnable, long delayMillis) {
        if (mUiHandler == null) {
            mUiHandler = new Handler(Looper.getMainLooper());
        }
        mUiHandler.postDelayed(runnable, delayMillis);
    }


    public static synchronized void runOnIoThread(Runnable runnable) {
        if (mIoHandler == null) {
            if (mIoHandlerThread == null || !mIoHandlerThread.isAlive()) {
                mIoHandlerThread = new HandlerThread("IoTask", Process.THREAD_PRIORITY_BACKGROUND);
                mIoHandlerThread.start();
            }
            mIoHandler = new Handler(mIoHandlerThread.getLooper());
        }
        mIoHandler.post(runnable);
    }

    public static synchronized void runOnIoThreadDelay(Runnable runnable, long delayMillis) {
        if (mIoHandler == null) {
            if (mIoHandlerThread == null || !mIoHandlerThread.isAlive()) {
                mIoHandlerThread = new HandlerThread("IoTask", Process.THREAD_PRIORITY_BACKGROUND);
                mIoHandlerThread.start();
            }
            mIoHandler = new Handler(mIoHandlerThread.getLooper());
        }
        mIoHandler.postDelayed(runnable, delayMillis);
    }

    public static boolean runningOnUiThread() {
        return mUiHandler.getLooper() == Looper.myLooper();
    }
}
