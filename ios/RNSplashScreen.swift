/**
 * SplashScreen
 * from：http://attarchi.github.io
 * Author: Attarchi
 * GitHub: https://github.com/attarchi
 * Email: attarchi@me.com
 * Swift version by: React Native Community
 */

import UIKit
import React
import Lottie
import Haptica

@objc(SplashScreen)
public class SplashScreen: NSObject, RCTBridgeModule {

    // MARK: - Static Properties
    private static var forceToCloseByHideMethod = false
    private static var loopAnimation = false
    private static var loadingView: UIView?
    private static var isAnimationFinished = false
    private static var window: UIWindow?
    private static var animationStartTime: CFTimeInterval = 0
    private static var minAnimationDuration: TimeInterval?
    private static var maxAnimationDuration: TimeInterval?
    private static var vibrate = false
    private static var vibrationPattern: String?
    private static var hapticTimer: Timer?
    private static var pauseDuration: TimeInterval = 0.25
    private static var vibrateLoop = false
    private static var isHapticActive = false

    // MARK: - RCTBridgeModule
    public static func moduleName() -> String! {
        return "SplashScreen"
    }

    public func methodQueue() -> DispatchQueue! {
        return DispatchQueue.main
    }

    @objc public static func setupLottieSplash(in window: UIWindow?, lottieName: String, backgroundColor: UIColor = UIColor.white, forceToCloseByHideMethod: Bool = false, vibrate: Bool = false, vibrationPattern: String? = nil, pauseDuration: TimeInterval = 0.25, vibrateLoop: Bool = true) {
        setupLottieSplashInternal(in: window, lottieName: lottieName, backgroundColor: backgroundColor, forceToCloseByHideMethod: forceToCloseByHideMethod, loopAnimation: false, minAnimationDuration: -1, maxAnimationDuration: -1, vibrate: vibrate, vibrationPattern: vibrationPattern, pauseDuration: pauseDuration, vibrateLoop: vibrateLoop)
    }

    @objc public static func setupLottieSplashWithDuration(in window: UIWindow?, lottieName: String, backgroundColor: UIColor = UIColor.white, forceToCloseByHideMethod: Bool = false, loopAnimation: Bool = false, minAnimationDuration: TimeInterval, maxAnimationDuration: TimeInterval, vibrate: Bool = false, vibrationPattern: String? = nil, pauseDuration: TimeInterval = 0.25, vibrateLoop: Bool = true) {
        setupLottieSplashInternal(in: window, lottieName: lottieName, backgroundColor: backgroundColor, forceToCloseByHideMethod: forceToCloseByHideMethod, loopAnimation: loopAnimation, minAnimationDuration: minAnimationDuration, maxAnimationDuration: maxAnimationDuration, vibrate: vibrate, vibrationPattern: vibrationPattern, pauseDuration: pauseDuration, vibrateLoop: vibrateLoop)
    }

    @objc public static func setupLottieSplashInternal(in window: UIWindow?, lottieName: String, backgroundColor: UIColor = UIColor.white, forceToCloseByHideMethod: Bool = false, loopAnimation: Bool = false, minAnimationDuration: TimeInterval, maxAnimationDuration: TimeInterval, vibrate: Bool = false, vibrationPattern: String? = nil, pauseDuration: TimeInterval = 0.25, vibrateLoop: Bool = true) {
        guard let rootViewController = window?.rootViewController,
              let rootView = rootViewController.view else { return }

        self.window = window
        self.forceToCloseByHideMethod = forceToCloseByHideMethod
        self.loopAnimation = loopAnimation
        self.minAnimationDuration = minAnimationDuration > 0 ? minAnimationDuration : nil
        self.maxAnimationDuration = maxAnimationDuration > 0 ? maxAnimationDuration : nil
        self.isAnimationFinished = false
        self.vibrate = vibrate
        self.vibrationPattern = vibrationPattern
        self.pauseDuration = pauseDuration
        self.vibrateLoop = vibrateLoop

        rootView.backgroundColor = backgroundColor

        let animationView = LottieAnimationView(name: lottieName)
        animationView.frame = rootView.frame
        animationView.center = rootView.center
        animationView.backgroundColor = backgroundColor
        if loopAnimation {
            animationView.loopMode = .loop
        }

        showLottieSplash(animationView, inRootView: rootView)
    }

