package com.optimizely.app.ui

import android.app.Activity
import android.os.Bundle
import com.optimizely.app.OptlyApplication
import io.realm.Realm
import org.jetbrains.anko.AnkoLogger
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * Created by jdeffibaugh on 12/10/15 for Optimizely.
 *
 * Manages things common to all Activities
 */
abstract class BaseActivity : Activity(), AnkoLogger {

    override val loggerTag: String
        get() = "Optly"

    private var compoSub: CompositeSubscription = CompositeSubscription()

    @Inject lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OptlyApplication.graph.inject(this);
    }

    override fun onStop() {
        compoSub.unsubscribe()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    fun manageSub(subscription: Subscription) {
        compoSub.add(subscription)
    }
}
