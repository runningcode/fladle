# FAQ

## Error APK file not found
The app APK and the instrumentation apk are expected to have already been generated before calling runFlank.
If you would like the flank task to automatically create the APKs, you can add the following to your application's build.gradle.
```
afterEvaluate {
    tasks.named("execFlank").configure {
        dependsOn("assembleDebugAndroidTest")
    }
}
```

See [https://issuetracker.google.com/issues/152240037]() for more information.
