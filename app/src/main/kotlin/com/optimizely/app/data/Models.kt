@file:Suppress("unused")

package com.optimizely.app.data

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

/**
 * Created by jdeffibaugh on 12/9/15 for Optimizely.
 *
 * Models for app
 *
 * Realm objects can not be used across. Since view manipulation can be done on the UI thread
 * and I/O should be on a background thread we need a separate UI only model.  The conversion should
 * happen inside of a map function in an RxJava chain.
 */
open class Project(
        @PrimaryKey open var id: Long = -1,
        open var projectName: String = "",
        @Index open var accountId: Long = -1,
        open var ipAnonymization: Boolean = false,
        open var projectStatus: String = "",
        open var created: String = "",
        open var lastModified: String = ""


) : RealmObject() {
    override fun equals(other: Any?): Boolean {
        if (other is Project) {
            return id.equals(other.id);
        }
        return false;
    }

    override fun hashCode(): Int {
        return id.hashCode();
    }
}

open class Token(
        @PrimaryKey open var accessToken: String = "",
        open var expiresIn: Long = -1,
        open var tokenType: String = "",
        open var refreshToken: String = ""
) : RealmObject()
