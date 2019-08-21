package com.qihuan.daggerdemo.main;

import com.qihuan.daggerdemo.base.AbsPresenter;

import javax.inject.Inject;

/**
 * MainPresenter
 *
 * @author qi
 * @date 2019/4/17
 */
public class MainPresenter extends AbsPresenter<MainContract.View> implements MainContract.Presenter {

    @Inject
    public MainPresenter(MainContract.View view) {
        super(view);
    }

    @Override
    public void requestData() {
        String content = "hello dagger demo";
        getView().onData(content);
    }
}
