# Authentication

There are two authentication mechanisms for using Fladle.

It is recommended to use user authentication on local development machines to avoid sharing credentials and a service account on CI.

## User authentication

Credentials are stored in `~/.flank`.

1. `./gradlew flankAuth`
2. Sign in to web browser.
3. Specify [projectId](../configuration/#projectid)
4. `./gradlew runFlank`

## Service account credentials

1. **Create a service account**. Service accounts aren't subject to spam checks or captcha prompts, which could
   otherwise block your CI builds. Create a service account with an **Editor** role in the
   [Google Cloud Platform console].

2. **Enable required APIs**. After logging in using the service account: In the [Google Developers Console API Library]
   page, enable the **Google Cloud Testing API** and **Cloud Tool Results API**. To enable these APIs, type these API names into
   the search box at the top of the console, and then click **Enable API** on the overview page for that API.

3. Download the `json` service account credentials and place them on the file system.

4. Configure the [Fladle extension serviceAccountCredentials] to point to the credentials.

Above instructions are based on Google instruction for [authenticating with CI].

See also Flank's instructions for [authenticating with a service account].


[google cloud platform console]: https://console.cloud.google.com/iam-admin/serviceaccounts/
[google developers console api library]: https://console.developers.google.com/apis/library
[these steps]: https://firebase.google.com/docs/test-lab/android/continuous#requirements
[Fladle extension serviceAccountCredentials]: ../configuration/#serviceaccountcredentials
[authenticating with CI]: https://firebase.google.com/docs/test-lab/android/continuous
[authenticating with a service account]: https://flank.github.io/flank/#authenticate-with-a-service-account
