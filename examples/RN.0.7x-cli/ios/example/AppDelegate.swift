import UIKit
import React
import React_RCTAppDelegate
import ReactAppDependencyProvider

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
  var window: UIWindow?

  var reactNativeDelegate: ReactNativeDelegate?
  var reactNativeFactory: RCTReactNativeFactory?

  func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
  ) -> Bool {
    let delegate = ReactNativeDelegate()
    let factory = RCTReactNativeFactory(delegate: delegate)
    delegate.dependencyProvider = RCTAppDependencyProvider()

    reactNativeDelegate = delegate
    reactNativeFactory = factory

    window = UIWindow(frame: UIScreen.main.bounds)

    factory.startReactNative(
      withModuleName: "example",
      in: window,
      launchOptions: launchOptions
    )

    // RNSplashScreen integration
    if let rootViewController = window?.rootViewController,
       let rootView = rootViewController.view {
      
      rootView.backgroundColor = UIColor.white // change with your desired backgroundColor

      let dynamic = Dynamic()
      let animationUIView = dynamic.createAnimationView(rootView: rootView, lottieName: "loading") // change lottieName to your lottie files name

      // register LottieSplashScreen to RNSplashScreen
      RNSplashScreen.showLottieSplash(animationUIView, inRootView: rootView)
      
      // play animation
      dynamic.play(animationView: animationUIView)
      
      // Skip waiting for the Lottie animation to finish
      RNSplashScreen.setAnimationFinished(true)
    }

    return true
  }
}

class ReactNativeDelegate: RCTDefaultReactNativeFactoryDelegate {
  override func sourceURL(for bridge: RCTBridge) -> URL? {
    self.bundleURL()
  }

  override func bundleURL() -> URL? {
#if DEBUG
    RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index")
#else
    Bundle.main.url(forResource: "main", withExtension: "jsbundle")
#endif
  }
}
