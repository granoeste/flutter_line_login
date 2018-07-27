# flutter_line_login

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

For more detailed code you need to see an [example](https://github.com/granoeste/flutter_line_login/tree/master/example).

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