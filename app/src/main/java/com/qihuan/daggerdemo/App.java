package com.qihuan.daggerdemo;

import com.qihuan.annotationlib.dagger.AutoAppBinding;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;

/**
 * App
 *
 * @author qi
 * @date 2019/4/17
 */
@AutoAppBinding
public class App extends DaggerApplication {

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().application(this).build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
