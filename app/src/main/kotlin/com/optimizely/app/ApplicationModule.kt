package com.optimizely.app

import android.app.Application
import android.provider.Settings
import com.optimizely.app.data.OptimizelyApisService
import com.optimizely.app.data.OptimizelyAppService
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.net.URL
import javax.inject.Named
import javax.inject.Singleton

/**
 * Base module for entire application.
 */
@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @Singleton
    fun providesApplication(): Application {
        return application
    }

    @Provides
    @Singleton
    fun providesOptimizelyApisService(): OptimizelyApisService {
       return OptimizelyApisService.create()
    }

    @Provides
    @Singleton
    fun providesOptimizelyAppService(): OptimizelyAppService {
        return OptimizelyAppService.create()
    }

    @Provides
    fun providesRealmConfig(application: Application): RealmConfiguration {
        return RealmConfiguration.Builder(application).build()
    }

    @Provides
    fun providesRealm(): Realm {
        return Realm.getDefaultInstance()
    }

    @Provides
    @Named("clientId")
    fun providesClientId(): String {
        return "4234623377"
    }

    @Provides
    @Named("clientSecret")
    fun providesClientSecret(): String {
        // TODO Load from config
        return ""
    }

    @Provides
    @Named("redirectUrl")
    fun providesRedirectUrl(): String {
        return "https://android.optimizely.com/"
    }

    @Provides
    @Named("hashedAndroidId")
    fun providesHashedAndroidId(application: Application): String {
        val androidId = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
        return String(Hex.encodeHex(DigestUtils.md5(androidId)));
    }

    @Provides
    @Named("authorizeUrl")
    fun providesAuthorizeUrl(@Named("clientId") cid: String, @Named("redirectUrl") redirectUrl: String, @Named("hashedAndroidId") hashedAndroidId: String): URL {
        return URL("https://app.optimizely.com/oauth2/authorize?client_id=$cid&redirect_uri=$redirectUrl&response_type=code&scopes=all&state=$hashedAndroidId")
    }
}
