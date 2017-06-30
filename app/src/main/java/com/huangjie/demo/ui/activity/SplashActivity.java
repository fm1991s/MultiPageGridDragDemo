package com.huangjie.demo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.huangjie.demo.manager.GridManager;
import com.huangjie.demo.util.ThreadUtils;
import com.huangjie.demo.ui.R;

/**
 * Created by huangjie on 2017/5/30.
 */

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GridManager.getInstance().loadGrid(null);
        ThreadUtils.postOnUiThreadDelay(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_no_animation, R.anim.activity_zoom_in);
                finish();
            }
        }, 2000);
    }

}
