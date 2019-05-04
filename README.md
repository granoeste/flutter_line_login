# flutter_line_login

[![pub package](https://img.shields.io/pub/v/flutter_line_login.svg)](https://pub.dartlang.org/packages/flutter_line_login)

A Flutter plugin for allowing users to authenticate with native Android & iOS LINE login SDKs.

## Getting Started

Simple code for LINE login:

```dart
  var _flutterLineLogin = new FlutterLineLogin();

  await _flutterLineLogin.startWebLogin(
            (data) => {
              // LoginSuccess              
            },
            (error) => {
              // LoginError
            });

}

```

For more detailed code you need to see an [example](https://github.com/granoeste/flutter_line_login/tree/master/example).

## Installation

You'll have to declare a pubspec dependency in your Flutter project. Also some minimal Android & iOS specific configuration must be done.

### On your Flutter project

See the [installation instructions on pub](https://pub.dartlang.org/packages/flutter_line_login#-installing-tab-).

### Android

First of all, you need to create a channel for your application in LINE's Developer Console.
Please refer to [Integrating LINE Login with your Android app](https://developers.line.me/en/docs/line-login/android/integrate-line-login/) and prepare.
Once this is done you can get the channel ID.

Next, copy and paste the following into the string resource file.

**\<your project root\>/android/app/src/main/res/values/strings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Replace "0000000000" with your LINE Channel ID here. -->
    <string name="line_channel_id">0000000000</string>
</resources>
```

If you want change requestCode of startActivityForResult when Line login, add below  integer resource.

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Replace "20000904" with requestCode in your app. -->
    <integer name="request_code_login">20000904</integer>
</resources>
```


When the following error occurs at the time of build, it is necessary to make the version of Support Library the same. flutter_line_login dependencies `com.android.support: customtabs: 27.1.1`.

```
Caused by: java.lang.RuntimeException: Android dependency 'com.android.support:support-core-utils' has different version for the compile (26.1.0) and runtime (27.1.1) classpath. You should manually set the same version via DependencyResolution
```

Done!

### iOS

First of all, you need to create a channel for your application in LINE's Developer Console.
Please refer to [Integrate LINE Login with your iOS app](https://developers.line.me/en/docs/line-login/ios/integrate-line-login/) and prepare.
Once this is done you can get the channel ID.

Next, Set your channel ID in your application’s Info.plist as follows. Make sure you change “1234567890” to the correct channel ID for your channel. Add the required settings for app-to-app login to Info.plist. This lets the user automatically log in to your app if they are logged in to the LINE app.

**\<your project root\>/ios/Runner/Info.plist**

```xml
<key>LineSDKConfig</key>
<dict>
    <key>ChannelID</key>
    <!-- Replace "1234567890" with your LINE Channel ID here. -->
    <string>1234567890</string>
</dict>

<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleTypeRole</key>
        <string>Editor</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>line3rdp.$(PRODUCT_BUNDLE_IDENTIFIER)</string>
        </array>
    </dict>
</array>
<key>LSApplicationQueriesSchemes</key>
<array>
    <string>lineauth</string>
    <string>line3rdp.$(PRODUCT_BUNDLE_IDENTIFIER)</string>
</array>
```

Done!

## Usage 
### Login

Begins the login process. If the LINE app is installed, the SDK defaults to using app-to-app authentication. If it is not installed, the SDK logs in using a web.
The login result for LINE app is got by a callback function.

```dart
  // Begins the login process.
  await _flutterLineLogin.startLogin(_onLoginSuccess, _onLoginError);

  /// Success callback for LINE login result.
  ///
  /// The data is the map resulting from successful login with LINE login.
  ///
  /// Attributes from LineProfile & LineCredential
  ///
  /// * userID - The user's user ID.
  /// * displayName - The user’s display name.
  /// * pictureUrl - The user’s profile media URL.
  /// * statusMessage - The user’s status message.
  /// * accessToken - User access token.
  /// * expiresIn - The amount of time in milliseconds until the user access token expires.
  /// * permissions - The set of permissions that the access token holds. The following is a list of the permission codes.
  void _onLoginSuccess(Object data) {
    debugPrint("userID:${data['userID']}");
    debugPrint("displayName:${data['displayName']}");
    debugPrint("pictureUrl:${data['pictureUrl']}");
    debugPrint("statusMessage:${data['statusMessage']}");
    debugPrint("accessToken: ${data['accessToken']}.");
    debugPrint("expiresIn: ${data['expiresIn']}.");
  }

  /// Error callback for LINE login result.
  ///
  /// The error is the PlatformException resulting of failing login with LINE login.
  /// Attributes differs between Android and iOS.
  void _onLoginError(Object error) {
    debugPrint("PlatformException: ${error}");
  }
```

### Login with web

Begins the login process. This function uses a Web to log in, not app-to-app authentication.

```dart
  await _flutterLineLogin.startWebLogin(_onLoginSuccess, _onLoginError);
```

The login result is obtained by a callback function. It's same Login.

### Logout

Revokes the user access token.
If access token is null, PlatformException will be thrown.

```dart
  // Platform messages may fail, so we use a try/catch PlatformException.
  try {
    await _flutterLineLogin.logout();
    debugPrint("Logout success.");
  } on PlatformException catch (e) {
    debugPrint("PlatformException: ${e}");
  }
```

### Profile

Gets the profile information of the user.
If access token is null, PlatformException will be thrown.

```dart
  // Platform messages may fail, so we use a try/catch PlatformException.
  try {
    var profile = await _flutterLineLogin.getProfile();
    debugPrint("userID:${profile['userID']}");
    debugPrint("displayName:${profile['displayName']}");
    debugPrint("pictureUrl:${profile['pictureUrl']}");
    debugPrint("statusMessage:${profile['statusMessage']}");
  } on PlatformException catch (e) {
    debugPrint("PlatformException: ${e}");
  }
```

### Access Token

Gets the access token for the user.
If access token is null, PlatformException will be thrown.

```dart
  // Platform messages may fail, so we use a try/catch PlatformException.
  try {
    var result = await _flutterLineLogin.currentAccessToken();
    debugPrint("accessToken: ${result['accessToken']}.");
    debugPrint("expiresIn: ${result['expiresIn']}.");
  } on PlatformException catch (e) {
    debugPrint("PlatformException: ${e}");
  }
```

### Verify token

Checks whether the access token for the user is valid.
If access token is null, PlatformException is threw.

```dart
  // Platform messages may fail, so we use a try/catch PlatformException.
  try {
    var result = await _flutterLineLogin.verifyToken();
    // Return null only. That means it is successful.
    debugPrint("VerifyToken: ${result == null}.");
  } on PlatformException catch (e) {
    debugPrint("PlatformException: ${e}");
  }
```

### Refresh token

Refreshes the access token for the user.
If access token is null, PlatformException will be thrown.

```dart
  // Platform messages may fail, so we use a try/catch PlatformException.
  try {
    var result = await _flutterLineLogin.verifyToken();
    debugPrint("accessToken: ${result['accessToken']}.");
    debugPrint("expiresIn: ${result['expiresIn']}.");
  } on PlatformException catch (e) {
    debugPrint("PlatformException: ${e}");
  }
```

**LINE SDK has API difference between Android and iOS.**
So, I did not create all Naive API calling functions.
To learn more, you need to look at the [Android SDK reference](https://developers.line.me/en/reference/android-sdk/) and [iOS SDK reference](https://developers.line.me/en/reference/ios-sdk/) of LINE documentation.


If you find a bug please register issue or Pull Request.

Thank you and best regards,
