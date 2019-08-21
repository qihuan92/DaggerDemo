package com.qihuan.daggerdemo.main;

import com.qihuan.daggerdemo.base.BasePresenter;
import com.qihuan.daggerdemo.base.BaseView;

/**
 * MainContract
 *
 * @author qi
 * @date 2019/4/17
 */
public interface MainContract {
    interface View extends BaseView {
        void onData(String content);
    }

    interface Presenter extends BasePresenter {
        void requestData();
    }
}
