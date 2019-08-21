package com.qihuan.daggerdemo.di.component;

import android.app.Application;

import com.qihuan.dagger.AndroidBindingModule;
import com.qihuan.daggerdemo.App;
import com.qihuan.daggerdemo.di.module.AppModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * AppComponent
 *
 * @author qi
 * @date 2019/4/17
 */
@Singleton
@Component(modules = {AppModule.class, AndroidSupportInjectionModule.class, AndroidBindingModule.class})
public interface AppComponent extends AndroidInjector<App> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        AppComponent.Builder application(Application application);

        AppComponent build();
    }
}
