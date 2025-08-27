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
import android.os.Build
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


    fun show(activity: Activity?, themeResId: Int = R.style.SplashScreen_SplashTheme, lottieId: Int, forceToCloseByHideMethod: Boolean = false) {
        showInternal(activity, themeResId, lottieId, forceToCloseByHideMethod, false, -1.0, -1.0)
    }

    fun showWithDuration(activity: Activity?, themeResId: Int = R.style.SplashScreen_SplashTheme, lottieId: Int, forceToCloseByHideMethod: Boolean = false, loopAnimation: Boolean = false, minDuration: Double = -1.0, maxDuration: Double = -1.0) {
        showInternal(activity, themeResId, lottieId, forceToCloseByHideMethod, loopAnimation, minDuration, maxDuration)
    }

    fun showInternal(activity: Activity?, themeResId: Int = R.style.SplashScreen_SplashTheme, lottieId: Int, forceToCloseByHideMethod: Boolean = false, loopAnimation: Boolean = false, minDuration: Double = -1.0, maxDuration: Double = -1.0) {
        if (activity == null) {
            println("SplashScreen: ERROR - Activity is null")
            return
        }
        mActivity = WeakReference(activity)
        this.forceToCloseByHideMethod = forceToCloseByHideMethod
        this.isAnimationFinished = false
        this.currentLottieId = lottieId
        this.isLoopingAnimation = loopAnimation
        
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
    }
    
    @JvmStatic
    fun setForceToCloseByHideMethod(flag: Boolean) {
        forceToCloseByHideMethod = flag
    }
}
