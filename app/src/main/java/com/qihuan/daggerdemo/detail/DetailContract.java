package com.qihuan.daggerdemo.detail;

import com.qihuan.daggerdemo.base.BasePresenter;
import com.qihuan.daggerdemo.base.BaseView;

/**
 * DetailContract
 *
 * @author qi
 * @date 2019-08-21
 */
public interface DetailContract {
    interface View extends BaseView {
        void onContent(String content);
    }

    interface Presenter extends BasePresenter {
        void getContent();
    }
}
