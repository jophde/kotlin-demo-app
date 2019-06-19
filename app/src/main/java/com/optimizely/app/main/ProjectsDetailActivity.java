package com.optimizely.app.main;

import android.os.Bundle;

import com.optimizely.app.OptlyApplication;
import com.optimizely.app.R;
import com.optimizely.app.data.OptimizelyApisService;
import com.optimizely.app.ui.BaseActivity;

import javax.inject.Inject;

public class ProjectsDetailActivity extends BaseActivity {

    @Inject OptimizelyApisService optimizelyApisService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OptlyApplication.graph.inject(this);
        setContentView(R.layout.activity_projects_detail);
    }
}
