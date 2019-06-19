package com.optimizely.app

import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

/**
 * Created by jdeffibaugh on 12/07/15 for Optimizely.
 *
 * Main entry to point for the app.  Will be called before any Activity, Service, BroadcastReceiver,
 * or ContentProvider is created.
 */

class OptlyApplication : android.app.Application() {

    companion object {
        lateinit var graph: ApplicationComponent
    }

    @Inject lateinit var realmConfig: RealmConfiguration

    override fun onCreate() {
        super.onCreate()
        graph = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
                .build()
        graph.inject(this)

        Realm.setDefaultConfiguration(realmConfig);
    }
}