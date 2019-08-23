package com.qihuan.daggerdemo.repository;

import com.qihuan.annotationlib.dagger.AutoModule;

import dagger.Binds;
import dagger.Module;

/**
 * RepositoryModule
 *
 * @author qi
 * @date 2019-08-23
 */
@AutoModule
@Module
public abstract class RepositoryModule {

    @Binds
    abstract DataRepository bindTestDataRepo(TestDataRepository repository);
}
