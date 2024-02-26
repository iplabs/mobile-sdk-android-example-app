# ip.labs Mobile SDK – Example App for Android

This is an example app to demonstrate the usage of the [ip.labs Mobile SDK](https://www.iplabs.com/photo-commerce-mobile-sdk/) on Android.

Please refer to the [detailed documentation](https://docs.sdk.iplabs.io/) for further information on how to integrate the ip.labs Mobile SDK into your app.

## Development Setup

### Opening the Project

This folder is an [Android Studio](https://developer.android.com/studio/) project and should be imported with the latest version of this IDE to check and run the app. The main language used is [Kotlin](https://kotlinlang.org/) and [JDK 20](https://www.oracle.com/java/technologies/javase/jdk20-archive-downloads.html) must be installed.

### Configuring Secrets

When first checking out this repository, building, and launching the app, the features user login, product ordering, and user tracking will not be available. This is due to the fact, that all of these functionalities each require a secret that you have to configure locally in order for them to be activated in a regular fashion. There are two ways to achieve this:

* Store the secrets in your system’s environment variables. For more information please refer to the documentation of your specific platform.
* Put the secrets in a special file called `local.properties` that you have to create in the root of this project with key-value pairs in it (one per line, key and value separated by just an equals sign, values double-quoted).

⚠️ *If you choose the latter variant: Please create the file exactly as described, so it is excluded from committing and pushing via Git. Never let any of the secrets become public.*

Using environment variables is the preferred way. Though, if you just want to temporarily change one of the values, it is easier to do so in the file, because that takes precedence over the environment definitions and also prevents you from having to restart the IDE / terminal to reevaluate the system environment.

Here is a list of all secrets currently available and their meaning:

* `IPLABS_MOBILE_SDK_ADD_USER_INFO_URL`:  
URL to your WIPE SSO login backend; activates availability of login
* `IPLABS_MOBILE_SDK_EXTERNAL_CART_SERVICE_KEY`:  
secret key for your ECS backend instance; activates availability of ordering
* `IPLABS_MOBILE_SDK_AMPLITUDE_API_KEY`:  
your Amplitude API token; activates availability of user tracking

You can also just define a subset of these secret and leave others out. The values for `IPLABS_MOBILE_SDK_ADD_USER_INFO_URL` and `IPLABS_MOBILE_SDK_EXTERNAL_CART_SERVICE_KEY` are tied to a specific customer setup. [Get in contact with us](https://www.iplabs.com/photo-commerce-mobile-sdk/) for further details.

## Disclaimer

The code in this repository serves demonstrative purposes only and is not meant to be used in “real-world” apps “as is”. It does not necessarily reflect any of Google’s best practices, general concerns for GDPR, and other data protection laws compliance, latest dependency update states etc. Use at your own risk.

*[ECS]: External Cart Service
