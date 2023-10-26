package com.pluscubed.velociraptor.limit

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.utils.PrefUtils
import com.pluscubed.velociraptor.utils.Utils
import com.google.android.material.R as MaterialR
import java.util.*
import kotlin.math.abs
import kotlin.math.cos

class FloatingView(private val service: LimitService) : LimitView {
    private lateinit var limitView: View
    private var limitLabelText: TextView? = null
    private lateinit var limitText: TextView
    private lateinit var limitSourceText: TextView
    private lateinit var speedometerView: View
    private lateinit var arcView: ArcProgressView
    private lateinit var speedometerText: TextView
    private lateinit var speedometerUnitsText: TextView

    private val windowManager: WindowManager?
    private var style: Int = 0
    private lateinit var floatingView: View
    private var debuggingText: TextView? = null

    private val windowType: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

    init {
        windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        inflateMonitor()
        updatePrefs()
    }

    @SuppressLint("InflateParams")
    private fun inflateDebugging() {
        debuggingText =
                LayoutInflater.from(ContextThemeWrapper(service, R.style.Theme_Velociraptor_Light))
                        .inflate(R.layout.floating_stats, null, false) as TextView

        val debuggingParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        debuggingParams.gravity = Gravity.BOTTOM
        try {
            windowManager!!.addView(debuggingText, debuggingParams)
        } catch (_: Exception) {}
    }

    private fun inflateMonitor() {
        style = PrefUtils.getSignStyle(service)
        val layout = when (style) {
            PrefUtils.STYLE_US -> R.layout.floating_region_us
            PrefUtils.STYLE_INTERNATIONAL -> R.layout.floating_region_international
            else -> R.layout.floating_region_international
        }

        // System.err errors here are printed by the ArcProgressStackView library
        floatingView = LayoutInflater.from(ContextThemeWrapper(service, R.style.Theme_Velociraptor_Light))
                .inflate(layout, null, false)

        if (layout == R.layout.floating_region_us) {
            limitLabelText = floatingView.findViewById(R.id.limit_label_text)
        }

        limitView = floatingView.findViewById(R.id.limit)
        limitText = floatingView.findViewById(R.id.limit_text)
        limitSourceText = floatingView.findViewById(R.id.limit_source_text)
        speedometerView = floatingView.findViewById(R.id.speedometer)
        arcView = floatingView.findViewById(R.id.arcview)
        speedometerText = floatingView.findViewById(R.id.speed)
        speedometerUnitsText = floatingView.findViewById(R.id.speedUnits)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.alpha = PrefUtils.getOpacity(service) / 100.0f
        if (windowManager != null) {
            try {
                windowManager.addView(floatingView, params)
            } catch (_: Exception) {}
        }

        floatingView.setOnTouchListener(FloatingOnTouchListener())

        initMonitorPosition()

        val models = ArrayList<ArcProgressView.Model>()
        models.add(
            ArcProgressView.Model(
                "", 0f,
                ContextCompat.getColor(service, R.color.colorPrimary800),
                ContextCompat.getColor(service, R.color.colorAccent)
            )
        )
        arcView.textColor = ContextCompat.getColor(service, android.R.color.transparent)
        arcView.models = models
    }

