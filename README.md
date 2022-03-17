# Tauk package for Android Espresso Tests.

The Tauk Espresso Package allows you to easily report and monitor your Espresso automation tests on the Tauk platform.


## Getting Started

Add the following dependency to your app level gradle

```gradle
dependencies {
    // ...
    
    androidTestImplementation 'com.tauk:tauk-espresso-library:0.1.2'
}
```

## Usage 

Add the following rule to your test class
```
    @get:Rule
    val watcher = TaukWatcher(API_TOKEN, PROJECT_ID)
```

## Invoking test via `adb`
API Token and Project ID can be specified as arguments while initializing `TaukWatcher()` or they can also be 
specified as instrumentation arguments using    
``-e taukApiToken YOUR_TOKEN -e taukProjectId YOUR_PROJECT_ID``

Example:
```
    adb shell am instrument -m -w -e taukApiToken <API_TOKEN> -e taukProjectId <PROJECT_ID> <APP_PACKAGE>/androidx.test.runner.AndroidJUnitRunner
```

## Invoking test via `Gradle`
You can also invoke the test from gradle using gradle `connectedAndroidTest` task

Example:
```
    ./gradlew cAT -Pandroid.testInstrumentationRunnerArguments.taukProjectId=<PROJECT_ID> -Pandroid.testInstrumentationRunnerArguments.taukApiToken=<API_TOKEN>
```

Alternatively you can also provide the API Token and project ID in `build.gradle` file

```
    defaultConfig {
        ...
        testInstrumentationRunnerArguments taukProjectId: '<PROJECT_ID>'
        testInstrumentationRunnerArguments taukApiToken: '<API_TOKEN>'
        ...
    }
```


### NOTE: In order to upload the results to Tauk make sure that the manifest contains the below permissions

```
    <uses-permission android:name="android.permission.INTERNET" />
```