    @objc public static func setupCustomLottieSplash(in window: UIWindow?,  animationView: UIView, inRootView rootView: UIView, forceToCloseByHideMethod: Bool = false) {
        self.window = window
        self.forceToCloseByHideMethod = forceToCloseByHideMethod
        self.isAnimationFinished = false

        showLottieSplash(animationView, inRootView: rootView)
    }

    @objc private static func showLottieSplash(_ animationView: UIView, inRootView rootView: UIView) {
        loadingView = animationView
        isAnimationFinished = false
        animationStartTime = CACurrentMediaTime()

        startHapticLoop()

        // Ensure splash screen appears on top of React Native screen
        rootView.addSubview(animationView)
        rootView.bringSubviewToFront(animationView)

        // Set higher z-index to ensure splash screen stays on top
        animationView.layer.zPosition = 1000

        // Temporarily raise the window level to ensure it stays on top
        let originalWindowLevel = window?.windowLevel
        window?.windowLevel = UIWindow.Level.alert + 1

        // Set up max duration timeout if specified
        if let maxDuration = maxAnimationDuration {
            DispatchQueue.main.asyncAfter(deadline: .now() + maxDuration) {
                if !isAnimationFinished {
                    isAnimationFinished = true
                    checkAndHideSplashScreen()
                }
            }
        }

        // Play animation and handle completion
        if let lottieView = animationView as? LottieAnimationView {
            lottieView.play { finished in
                DispatchQueue.main.async {
                    isAnimationFinished = true
                    checkAndHideSplashScreen()
                }
            }
        } else {
            // Fallback for non-Lottie views
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                isAnimationFinished = true
                checkAndHideSplashScreen()
            }
        }
    }

    private static func checkAndHideSplashScreen() {
        guard let minDuration = minAnimationDuration else {
            // No minimum duration set, hide immediately
            hideSplashScreen()
            return
        }

        let currentTime = CACurrentMediaTime()
        let elapsedTime = currentTime - animationStartTime

        if elapsedTime >= minDuration {
            // Minimum time has passed, hide immediately
            hideSplashScreen()
        } else {
            // Wait for remaining time to reach minimum duration
            let remainingTime = minDuration - elapsedTime
            DispatchQueue.main.asyncAfter(deadline: .now() + remainingTime) {
                hideSplashScreen()
            }
        }
    }


    @objc public static func hide() {
        // Only hide if forceToCloseByHideMethod is true
        if forceToCloseByHideMethod {
            hideSplashScreen()
            return
        }
        // Hide splash screen if:
        // 1. minAnimationDuration is set (custom timing), OR
        // 2. loopAnimation is enabled (infinite loop needs manual control)
        // Otherwise, let animation finish naturally
        if minAnimationDuration != nil || loopAnimation {
            checkAndHideSplashScreen()
            return
        }
    }

    private static func hideSplashScreen() {
        guard let loadingView = loadingView else { return }

        // Stop haptic immediately when hiding splash screen
        stopHapticLoop()

        DispatchQueue.main.async {
            UIView.animate(withDuration: 0.2, animations: {
                loadingView.alpha = 0.0
            }, completion: { _ in
                loadingView.removeFromSuperview()
                self.loadingView = nil

                // Restore original window level
                window?.windowLevel = UIWindow.Level.normal
            })
        }

    }

    private static func startHapticLoop() {
        guard vibrate, let pattern = vibrationPattern else { return }

        isHapticActive = true

        if vibrate == true {
          hapticTimer = Timer.scheduledTimer(withTimeInterval: 0, repeats: vibrateLoop) { _ in
            guard isHapticActive else { return }
            Haptic.play(pattern, delay: pauseDuration)
          }

          RunLoop.main.add(hapticTimer!, forMode: .common)
        }
    }

    private static func stopHapticLoop() {
        isHapticActive = false
        hapticTimer?.invalidate()
        hapticTimer = nil
        // Cancel all haptic operations in the queue to stop current vibration
        Haptic.queue.cancelAllOperations()
    }
    
    
    @objc public static func jsLoadError(_ notification: Notification) {
        // If there was an error loading javascript, hide the splash screen so it can be shown.  Otherwise
        // the splash screen will remain forever, which is a hassle to debug.
        hideSplashScreen()
    }
    
    // MARK: - Bridge Method
    @objc public func hide() {
        SplashScreen.hide()
    }
}

// MARK: - Module Registration
extension SplashScreen {
    @objc public static func requiresMainQueueSetup() -> Bool {
        return true
    }
}
