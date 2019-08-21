package com.qihuan.daggerdemo.base;

import android.os.Bundle;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * BaseActivity
 *
 * @author qi
 * @date 2019/4/17
 */
public abstract class BaseActivity extends DaggerAppCompatActivity implements BaseView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
