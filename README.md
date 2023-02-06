# ip.labs Mobile SDK – Example App for Android

This is an example app to demonstrate the usage of the [ip.labs Mobile SDK](https://www.iplabs.com/photo-commerce-mobile-sdk/) on Android.

Please refer to the [detailed documentation](http://mobile-sdk-docs.s3-website.eu-central-1.amazonaws.com/) for further information on how to integrate the ip.labs Mobile SDK into your app.

## Development Setup

### Opening the Project

This folder is an [Android Studio](https://developer.android.com/studio/) project and should be imported with this IDE to check and run the app. The main language used is [Kotlin](https://kotlinlang.org/).

### Configuring Secrets

When first checking out this repository, building, and launching the app, the features user login, product ordering, and user tracking will not be available. This is due to the fact, that all of these functionalities each require a secret that you have to configure locally in order for them to be activated in a regular fashion. To achieve that, create a text file called `secrets.properties` in the root of this project and fill it with these key-value pairs (one per line and doesn’t have to be each one):

* `ADD_USER_INFO_URL="<value>"`  
in which `<value>` is the URL to your WIPE SSO login backend  
→ activates availability of login
* `EXTERNAL_CART_SERVICE_KEY="<value>"`  
in which `<value>` is the secret key for your ECS backend instance  
→ activates availability of ordering
* `AMPLITUDE_API_KEY="<value>"`  
in which `<value>` is your Amplitude API token  
→ activates availability of user tracking

The values for `ADD_USER_INFO_URL` and `EXTERNAL_CART_SERVICE_KEY` are tied to a specific customer setup. [Get in contact with us](https://www.iplabs.com/photo-commerce-mobile-sdk/) for further details.

⚠️ *Please create the file exactly as described, so it is excluded from committing and pushing via Git. Never let any of the secrets become public.*

## Disclaimer

The code in this repository serves demonstrational purposes only and is not meant to be used in “real-world” apps “as is”. It does not necessarily reflect any of Google’s best practices, general concerns for GDPR and other data protection laws compliance, latest dependency update states etc. Use at your own risk.
