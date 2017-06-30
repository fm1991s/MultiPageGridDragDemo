package com.huangjie.demo.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by huangjie on 2017/6/7.
 */

public class NetworkUtils {

    public static final String METHOD_TYPE_GET = "GET";
    public static final String METHOD_TYPE_POST = "POST";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    public static void getDataAsync(String url, onResponseListener responseListener) {

    }

    public static byte[] getDataSyncByUrlConnection(String urlString) {
        byte[] result = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(METHOD_TYPE_GET);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            InputStream in = urlConnection.getInputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            while ((len = bufferedInputStream.read(buffer, 0, 1024)) != -1) {
                os.write(buffer, 0, len);
            }
            result = os.toByteArray();
            bufferedInputStream.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    public static byte[] getDataSyncByOkhttp(String url) {
        byte[] result = null;
        ResponseBody responseBody = OkHttpHelper.getInstance().getRequestSync(url, true);
        if (responseBody != null) {
            InputStream inputStream = responseBody.byteStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                int readChar;
                while ((readChar = inputStream.read()) != -1) {
                    os.write(readChar);
                }
                result = os.toByteArray();
                inputStream.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public interface onResponseListener {
        void onSuccess();
        void onFailed(String errorMsg);
    }
}
