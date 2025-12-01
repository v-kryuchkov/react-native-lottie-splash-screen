/**
 * SplashScreen
 * from：http://attarchi.github.io
 * Author: Attarchi
 * GitHub: https://github.com/attarchi
 * Email: attarchi@me.com
 * Swift version by: React Native Community
 */

package org.devio.rn.splashscreen

import android.animation.Animator
import android.app.Activity
import android.app.Dialog
import android.content.Context;
import android.os.Build
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Handler
import android.os.Looper
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import java.lang.ref.WeakReference

object SplashScreen {
    private var mSplashDialog: Dialog? = null
    private var mActivity: WeakReference<Activity>? = null
    private var isAnimationFinished = false
    private var waiting = false
    private var forceToCloseByHideMethod = false
    private var animationStartTime: Long = 0
    private var minAnimationDuration: Double? = null
    private var maxAnimationDuration: Double? = null
    private var currentLottieId: Int = 0
    private var isLoopingAnimation: Boolean = false
    private var vibrate: Boolean = false
    private var vibrationPattern: String? = null
    private var pauseDuration: Double = 0.25
    private var vibrateLoop: Boolean = true
    private var vibrationHandler: Handler? = null
    private var vibrationRunnable: Runnable? = null
    private var isVibrationActive: Boolean = false


    fun show(activity: Activity?, themeResId: Int = R.style.SplashScreen_SplashTheme, lottieId: Int, forceToCloseByHideMethod: Boolean = false, vibrate: Boolean = false) {
        showInternal(activity, themeResId, lottieId, forceToCloseByHideMethod, false, -1.0, -1.0, vibrate, null, 0.25, true)
    }

    fun showWithDuration(activity: Activity?, themeResId: Int = R.style.SplashScreen_SplashTheme, lottieId: Int, forceToCloseByHideMethod: Boolean = false, loopAnimation: Boolean = false, minDuration: Double = -1.0, maxDuration: Double = -1.0, vibrate: Boolean = false, vibrationPattern: String? = null, pauseDuration: Double = 0.25, vibrateLoop: Boolean = true) {
        showInternal(activity, themeResId, lottieId, forceToCloseByHideMethod, loopAnimation, minDuration, maxDuration, vibrate, vibrationPattern, pauseDuration, vibrateLoop)
    }

    fun showInternal(activity: Activity?, themeResId: Int = R.style.SplashScreen_SplashTheme, lottieId: Int, forceToCloseByHideMethod: Boolean = false, loopAnimation: Boolean = false, minDuration: Double = -1.0, maxDuration: Double = -1.0, vibrate: Boolean = false, vibrationPattern: String? = null, pauseDuration: Double = 0.25, vibrateLoop: Boolean = true) {
        if (activity == null) {
            println("SplashScreen: ERROR - Activity is null")
            return
        }
        mActivity = WeakReference(activity)
        this.forceToCloseByHideMethod = forceToCloseByHideMethod
        this.isAnimationFinished = false
        this.currentLottieId = lottieId
        this.isLoopingAnimation = loopAnimation
        this.vibrate = vibrate
        this.vibrationPattern = vibrationPattern
        this.pauseDuration = pauseDuration
        this.vibrateLoop = vibrateLoop

        // Store duration values
        this.minAnimationDuration = if (minDuration > 0) minDuration else null
        this.maxAnimationDuration = if (maxDuration > 0) maxDuration else null

        activity.runOnUiThread {
            if (!activity.isFinishing) {
                mSplashDialog = Dialog(activity, themeResId)
                mSplashDialog?.setContentView(R.layout.launch_screen)
                mSplashDialog?.setCancelable(false)
                val lottie = mSplashDialog?.findViewById<LottieAnimationView>(lottieId)

                // Configure Lottie animation repeat behavior
                // These settings will override any XML configuration
                if (loopAnimation) {
                    lottie?.repeatCount = LottieDrawable.INFINITE
                } else {
                    lottie?.repeatCount = 0
                }
                lottie?.speed = 1.0f

                // Ensure animation is stopped before starting programmatically
                lottie?.cancelAnimation()
                lottie?.progress = 0f

                lottie?.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        println("SplashScreen: Animation started")
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        setAnimationFinished(true)
                        checkAndHideSplashScreen()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        println("SplashScreen: Animation was cancelled")
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        println("SplashScreen: Animation repeated (this shouldn't happen)")
                    }
                })

