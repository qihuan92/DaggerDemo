package com.qihuan.daggerdemo.detail;

import android.os.Bundle;
import android.widget.TextView;

import com.qihuan.annotationlib.dagger.AutoComponent;
import com.qihuan.daggerdemo.R;
import com.qihuan.daggerdemo.base.BaseActivity;

import javax.inject.Inject;

@AutoComponent(modules = DetailModule.class)
public class DetailActivity extends BaseActivity implements DetailContract.View {

    @Inject
    DetailPresenter detailPresenter;

    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        initView();

        detailPresenter.getContent();
    }

    private void initView() {
        tvContent = findViewById(R.id.tv_content);
    }

    @Override
    public void onContent(String content) {
        tvContent.setText(content);
    }
}
