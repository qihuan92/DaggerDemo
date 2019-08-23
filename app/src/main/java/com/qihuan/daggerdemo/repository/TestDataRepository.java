package com.qihuan.daggerdemo.repository;

import javax.inject.Inject;

/**
 * TestDataRepository
 *
 * @author qi
 * @date 2019-08-23
 */
public class TestDataRepository implements DataRepository {

    @Inject
    public TestDataRepository() {
    }

    @Override
    public String getContent() {
        return getClass().getSimpleName();
    }
}
