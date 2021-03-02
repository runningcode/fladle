# FAQ

## Error APK file not found
The app APK and the instrumentation APK are expected to have already been generated before calling runFlank. To generate APKs, run `assembleDebug` and `assembleDebugAndroidTest` before running `runFlank`. 

You can also have Fladle build them for you by using the [`dependOnAssemble`](../configuration/#dependOnAssemble) property.




## No signature of method
If you receive an error like this, it is likely caused by invalid fladle extension confiuration.
The syntax was changed in the `0.9.X` releases in order to avoid touching files during the configuration phase.
```bash
No signature of method: flank_4vvjv7w3oopge32w1tl9cs6e4.fladle() is applicable for argument types: (flank_4vvjv7w3oopge32w1tl9cs6e4$_run_closure1) values: [flank_4vvjv7w3oopge32w1tl9cs6e4$_run_closure1@649a2315]
			Possible solutions: file(java.lang.Object), find(), findAll(), file(java.lang.Object, org.gradle.api.PathValidation), files([Ljava.lang.Object;), findAll(groovy.lang.Closure)
```

If you receive a similar error, please check [configuration](../configuration#sample-configuration) for a sample configuration.

## Debugging
`./gradlew runFlank -PdumpShards` Will dump shards and exit the process without running the tests.

`./gradlew printYml` Will print out the current yaml configuration to be passed to Flank.


## More help?
Still having trouble? Check the #flank channel in the [Firebase Community Slack](https://firebase.community/)
