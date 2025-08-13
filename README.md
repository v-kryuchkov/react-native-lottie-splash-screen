# @attarchi/react-native-lottie-splash-screen

[![npm](https://img.shields.io/badge/npm-@attarchi/react--native--lottie--splash--screen-blue)](https://www.npmjs.com/package/@attarchi/react-native-lottie-splash-screen)
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-brightgreen.svg)](https://github.com/attarchi/react-native-lottie-splash-screen/pulls)
[![License MIT](https://img.shields.io/badge/license-MIT-orange.svg)](https://raw.githubusercontent.com/crazycodeboy/react-native-check-box/master/LICENSE)

Fork of [react-native-splash-screen](https://github.com/crazycodeboy/react-native-splash-screen) with animated splash screen using Airbnb Lottie. Works on iOS and Android.

### Acknowledgement

Huge thanks to the original authors and contributors of `react-native-lottie-splash-screen`. The original package became outdated and PRs/issues went unanswered. To keep it maintained and compatible with new React Native versions, this updated package is published under my namespace as `@attarchi/react-native-lottie-splash-screen`.

## Contents
- [Version Compatibilities](#versions-compatibilities)
- [Examples](#examples)
- [Installation - React Native Bare ≥ 0.77)](#installation-react-native--077)
- [Installation - Expo (Bare Workflow)](#expo-bare-workflow)
- [Usage](#usage)
- [API](#api)
- [Upgrade v2 → v3](#upgrade-v2--v3)
- [Contribution](#contribution)


## Versions Compatibilities
| React Native | react-native-lottie-splash-screen |
|---|---|
| >= 0.77 | 3.x |
| >= 0.70 & < 0.77 | [2.x](https://github.com/attarchi/react-native-lottie-splash-screen/tree/v2) |
| < 0.70 | [1.x](https://github.com/attarchi/react-native-lottie-splash-screen/tree/v2?tab=readme-ov-file#first-stepdownload) |
#### Warning: Version 3.x has no backward compatibility. You need to follow the [upgrade instructions](#upgrade-v2--v3).


## Examples
You can clone this project and run the examples with these commands:

```bash
yarn install

# Run react-native bare 79 example
yarn bare:install
yarn bare:ios
yarn bare:android

# Run the EXPO example
yarn expo:install
yarn expo:ios
yarn expo:android

```

## Installation (React Native ≥ 0.77)

Follow these steps in order.

### 1) Install packages

```bash
yarn add @attarchi/react-native-lottie-splash-screen lottie-react-native@7.3.1

cd ios && bundle install && bundle exec pod install
```

### 2) iOS setup

1. Add your Lottie JSON (e.g. `loading.json`) to the Xcode project and include it in the app target.
<details>
 <summary>How to add Lottie JSON to Xcode project</summary>
Drag your lottie files to Xcode Project. Click Finish. That's all.

![](screenshot/2022-07-09-16-40-46.png)
![](screenshot/2022-07-09-16-41-45.png)
</details>

2. Open `AppDelegate.swift` in the `ios` folder and add the setup call:

```swift
import UIKit
...
import SplashScreen // <- Add this line

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
  func application(...) -> Bool {
    ...

    // Before return, add this:
    // Setup Lottie splash screen using the SplashScreen module
    SplashScreen.setupLottieSplash(in: window, lottieName: "loading", backgroundColor: UIColor.white, forceToCloseByHideMethod: false)

    return true
  }
}

```

3. Remove the default iOS launch screen.

   By default, iOS displays the launch storyboard before your app is ready. To ensure a seamless transition to your Lottie splash, you should make the launch screen blank or match the first frame of your Lottie animation.

   To make it blank, open the `LaunchScreen.storyboard` file in the `ios` folder and remove the `<subviews>` section from the main `<view>`. This will prevent any default labels or images from appearing.

4. Build iOS once to verify.

### 3) Android setup

1. Create `android/app/src/main/res/layout/launch_screen.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/windowSplashScreenBackground">
  <com.airbnb.lottie.LottieAnimationView
      android:id="@+id/lottie"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      app:lottie_rawRes="@raw/loading"
      app:lottie_autoPlay="false"
      app:lottie_loop="false" />
</LinearLayout>
```

2. Place your Lottie JSON at `android/app/src/main/res/raw/loading.json`.
3. Ensure styles exist at `android/app/src/main/res/values/styles.xml`:

```xml
<resources>
  <style name="AppTheme" parent="Theme.AppCompat.DayNight.NoActionBar">
    <item name="android:editTextBackground">@drawable/rn_edit_text_material</item>
    <item name="android:statusBarColor">#ffffff</item>
    <!-- Add the below line: -->
    <item name="android:windowDisablePreview">true</item>
  </style>

  <!-- Also, copy these lines to you project. -->
  <style name="SplashScreen_SplashAnimation">
    <item name="android:windowExitAnimation">@android:anim/fade_out</item>
  </style>

  <style name="SplashScreen_SplashTheme" parent="Theme.AppCompat.NoActionBar">
    <item name="android:windowAnimationStyle">@style/SplashScreen_SplashAnimation</item>
    <item name="windowActionBarOverlay">false</item>
    <item name="android:windowTranslucentStatus">true</item>
  </style>
  <!-- End of copy -->
</resources>
```

4. Ensure color exists at `android/app/src/main/res/values/colors.xml`:

```xml
<resources>
  <color name="windowSplashScreenBackground">#ffffff</color>
</resources>
```

5. Update `MainActivity.kt`:

```kotlin
...
import com.facebook.react.defaults.DefaultReactActivityDelegate
import org.devio.rn.splashscreen.SplashScreen // <- Add this line
import android.os.Bundle

class MainActivity : ReactActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Add the below line at the bottom of the onCreate function: 
    SplashScreen.show(this, R.style.SplashScreen_SplashTheme, R.id.lottie, false)
  }

...
```

6. Build Android to verify.

## 

## Expo Bare Workflow

This package supports Expo Bare projects. It does not work in the `Expo Go` App.

Follow these steps.

### 1) Prebuild native projects
If you have a Expo project and you want a real lottie splash screen you have to eject your project to bare workflow with these commands:

```bash
npx expo prebuild -p android
npx expo prebuild -p ios
```

### 2) Install and configure

- Run the Installation steps above (packages, iOS and Android setup).
- Android manifest: set your `MainActivity` theme to `@style/AppTheme` (not Expo’s splash theme).
- In `MainActivity.kt`, ensure:

```kotlin
setTheme(R.style.AppTheme)
super.onCreate(null)
SplashScreen.show(this, R.style.SplashScreen_SplashTheme, R.id.lottie, false) // This line
```

- iOS: Add your Lottie JSON and call `SplashScreen.setupLottieSplash(...)` in `AppDelegate.swift` as shown above.

### 3) Run on devices/simulators

```bash
yarn android
yarn ios
```

Use these instead of `yarn start` to see the native splash overlay. See [this commit](https://github.com/attarchi/react-native-lottie-splash-screen/commit/b4b6e452f13011e0f42a4a5208ae426522f51717) for a working Expo example configuration.

## Usage

1. Import in your app entry and hide once the app is ready:

```js
import { useEffect } from "react";
import LottieSplashScreen from "@attarchi/react-native-lottie-splash-screen";

export default function App() {
  useEffect(() => {
    // Hide the splash screen when your app is ready.
    // The optional chaining (?.) is important for Expo projects.
    LottieSplashScreen?.hide();
  }, []);
  return null;
}
```

## API

| Method | Type     | Optional | Description |
| ------ | -------- | -------- | ----------- |
| hide() | function | false    | Closes the Lottie splash overlay |

## Upgrade v2 → v3 (React Native ≥ 0.77)

You can see all needed changes together in these commits:
- [iOS](https://github.com/attarchi/react-native-lottie-splash-screen/commit/da5308ac1b7c8311978584ac10ca326e2f137d3e)
- [Android](https://github.com/attarchi/react-native-lottie-splash-screen/commit/0f94960366e64aecc4b2d41e5b20ce4bc297bae7)

<details>
 <summary>Or follow this upgrade instruction:</summary>

1. Update packages:

```bash
yarn add @attarchi/react-native-lottie-splash-screen@^3 lottie-react-native@^7
cd ios && bundle exec pod install
```

2. iOS changes:
   - Remove any previous `Dynamic.swift` and bridging-header usage.
   - Add your Lottie JSON to the app target if not present.
   - Add following codes in `AppDelegate.swift`:

```swift
import SplashScreen
SplashScreen.setupLottieSplash(in: window, lottieName: "loading", backgroundColor: UIColor.white, forceToCloseByHideMethod: false)
```

3. Android changes:
   - Replace any `SplashScreen.show(this, R.style.SplashScreen_SplashTheme, R.id.lottie)` with:

```kotlin
SplashScreen.show(this, R.style.SplashScreen_SplashTheme, R.id.lottie, false)
```

   - Remove `SplashScreen.setAnimationFinished(true)` from `onCreate`.
   - Ensure `launch_screen.xml` uses `app:lottie_autoPlay="false"` and the layout/background/styles/colors from the Installation section.

4. JS:
   - Keep `LottieSplashScreen?.hide()` when your app is ready.
</details>

## Contribution

Issues and PRs are welcome. The fastest way to receive help is to include a minimal repro (you can base it on the examples in this repo).

---

**[MIT Licensed](https://github.com/attarchi/react-native-lottie-splash-screen/blob/main/LICENSE)**
