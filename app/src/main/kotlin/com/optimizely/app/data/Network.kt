package com.optimizely.app.data

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.realm.RealmObject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import rx.Observable

/**
 * Created by jdeffibaugh on 12/9/15 for Optimizely.
 *
 * Retrofit Interfaces.
 */
interface OptimizelyApisService {
    @GET("/experiment/v1/projects/")
    fun projects(@Header("Authorization") t: String): Observable<MutableList<Project>>

    companion object {
        fun create(): OptimizelyApisService {
            return restAdapter(gsonBuilder(), httpClient(), "https://www.optimizelyapis.com").create(OptimizelyApisService::class.java)
        }
    }
}

interface OptimizelyAppService {
    @POST("/oauth2/token")
    fun token(@Query("code") code: String,
              @Query("client_id") clientId: String,
              @Query("client_secret") clientSecret: String,
              @Query("redirect_uri") redirectUri: String,
              @Query("grant_type") grantType: String = "authorization_code"): Observable<Token>

    companion object {
        fun create(): OptimizelyAppService {
            return restAdapter(gsonBuilder(), httpClient(), "https://app.optimizely.com").create(OptimizelyAppService::class.java)
        }
    }
}

private fun gsonBuilder(): GsonBuilder {
    return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setExclusionStrategies(object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                    return false
                }

                override fun shouldSkipField(f: FieldAttributes?): Boolean {
                    val ret = f?.declaringClass == RealmObject::class.java
                    return ret
                }
            })
}

private fun httpClient(): OkHttpClient {
    return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .build()
}

private fun loggingInterceptor(): Interceptor {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY
    return interceptor;
}

private fun restAdapter(gsonBuilder: GsonBuilder, httpClient: OkHttpClient, baseUrl: String): Retrofit {
    return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            .client(httpClient)
            .build()
}
