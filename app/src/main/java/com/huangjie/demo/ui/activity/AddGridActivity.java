package com.huangjie.demo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.huangjie.demo.manager.GridManager;
import com.huangjie.demo.ui.R;
import com.huangjie.demo.util.ThreadUtils;
import com.huangjie.demo.util.UrlUtils;

/**
 * Created by huangjie on 2017/6/30.
 */

public class AddGridActivity extends Activity implements View.OnClickListener{


    private ImageView mBackBtn;
    private EditText mTitleEditText;
    private EditText mWebsiteEditText;
    private Button mAddGridBtn;
    private InputMethodManager mInputMethodManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_grid_layout);
        mBackBtn = (ImageView) findViewById(R.id.add_grid_back);
        mTitleEditText = (EditText) findViewById(R.id.add_website_title);
        mWebsiteEditText = (EditText) findViewById(R.id.add_website_url);
        mAddGridBtn = (Button) findViewById(R.id.add_grid);
        mBackBtn.setOnClickListener(this);
        mAddGridBtn.setOnClickListener(this);
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mTitleEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI
                | EditorInfo.IME_ACTION_DONE);
        mWebsiteEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        mWebsiteEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI
                | EditorInfo.IME_ACTION_DONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTitleEditText.requestFocus();
        showKeyboard(mTitleEditText);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }

    private void showKeyboard(View view) {
        mInputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    private void hideKeyboard() {
        mInputMethodManager.hideSoftInputFromWindow(
                mTitleEditText.getWindowToken(), 0);
        mInputMethodManager.hideSoftInputFromWindow(
                mWebsiteEditText.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_grid_back:
                onBackPressed();
                break;
            case R.id.add_grid:
                hideKeyboard();
                ThreadUtils.postOnUiThreadDelay(new Runnable() {
                    @Override
                    public void run() {
                        addWebsite();
                    }
                }, 50);
                break;
        }
    }

    private void addWebsite() {
        final String title = mTitleEditText.getText().toString();
        final String url = mWebsiteEditText.getText().toString();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "网站名称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "网站地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidUrl(url)) {
            Toast.makeText(this, "网站地址为无效地址", Toast.LENGTH_SHORT).show();
            return;
        }

        String fixUrl = url;
        if (UrlUtils.WEB_URL.matcher(url.toLowerCase().trim()).matches()) {
            fixUrl = UrlUtils.reformatUrl(url);
        }
        final String finalUrl = fixUrl;
        GridManager.getInstance().queryByUrl(finalUrl, new GridManager.QueryResponseListener() {
            @Override
            public void onResult(final Object obj) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (obj != null) {
                            Toast.makeText(AddGridActivity.this, "网站地址已存在！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent intent = new Intent();
                        intent.putExtra("title", title);
                        intent.putExtra("url", finalUrl);
                        AddGridActivity.this.setResult(RESULT_OK, intent);
                        finish();
                        overridePendingTransition(R.anim.activity_zoom_in, R.anim.activity_zoom_out);
                    }
                });
            }

            @Override
            public void onError(String msg) {
                Log.d("AddGridActivity", "error msg:" + msg);
            }
        });
    }

    private boolean isValidUrl(String url) {
        if (!url.toLowerCase().startsWith("file://") &&
                !url.toLowerCase().startsWith("ftp://") &&
                !url.toLowerCase().startsWith("market://") &&
                !url.toLowerCase().startsWith("about:") &&
                !url.toLowerCase().startsWith("wtai://") &&
                !url.toLowerCase().startsWith("data:") &&
                !UrlUtils.WEB_URL.matcher(url.toLowerCase().trim()).matches())
        {
            return false;
        }

        return true;
    }
}
