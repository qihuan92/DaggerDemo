package com.qihuan.daggerdemo.main;

import dagger.Binds;
import dagger.Module;

/**
 * MainModule
 *
 * @author qi
 * @date 2019/4/17
 */
@Module
public abstract class MainModule {

    @Binds
    abstract MainContract.Presenter bindMainPresenter(MainPresenter presenter);

    @Binds
    abstract MainContract.View bindView(MainActivity mainActivity);
}
