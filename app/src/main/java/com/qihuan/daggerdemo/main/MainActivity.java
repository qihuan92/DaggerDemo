package com.qihuan.daggerdemo.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.qihuan.annotationlib.dagger.AutoComponent;
import com.qihuan.daggerdemo.R;
import com.qihuan.daggerdemo.base.BaseActivity;
import com.qihuan.daggerdemo.detail.DetailActivity;

import javax.inject.Inject;

@AutoComponent(modules = {MainModule.class})
public class MainActivity extends BaseActivity implements MainContract.View {

    @Inject
    MainContract.Presenter mainPresenter;

    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLifecycle().addObserver(mainPresenter);
        initView();
        initData();
    }
    private void initView() {
        tvContent = findViewById(R.id.tv_content);
        tvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        mainPresenter.requestData();
    }

    @Override
    public void onData(String content) {
        tvContent.setText(content);
    }
}
