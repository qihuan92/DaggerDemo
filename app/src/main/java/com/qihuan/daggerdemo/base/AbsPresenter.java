package com.qihuan.daggerdemo.base;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * AbsPresenter
 *
 * @author qi
 * @date 2019/4/17
 */
public abstract class AbsPresenter<V> implements BasePresenter {

    private V view;

    public AbsPresenter(V view) {
        this.view = view;
        // lifecycle
        if (view != null) {
            if (view instanceof FragmentActivity) {
                ((FragmentActivity) view).getLifecycle().addObserver(this);
            } else if (view instanceof Fragment) {
                ((Fragment) view).getLifecycle().addObserver(this);
            }
        }
    }

    protected V getView() {
        return view;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        view = null;
    }

    @Override
    public void onLifecycleChanged(@NonNull LifecycleOwner owner, @NonNull Lifecycle.Event event) {

    }
}
