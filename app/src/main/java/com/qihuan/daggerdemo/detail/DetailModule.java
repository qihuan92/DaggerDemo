package com.qihuan.daggerdemo.detail;

import dagger.Binds;
import dagger.Module;

/**
 * DetailModule
 *
 * @author qi
 * @date 2019-08-21
 */
@Module
public abstract class DetailModule {

    @Binds
    abstract DetailContract.View bindView(DetailActivity detailActivity);
}
