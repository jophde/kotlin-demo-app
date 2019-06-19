package com.optimizely.app.ui.login

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.optimizely.app.OptlyApplication
import com.optimizely.app.R
import com.optimizely.app.data.AuthService
import com.optimizely.app.ui.BaseActivity
import com.optimizely.app.ui.main.MainActivity
import org.jetbrains.anko.*
import java.net.URL
import javax.inject.Inject
import javax.inject.Named
import android.R as AR

/**
 * Created by jdeffibaugh on 12/15/15 for Optimizely.
 *
 * Handles logging user in with Optimizely SSO
 */
class LoginActivity : BaseActivity(), AnkoLogger, LoginPresenter {
    lateinit var view: LoginView

    @field:[Inject Named("authorizeUrl")] lateinit var authorizeUrl: URL
    @Inject lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OptlyApplication.graph.inject(this)

        val ui = LoginActivityUi(this)
        ui.setContentView(this)
        view = ui

        val sub = authService.authorize(intent, realm).mergeWith(authService.authorized(realm)).subscribe({
            if (it.isValid) {
                debug("Got valid token")
                startActivity<MainActivity>()
                finish()
            } else {
                debug("Got invalid token")
            }
        }, {
            error("Unable to get token", it)
        })
        manageSub(sub)
    }

    override fun onLogin() {
        debug("Redirecting to browser app for url:\n${authorizeUrl.toString()}")
        // Launches an Android Web Browser App via Intent
        // After the user logs in in the browser a new instance of
        // this activity will be started because the browser will
        // try to open up a URL that this activity registered an
        // intent filter on.  Optimizely passes token data back
        // in that URL.  The intent that started this activity is
        // checked in initialize.
        browse(authorizeUrl.toString())
        finish()
    }
}

class LoginActivityUi(val presenter: LoginPresenter? = null) : AnkoComponent<LoginActivity>, AnkoLogger, LoginView {

    override fun createView(ui: AnkoContext<LoginActivity>): View = ui.apply {
        frameLayout {
            verticalLayout {
                button {
                    textResource = R.string.login_button_text
                    onClick {
                        presenter?.onLogin()
                    }
                }
            }.lparams {
                horizontalMargin = dip(16)
                verticalMargin = dip(16)
                gravity = Gravity.CENTER
            }
        }
    }.view
}

interface LoginPresenter {
    fun onLogin()
}

interface LoginView {}
