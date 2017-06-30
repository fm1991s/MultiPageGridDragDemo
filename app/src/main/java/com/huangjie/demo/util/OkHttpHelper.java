package com.huangjie.demo.util;

import com.huangjie.demo.GApplication;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by huangjie on 2017/6/20.
 */

public class OkHttpHelper {

    private static OkHttpHelper mOkHttpHelper = null;
    private static OkHttpClient mHttpClient = null;
    private static final int CACHE_SIZE = 20 * 1024 * 1024;

    private OkHttpHelper() {
        File cacheDir = GApplication.getApplication().getExternalCacheDir();
        int cacheSize = CACHE_SIZE;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .cache(new Cache(cacheDir, cacheSize));
        mHttpClient = builder.build();
    }

    public static OkHttpHelper getInstance() {
        if (mOkHttpHelper == null) {
            synchronized (OkHttpHelper.class) {
                if (mOkHttpHelper == null) {
                    return new OkHttpHelper();
                }
            }
        }
        return mOkHttpHelper;
    }

    public void getRequestAsync(String url, final HttpResponseListener<ResponseBody> listener, boolean cache) {
        Request.Builder builder = new Request.Builder().url(url);
        if (!cache) {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        Call call = mHttpClient.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                listener.onResponse(response.body());
            }
        });
    }

    public ResponseBody getRequestSync(String url, boolean cache) {
        Request.Builder builder = new Request.Builder().url(url);
        if (!cache) {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        Call call = mHttpClient.newCall(builder.build());
        try {
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void postRequestAync(String url, final HttpResponseListener<ResponseBody> listener, final RequestBody requestBody, boolean cache) {
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        if (!cache) {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        Call call = mHttpClient.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                listener.onResponse(response.body());
            }
        });
    }

    public ResponseBody postRequestSync(String url, RequestBody requestBody, boolean cache) {
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        if (!cache) {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }
        Call call = mHttpClient.newCall(builder.build());
        try {
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public interface HttpResponseListener<T> {
        void onResponse(T t);
        void onError(String msg);
    }
}