                // Set up max duration timeout if specified
                maxAnimationDuration?.let { maxDur ->
                    lottie?.postDelayed({
                        if (!isAnimationFinished) {
                            setAnimationFinished(true)
                            hideSplashScreen()
                        }
                    }, (maxDur * 1000).toLong())
                }

                // Start the animation manually after a small delay
                lottie?.postDelayed({
                    animationStartTime = System.currentTimeMillis()
                    lottie?.playAnimation()
                }, 100)

                // Start vibration loop if enabled
                startVibrationLoop(activity)

                if (mSplashDialog?.isShowing == false) {
                    mSplashDialog?.show()
                }
            }
        }
    }

    @JvmStatic
    fun setAnimationFinished(flag: Boolean) {
        isAnimationFinished = flag
    }

    private fun checkAndHideSplashScreen() {
        val animationDuration = (System.currentTimeMillis() - animationStartTime) / 1000.0

        val shouldHideImmediately = if (minAnimationDuration == null) {
            animationDuration > 0.5 // Original logic
        } else {
            animationDuration >= minAnimationDuration!! // Custom logic
        }

        if (shouldHideImmediately) {
            hideSplashScreen()
        } else {
            val targetDuration = minAnimationDuration ?: 2.0
            val waitTime = targetDuration - animationDuration
            mSplashDialog?.findViewById<LottieAnimationView>(currentLottieId)?.postDelayed({
                hideSplashScreen()
            }, (waitTime * 1000).toLong())
        }
    }

    fun hide(activity: Activity?) {
        // Only hide if forceToCloseByHideMethod is true
        if (forceToCloseByHideMethod) {
            hideSplashScreen()
            return
        }
        // Hide splash screen if:
        // 1. minAnimationDuration is set (custom timing), OR
        // 2. loopAnimation is enabled (infinite loop needs manual control)
        // Otherwise, let animation finish naturally
        if (forceToCloseByHideMethod || minAnimationDuration != null || isLoopingAnimation) {
            checkAndHideSplashScreen()
            return
        }
    }

    private fun hideSplashScreen() {
        var _activity = mActivity?.get()
        if (_activity == null) {
            return
        }

        // Stop vibration loop when hiding splash screen
        stopVibrationLoop()

        Handler(Looper.getMainLooper()).postDelayed({
            _activity.runOnUiThread {
              if (mSplashDialog != null && mSplashDialog?.isShowing == true) {
                var isDestroyed = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                  isDestroyed = _activity.isDestroyed
                }

                if (!_activity.isFinishing && !isDestroyed) {
                  mSplashDialog?.dismiss()
                  mSplashDialog = null
                  waiting = true
                }
              }
            }
        }, 300)
    }

    @JvmStatic
    fun setForceToCloseByHideMethod(flag: Boolean) {
        forceToCloseByHideMethod = flag
    }

    private fun startVibrationLoop(activity: Activity?) {
        if (!vibrate || activity == null) {
            return
        }

        val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator == null || !vibrator.hasVibrator()) {
            return
        }

        isVibrationActive = true
        vibrationHandler = Handler(Looper.getMainLooper())

        vibrationRunnable = object : Runnable {
            override fun run() {
                if (!isVibrationActive) {
                    return
                }

                if (vibrationPattern != null && vibrationPattern!!.isNotEmpty()) {
                    // Parse pattern string (e.g., "..o" -> [0, 25, 25, 25, 50, 100])
                    val pattern = parseVibrationPattern(vibrationPattern!!)
                    if (pattern.isNotEmpty()) {
                        if (Build.VERSION.SDK_INT >= 26) {
                            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(pattern, -1)
                        }
                    }
                } else {
                    // Default vibration if no pattern specified
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(200)
                    }
                }

                if (vibrateLoop && isVibrationActive) {
                    vibrationHandler?.postDelayed(this, (pauseDuration * 1000).toLong())
                }
            }
        }

        // Start vibration immediately
        vibrationHandler?.post(vibrationRunnable!!)
    }

    private fun stopVibrationLoop() {
        isVibrationActive = false
        vibrationRunnable?.let {
            vibrationHandler?.removeCallbacks(it)
        }
        vibrationRunnable = null
        vibrationHandler = null

        // Cancel any ongoing vibration
        val activity = mActivity?.get()
        if (activity != null) {
            val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.cancel()
        }
    }

    private fun parseVibrationPattern(pattern: String): LongArray {
        // Parse pattern string in Haptica format (same as iOS)
        // Format according to Haptica documentation:
        // "." = Light impact
        // "o" = Medium impact
        // "O" = Heavy impact
        // "x" = Soft impact
        // "X" = Rigid impact
        // "-" = Pause (duration from pauseDuration variable)
        // Android format: [initial_delay, vibrate1, pause1, vibrate2, pause2, ...]
        // First element is initial delay (0), then alternates vibrate/pause
        //
        // How pauses are determined:
        // - After index 0 (delay), elements alternate: vibrate (odd index) -> pause (even index)
        // - If result.size % 2 == 0, last element is a pause (can extend)
        // - If result.size % 2 == 1, last element is a vibration (need to add pause)

        if (pattern.isEmpty()) {
            return longArrayOf(0, 200) // Default pattern
        }

        val pauseDurationMs = (pauseDuration * 1000).toLong() // Convert seconds to milliseconds

        val result = mutableListOf<Long>()
        result.add(0) // Initial delay (index 0)

        var i = 0
        while (i < pattern.length) {
            val char = pattern[i]

            when (char) {
                '.', 'o', 'O', 'x', 'X' -> {
                    // Haptic feedback symbols - add vibration duration
                    val vibrationDuration = when (char) {
                        '.' -> 25L  // Light impact
                        'o' -> 50L  // Medium impact
                        'O' -> 100L // Heavy impact
                        'x' -> 30L  // Soft impact
                        'X' -> 80L  // Rigid impact
                        else -> 50L // Default
                    }

                    // Add vibration
                    result.add(vibrationDuration)

                    // Check if next character is also a vibration (not pause)
                    // Only add minimal pause if there's no explicit pause in pattern
                    val nextChar = if (i < pattern.length - 1) pattern[i + 1] else null
                    if (nextChar != null && nextChar != '-') {
                        // Next is a vibration without explicit pause, add minimal pause
                        result.add(10) // Minimal pause between consecutive vibrations
                    }
                    // If next is '-', it will add pause itself, so we don't add minimal pause

                    i++
                }
                '-' -> {
                    // Explicit pause - use pauseDuration variable
                    // Pause can only be added after a vibration
                    if (result.size > 1 && result.size % 2 == 1) {
                        // Last element is a vibration (odd index), add pause after it
                        result.add(pauseDurationMs)
                    } else if (result.size > 1 && result.size % 2 == 0) {
                        // Last element is already a pause (even index), extend it
                        val lastIndex = result.size - 1
                        result[lastIndex] += pauseDurationMs
                    }
                    i++
                }
                else -> {
                    // Unknown character (including spaces), skip it
                    i++
                }
            }
        }

        // Ensure we have at least one vibration
        if (result.size <= 1) {
            return longArrayOf(0, 200) // Default pattern
        }

        return result.toLongArray()
    }
}

