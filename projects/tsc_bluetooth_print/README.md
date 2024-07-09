# tsc_bluetooth_print
**Goal**: A Flutter plugin to manage TSC bluetooth print.

## Change the minSdkVersion for Android
This plugin is compatible only from version 23 of Android SDK so you should change this in `android/app/build.gradle`:
```xml
    Android {
        defaultConfig {
            minSdkVersion: 23
```

## Add permissions for Bluetooth
We need to add the permission to use Bluetooth and access location:

### Android
In the `android/app/src/main/AndroidManifest.xml` let’s add:
```xml
        <uses-permission android:name="android.permission.BLUETOOTH" />  
        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />  
        <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
        <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <application
```

## Dependencies
* Linter
  * [flutter_lints][dependency_flutter_lints]

## Troubleshooting

### No device available during the compilation and execution steps
* If none of device is present (*Available Virtual Devices* or *Connected Devices*),
    * Either select `Create a new virtual device`
    * or connect and select your phone or tablet

## Useful
* [Download Android Studio][useful_android_studio]
* [Create a new virtual device][useful_virtual_device]
* [Enable developer options and debugging][useful_developer_options]

[dependency_flutter_lints]: https://pub.dev/packages/flutter_lints
[useful_android_studio]: https://developer.android.com/studio
[useful_virtual_device]: https://developer.android.com/studio/run/managing-avds.html
[useful_developer_options]: https://developer.android.com/studio/debug/dev-options.html#enable