    private fun initMonitorPosition() {
        floatingView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val params = floatingView.layoutParams as WindowManager.LayoutParams

                    val split = PrefUtils.getFloatingLocation(service)
                        .split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val left = java.lang.Boolean.parseBoolean(split[0])
                    val yRatio = java.lang.Float.parseFloat(split[1])

                    val screenSize = Point()
                    windowManager!!.defaultDisplay.getSize(screenSize)
                    params.x = if (left) 0 else screenSize.x - floatingView.width
                    params.y = (yRatio * screenSize.y + 0.5f).toInt()

                    try {
                        windowManager.updateViewLayout(floatingView, params)
                    } catch (ignore: IllegalArgumentException) {
                    }

                    floatingView.visibility = View.VISIBLE

                    floatingView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        )
    }

    override fun changeConfig() {
        initMonitorPosition()
    }

    override fun stop() {
        removeWindowView(floatingView)
        removeWindowView(debuggingText)
    }

    private fun removeWindowView(view: View?) {
        if (view != null && windowManager != null) {
            try {
                windowManager.removeView(view)
            } catch (ignore: IllegalArgumentException) {}
        }
    }

    private fun animateViewToSideSlot() {
        val screenSize = Point()
        windowManager!!.defaultDisplay.getSize(screenSize)

        val params = floatingView.layoutParams as WindowManager.LayoutParams
        val endX = if (params.x + floatingView.width / 2 >= screenSize.x / 2) {
            screenSize.x - floatingView.width
        } else {
            0
        }

        PrefUtils.setFloatingLocation(service, params.y.toFloat() / screenSize.y, endX == 0)

        val valueAnimator = ValueAnimator.ofInt(params.x, endX).setDuration(300)
        valueAnimator.interpolator = LinearOutSlowInInterpolator()
        valueAnimator.addUpdateListener { animation ->
            val params1 = floatingView.layoutParams as WindowManager.LayoutParams
            params1.x = animation.animatedValue as Int
            try {
                windowManager.updateViewLayout(floatingView, params1)
            } catch (ignore: IllegalArgumentException) {}
        }
        valueAnimator.start()
    }

    override fun setSpeed(speed: Int, percentOfWarning: Int) {
        if (PrefUtils.getShowSpeedometer(service)) {
            speedometerText.text = String.format("%d", speed)
            arcView.models[0].progress = percentOfWarning.toFloat()
            arcView.animateProgress()
        }
    }

    override fun setSpeeding(speeding: Boolean) {
        val redColor = ContextCompat.getColor(service, R.color.red500)

        val typedValue = TypedValue()
        speedometerText.context?.theme?.resolveAttribute(MaterialR.attr.colorOnBackground, typedValue, true)
        val textColor = speedometerText.context?.let {
            ContextCompat.getColor(it, typedValue.resourceId)
        } ?: Color.BLACK

        val color = if (speeding) redColor else textColor
        speedometerText.setTextColor(color)
        speedometerUnitsText.setTextColor(color)
    }

    override fun setDebuggingText(text: String) {
        if (debuggingText != null) {
            debuggingText?.text = text
        }
    }

    override fun setLimit(limit: String, source: String) {
        limitText.text = limit
        limitSourceText.text = source
    }

    override fun updatePrefs() {
        val prefStyle = PrefUtils.getSignStyle(service)
        if (prefStyle != style) {
            removeWindowView(floatingView)
            inflateMonitor()
        }
        style = prefStyle

        val debuggingEnabled = PrefUtils.isDebuggingEnabled(service)
        if (debuggingEnabled && debuggingText == null) {
            inflateDebugging()
        } else if (!debuggingEnabled && debuggingText != null) {
            removeWindowView(debuggingText)
            debuggingText = null
        }

        val speedometerShown = PrefUtils.getShowSpeedometer(service)
        speedometerView.visibility = if (speedometerShown) View.VISIBLE else View.GONE

        val limitShown = PrefUtils.getShowLimits(service)
        limitView.visibility = if (limitShown) View.VISIBLE else View.GONE

        speedometerUnitsText.text = Utils.getUnitText(service)

        val layoutParams = floatingView.layoutParams as WindowManager.LayoutParams
        layoutParams.alpha = PrefUtils.getOpacity(service) / 100f
        try {
            windowManager!!.updateViewLayout(floatingView, layoutParams)
        } catch (ignore: IllegalArgumentException) {
        }

        // TODO: Remove hard-coded base text sizes
        val speedLimitSize = PrefUtils.getSpeedLimitSize(service)
        var textSize = 0f
        var height = 0f
        var width = 0f

        when (style) {
            PrefUtils.STYLE_US -> {
                val cardSidePadding = (2 + (1 - cos(Math.toRadians(45.0))) * 2).toFloat()
                val cardTopBottomPadding = (2 * 1.5 + (1 - cos(Math.toRadians(45.0))) * 2).toFloat()

                width = speedLimitSize * 56
                height = speedLimitSize * 80

                width += cardSidePadding * 2
                height += cardTopBottomPadding * 2

                textSize = 28f
            }
            PrefUtils.STYLE_INTERNATIONAL -> {
                height = speedLimitSize * 64
                width = speedLimitSize * 64

                textSize = 0f
            }
        }

        val limitLayoutParams = limitView.layoutParams
        limitLayoutParams.width = Utils.convertDpToPx(service, width)
        limitLayoutParams.height = Utils.convertDpToPx(service, height)
        limitView.layoutParams = limitLayoutParams

        if (textSize != 0f) {
            textSize *= speedLimitSize
            limitText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
        }

        var labelTextSize = 12 * speedLimitSize
        limitLabelText?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, labelTextSize)

        val sourceTextSize = 8 * speedLimitSize
        limitSourceText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, sourceTextSize)

        val speedometerSize = PrefUtils.getSpeedometerSize(service)

        width = 64 * speedometerSize
        height = 64 * speedometerSize

        textSize = 24 * speedometerSize
        speedometerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)

        labelTextSize = 12 * speedometerSize
        speedometerUnitsText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, labelTextSize)

        val speedometerlayoutParams = speedometerView.layoutParams
        speedometerlayoutParams.width = Utils.convertDpToPx(service, width)
        speedometerlayoutParams.height = Utils.convertDpToPx(service, height)
        speedometerView.layoutParams = speedometerlayoutParams
    }

    override fun hideLimit(hideLimit: Boolean) {
        limitView.visibility = if (!PrefUtils.getShowLimits(service)) {
            View.GONE
        } else if (hideLimit) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

    private inner class FloatingOnTouchListener : View.OnTouchListener {
        private var mInitialTouchX: Float = 0.toFloat()
        private var mInitialTouchY: Float = 0.toFloat()
        private var mInitialX: Int = 0
        private var mInitialY: Int = 0
        private var mStartClickTime: Long = 0
        private var mIsClick: Boolean = false

        private var fadeAnimator: AnimatorSet? = null
        private var initialAlpha: Float = 0.toFloat()
        private val fadeOut: ValueAnimator
        private val fadeIn: ValueAnimator

        init {
            val params = floatingView.layoutParams as WindowManager.LayoutParams
            fadeOut = ValueAnimator.ofFloat(params.alpha, 0.1f)
            fadeOut.interpolator = FastOutSlowInInterpolator()
            fadeOut.duration = 100
            fadeOut.addUpdateListener { valueAnimator ->
                params.alpha = valueAnimator.animatedValue as Float
                try {
                    windowManager!!.updateViewLayout(floatingView, params)
                } catch (ignore: IllegalArgumentException) {
                }
            }
            fadeIn = fadeOut.clone()
            fadeIn.setFloatValues(0.1f, params.alpha)
            fadeIn.startDelay = 5000
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val params = floatingView.layoutParams as WindowManager.LayoutParams

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mInitialTouchX = event.rawX
                    mInitialTouchY = event.rawY

                    mInitialX = params.x
                    mInitialY = params.y

                    mStartClickTime = System.currentTimeMillis()

                    mIsClick = true
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dX = event.rawX - mInitialTouchX
                    val dY = event.rawY - mInitialTouchY
                    if (mIsClick && (abs(dX) > 10 || abs(dY) > 10) || System.currentTimeMillis() - mStartClickTime > ViewConfiguration.getLongPressTimeout()) {
                        mIsClick = false
                    }

                    if (!mIsClick) {
                        params.x = (dX + mInitialX).toInt()
                        params.y = (dY + mInitialY).toInt()

                        try {
                            windowManager!!.updateViewLayout(floatingView, params)
                        } catch (ignore: IllegalArgumentException) {}
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (mIsClick && System.currentTimeMillis() - mStartClickTime <= ViewConfiguration.getLongPressTimeout()) {
                        if (fadeAnimator != null && fadeAnimator!!.isStarted) {
                            fadeAnimator!!.cancel()
                            params.alpha = initialAlpha
                            try {
                                windowManager!!.updateViewLayout(floatingView, params)
                            } catch (ignore: IllegalArgumentException) {}
                        } else {
                            initialAlpha = params.alpha

                            fadeAnimator = AnimatorSet()
                            fadeAnimator!!.play(fadeOut).before(fadeIn)
                            fadeAnimator!!.start()
                        }
                    } else {
                        animateViewToSideSlot()
                    }
                    return true
                }
            }
            return false
        }
    }
}
