package com.qihuan.daggerdemo.detail;

import com.qihuan.daggerdemo.base.AbsPresenter;
import com.qihuan.daggerdemo.repository.DataRepository;

import javax.inject.Inject;

/**
 * DetailPresenter
 *
 * @author qi
 * @date 2019-08-21
 */
public class DetailPresenter extends AbsPresenter<DetailContract.View> implements DetailContract.Presenter {

    @Inject
    DataRepository repository;

    @Inject
    public DetailPresenter(DetailContract.View view) {
        super(view);
    }

    @Override
    public void getContent() {
        getView().onContent(repository.getContent());
    }
}
