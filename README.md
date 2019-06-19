Optimizely Android App
=====================

The Optimizely Android app should allow users to view experiments, toggle them on and off, and see results.  Please see the designs pinned #optimizely-mobile-app Slack channel.  The app's backend is the [Optimizely REST API] (http://developers.optimizely.com/rest/).  A similar app is being built for iOS in Swift.

### 3rd Party Dependencies
##### Kotlin
The initial parts of the app were developed in Kotlin.  Kotlin is a JVM language that was created by Jetbrains.  They are the company that makes Intellij which Android Studio is a fork of.  Kotlin is is similar to Swift.  It has named parameters, first class functions, etc.  The best part is that the most of it's magic happens at build time.  There is very little run time overhead required to use Kotlin for an Android app which is important for limiting APK size and method count.  Kotlin is also extremely interoperable with Java and works flawlessly in Android Studio.

##### Anko
Anko is a view hierarchy DSL for Android that is made possible via Kotlin.  The initial prototype UI for the app was written in Anko.  Anko is not as robust as the regular Android XML based resource system but it is far more expressive.  It's not a zero sum game either.  Anko can be used for some UI while the traditional XML approach can be used elsewhere.  

```
linearLayout {
  button {
    onClick {
      foo()
    }
  }
}
```

This same code would require the equivalent XML and Java binding code.  Anko even has a visual editor like XML!  The only downside is that it doesn't work right with some libraries such as the App Compat Library from Google.  Fortunately this app is targetting Android 21+ so we don't need that library :).

##### RxJava
RxJava is part of the ReactiveX world.  It comes from Netflix. It allows us to create functional chains on obserabales that can be subsribed to by observers.  The beauty is that it easily allows things to happen on one thread and be communicated back to another thread.  This is very important on Android because NO I/O should happen on the UI thread.  

https://www.google.com/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=rxjava
http://blog.danlew.net/2014/09/15/grokking-rxjava-part-1/

##### Dagger 2
Dagger 2 is a dependency injection framework from Google.  It improves upon the original Dagger by not using reflection.  It does some build time magic to generate additional classes.

http://google.github.io/dagger/

##### Retrofit
Retrofit is a REST Api client generator.  It uses annotation processing to make defining endpoints very simple.  It uses OkHttp3 under the hood which it has a transitive dependency on.  It's from Square and widely used.

http://square.github.io/retrofit/

##### RxBinding
Also from Square.  This library adds Rx inspired bindings to common Android classes such as Buttons, Views, Intents etc.  It's not very useful when using Anko, however it really signs when building UIs the traditional way Android XML and Java bindings. 

https://github.com/JakeWharton/RxBinding

##### Realm
Realm is a very fast DB designed specifically for mobile.  It has sub 1ms read and writes.  It also has live updates.  

https://realm.io/

##### Espresso
Espresso is UI testing framework from Google that is the Android repo.  It makes UI testing much less flaky.

https://google.github.io/android-testing-support-library/docs/espresso/

##### Mockito
Used to mock out dependencies in a very easy way.  Very useful for unit testing code that contains Android dependencies so that tests can be run on the dev computer's JVM instead of a device.  This is much faster!

http://mockito.org/

#### Junit4
Java unit testing framework.

http://junit.org/junit4/

### Current State
The app currently has SSO implemented almost fully.  It's just missing handling expired tokens.  It also has two activiies implmented end to end with UI, I/O, etc.  The app is architected with a MVPS pattern.  MVPS stands for Model View Presenter Service.  

### Architecture
Models should be close to POJOs.  Since we are using Realm our models extend the `RealmObject` base object.  This enables live updating of data through Realm.  

Our views can Anko DSL or Android XML.  A class that implements view should be isolated from `Activity` to enable much easier testing.  Mocking out the the Android framework is very painful!  

Our Preseneter is usually an `Activity`.  Presenters will have a lot of Android dependencies so they will need to be tested on real devices or simulators.  Things without Android dependencies can be tested with plain Java unit tests on the JVM. 

Services handle I/O and are usually RxJava `Observable` classes.  This allows them to be chained together and operated on functionally.  `Service` classes usually deal with I/O both local and network.  Our `Service` component should not be confused with the Android `Service` class which is like a daemon.  

### TODO
- Handle auth token expiring.  The backend will return a 403.  The stored token should be cleared and the user should be prompted to log back in.
- Set up unit, integration, and UI tests on the existing core functionality.
- Continue implmenting design
