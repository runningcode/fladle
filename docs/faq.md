# FAQ

## Error APK file not found
The app APK and the instrumentation APK are expected to have already been generated before calling runFlank.
If you would like the flank task to automatically create the APKs, you can add the following to your application's build.gradle.
```
afterEvaluate {
    tasks.named("execFlank").configure {
        dependsOn("assembleDebugAndroidTest")
    }
}
```

See [https://issuetracker.google.com/issues/152240037]() for more information.


## No signature of method
If you receive an error like this, it is likely caused by invalid fladle extension confiuration.
The syntax was changed in the `0.9.X` releases in order to avoid touching files during the configuration phase.
```bash
No signature of method: flank_4vvjv7w3oopge32w1tl9cs6e4.fladle() is applicable for argument types: (flank_4vvjv7w3oopge32w1tl9cs6e4$_run_closure1) values: [flank_4vvjv7w3oopge32w1tl9cs6e4$_run_closure1@649a2315]
			Possible solutions: file(java.lang.Object), find(), findAll(), file(java.lang.Object, org.gradle.api.PathValidation), files([Ljava.lang.Object;), findAll(groovy.lang.Closure)
```

If you receive a similar error, please check [configuration](configuration/#sample-configuration) for a sample configuration.

## Debugging
`./gradlew runFlank -PdumpShards` Will dump shards and exit the process without running the tests.

`./gradlew printYml` Will print out the current yaml configuration to be passed to Flank.


## More help?
Still having trouble? Check the #flank channel in the [Firebase Community Slack](https://firebase.community/)