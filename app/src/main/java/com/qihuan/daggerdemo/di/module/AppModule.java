package com.qihuan.daggerdemo.di.module;

import android.app.Application;

import com.qihuan.daggerdemo.App;

import dagger.Binds;
import dagger.Module;

/**
 * AppModule
 *
 * @author qi
 * @date 2019/4/17
 */
@Module
public abstract class AppModule {

    @Binds
    abstract Application bindApp(App app);
}
