package com.optimizely.app.data

import android.content.Intent
import io.realm.Realm
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.observable
import rx.lang.kotlin.toSingletonObservable
import rx.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

class AuthService @Inject constructor(var optlyAppService: OptimizelyAppService,
                                      @Named("redirectUrl") var redirectUrl: String,
                                      @Named("clientSecret") var clientSecret: String,
                                      @Named("clientId") var clientId: String,
                                      @Named("hashedAndroidId") var hashedAndroidId: String) : AnkoLogger {

    private val sateParam = "state"
    private val codeParam = "code"

    fun authorize(intent: Intent, realm: Realm): Observable<Token> {
        return intent.toSingletonObservable().takeFirst {
            intent.action == Intent.ACTION_VIEW
                    && intent.data.queryParameterNames.contains(sateParam)
                    && intent.data.queryParameterNames.contains(codeParam)
                    && intent.data.getQueryParameter(sateParam) == hashedAndroidId
        }.map {
            it.data.getQueryParameter(codeParam)
        }.observeOn(Schedulers.io()).flatMap {
            optlyAppService.token(it, clientId, clientSecret, redirectUrl)
        }.observeOn(AndroidSchedulers.mainThread()).flatMap { token ->
            observable<Token> { subscriber ->
                realm.executeTransactionAsync({
                    it.copyToRealmOrUpdate(token)
                }, {
                    debug("Saved token to realm")
                    subscriber.onNext(realm.where(Token::class.java).findFirst())
                    subscriber.onCompleted()
                }, {
                    subscriber.onError(it)
                })
            }
        }
    }

    fun authorized(realm: Realm): Observable<Token> {
        return realm.where(Token::class.java)
                .findFirstAsync()
                .asObservable<Token>()
                .takeFirst {
                    it.isLoaded
                }
    }
}