# Releasing

* Create a local release branch from `master`
```bash
git checkout master
git pull
git checkout -b release_{{ fladle.next_release }}
```

* Update `version` in `fladle-plugin/build.gradle.kts` (remove `-SNAPSHOT`)
```kotlin
version = "{{ fladle.next_release }}"
```

* Update the current version and next version in `mkdocs.yml`:
```
extra:
  fladle:
    release: '{{ fladle.next_release }}'
    next_release: 'REPLACE_WITH_NEXT_VERSION_NUMBER'
```

* Take one last look
```
git diff
```

* Commit all local changes
```
git commit -am "Prepare {{ fladle.next_release }} release"
```

* Create a tag and push it
```bash
git tag v{{ fladle.next_release }}
git push origin v{{ fladle.next_release }}
```

Pushing the tag automatically triggers the [release workflow](https://github.com/runningcode/fladle/actions/workflows/gradle-release.yml) which:

1. Publishes to Maven Central
2. Publishes to Gradle Plugin Portal
3. Creates a GitHub Release with auto-generated notes

* Release to Maven Central
    * Login to Maven Central Repository: [https://central.sonatype.com/](https://central.sonatype.com/)
    * Click on **Publish**

* Merge the release branch to master
```
git checkout master
git pull
git merge --no-ff release_{{ fladle.next_release }}
```
* Update `version` in `fladle-plugin/build.gradle.kts` (increase version and add `-SNAPSHOT`)
```kotlin
version = "REPLACE_WITH_NEXT_VERSION_NUMBER-SNAPSHOT"
```

* Commit your changes
```
git commit -am "Prepare for next development iteration"
```

* Push your changes
```
git push
```
