package com.optimizely.app

import com.optimizely.app.main.ProjectsDetailActivity
import com.optimizely.app.ui.BaseActivity
import com.optimizely.app.ui.login.LoginActivity
import com.optimizely.app.ui.main.MainActivity
import dagger.Component
import javax.inject.Singleton

/**
 * Created by jdeffibaugh on 12/07/15 for Optimizely.
 *
 * Main Dagger 2 component.
 */
@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {
    fun inject(application: OptlyApplication)
    fun inject(activity: BaseActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: ProjectsDetailActivity)
}
