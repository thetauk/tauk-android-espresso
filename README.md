# Tauk package for Android Espresso Tests.

The Tauk Espresso Package allows you to easily report and monitor your Espresso automation tests on the Tauk platform.


## Getting Started

Add the following dependency to your app level gradle

```gradle
dependencies {
    // ...
    
    androidTestImplementation 'com.tauk:tauk-espresso-library:0.1.0'
}
```

## Usage 

Add the following rule to your test class
```
    @get:Rule
    val watcher = TaukWatcher(API_TOKEN, PROJECT_ID)
```

API Token and Project ID can be specified as arguments while initializing `TaukWatcher()` or they can also be 
specified as instrumentation arguments using    
``-e taukApiToken YOUR_TOKEN -e taukProjectId YOUR_PROJECT_ID``


NOTE: In order to upload the results to Tauk make sure that the manifest contains the below permissions
``<uses-permission android:name="android.permission.INTERNET" />``