package com.example.maadminiproject.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Device
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

/**
 * Custom View for displaying floor plan layouts with an abstract grid overlay
 * and dynamic device markers positioned by normalized (x, y) coordinates.
 */
class FloorPlanOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val backgroundImageView = ShapeableImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        scaleType = ImageView.ScaleType.FIT_CENTER
        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCorners(CornerFamily.ROUNDED, dpToPx(16f))
            .build()
    }

    private val dimOverlayView = View(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        // Background color logic will be handled in onDraw or via bounds to avoid dimming the empty space
        visibility = View.GONE 
    }

    private val gridOverlayView = object : View(context) {
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4400F0FF")
            strokeWidth = dpToPx(1f)
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(dpToPx(4f), dpToPx(4f)), 0f)
        }

        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8800F0FF")
            textSize = dpToPx(9f)
            textAlign = Paint.Align.CENTER
        }

        private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#77051424")
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            
            val bounds = getImageBounds()
            if (bounds.width() <= 0 || bounds.height() <= 0) return

            // 1. Draw the dim overlay only over the image area
            canvas.drawRect(bounds, dimPaint)

            if (!isGridVisible) return

            val w = bounds.width()
            val h = bounds.height()
            val offsetLeft = bounds.left
            val offsetTop = bounds.top

            val cols = 10
            val rows = 8

            // Draw vertical grid lines & column labels
            for (i in 0..cols) {
                val x = offsetLeft + (w / cols) * i
                canvas.drawLine(x, offsetTop, x, offsetTop + h, gridPaint)
                if (i < cols) {
                    val label = ('A' + i).toString()
                    canvas.drawText(label, x + (w / cols) / 2f, offsetTop + dpToPx(12f), textPaint)
                }
            }

            // Draw horizontal grid lines & row labels
            for (j in 0..rows) {
                val y = offsetTop + (h / rows) * j
                canvas.drawLine(offsetLeft, y, offsetLeft + w, y, gridPaint)
                if (j < rows) {
                    val label = (j + 1).toString()
                    canvas.drawText(label, offsetLeft + dpToPx(10f), y + (h / rows) / 2f + dpToPx(3f), textPaint)
                }
            }
        }
    }.apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    private val markersContainer = FrameLayout(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    private var isGridVisible: Boolean = false
    private var devices: List<Device> = emptyList()
    private var onDeviceClickListener: ((Device) -> Unit)? = null

    init {
        setWillNotDraw(false)
        addView(backgroundImageView)
        addView(dimOverlayView)
        addView(gridOverlayView)
        addView(markersContainer)
    }

    /**
     * Sets the floor plan background image based on a resource identifier name or fallback.
     */
    fun setFloorPlan(planName: String?) {
        val resId = when (planName?.lowercase()) {
            "first_floor_map", "first_floor", "floor2" -> R.drawable.first_floor_map
            "ground_floor", "studio", "modern_studio" -> R.drawable.ground_floor
            "first_floor_photo" -> R.drawable.first_floor
            "ground_floor_map", "ground_floor_plan", "floor1" -> R.drawable.ground_floor_map
            else -> R.drawable.ground_floor_map
        }
        backgroundImageView.setImageResource(resId)
    }

    /**
     * Toggles visibility of the 10x8 abstract grid overlay.
     */
    fun setGridEnabled(enabled: Boolean) {
        isGridVisible = enabled
        gridOverlayView.invalidate()
    }

    /**
     * Sets the device list and plots dynamic markers based on device.x and device.y.
     */
    fun setDevices(newDevices: List<Device>) {
        this.devices = newDevices
        post {
            renderDeviceMarkers()
        }
    }

    /**
     * Registers a callback for device marker clicks.
     */
    fun setOnDeviceClickListener(listener: (Device) -> Unit) {
        this.onDeviceClickListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        post {
            renderDeviceMarkers()
        }
    }

    private fun renderDeviceMarkers() {
        markersContainer.removeAllViews()
        val bounds = getImageBounds()
        
        val imageWidth = bounds.width()
        val imageHeight = bounds.height()

        if (imageWidth <= 0 || imageHeight <= 0) return

        for (device in devices) {
            addMarkerAt(device.x, device.y, device, bounds)
        }
    }

    private fun addMarkerAt(x: Double, y: Double, device: Device, bounds: RectF) {
        val imageWidth = bounds.width()
        val imageHeight = bounds.height()

        val clampedX = x.coerceIn(0.0, 1.0)
        val clampedY = y.coerceIn(0.0, 1.0)

        val pxX = bounds.left + (clampedX * imageWidth).toInt()
        val pxY = bounds.top + (clampedY * imageHeight).toInt()

        val markerSize = dpToPx(36f).toInt()
        val markerView = createMarkerView(device)

        val params = LayoutParams(markerSize, markerSize).apply {
            leftMargin = (pxX - (markerSize / 2)).toInt()
            topMargin = (pxY - (markerSize / 2)).toInt()
        }

        markerView.setOnClickListener {
            onDeviceClickListener?.invoke(device)
        }

        markersContainer.addView(markerView, params)
    }

    /**
     * Calculates the actual bounds of the centered floor plan image within the view.
     */
    private fun getImageBounds(): RectF {
        val drawable = backgroundImageView.drawable ?: return RectF(0f, 0f, width.toFloat(), height.toFloat())
        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        if (imageWidth <= 0 || imageHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) {
            return RectF(0f, 0f, viewWidth, viewHeight)
        }

        val scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight)
        val finalWidth = imageWidth * scale
        val finalHeight = imageHeight * scale
        val left = (viewWidth - finalWidth) / 2f
        val top = (viewHeight - finalHeight) / 2f

        return RectF(left, top, left + finalWidth, top + finalHeight)
    }

    private fun createMarkerView(device: Device): View {
        val frame = FrameLayout(context)

        // Outer glow/circle
        val outerCircle = View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            val bg = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                if (device.state) {
                    if (device.type == "IRON") {
                        setColor(Color.parseColor("#44FF5252"))
                        setStroke(dpToPx(2f).toInt(), Color.parseColor("#FF5252"))
                    } else {
                        setColor(Color.parseColor("#4400F0FF"))
                        setStroke(dpToPx(2f).toInt(), Color.parseColor("#00F0FF"))
                    }
                } else {
                    setColor(Color.parseColor("#660D1C2D"))
                    setStroke(dpToPx(1.5f).toInt(), Color.parseColor("#55B0BEC5"))
                }
            }
            background = bg
        }

        // Inner icon
        val iconView = ImageView(context).apply {
            val iconSize = dpToPx(18f).toInt()
            layoutParams = LayoutParams(iconSize, iconSize, Gravity.CENTER)
            val iconRes = resolveDeviceIcon(device.type)
            setImageResource(iconRes)
            val iconTint = if (device.state) {
                if (device.type == "IRON") Color.parseColor("#FF5252") else Color.parseColor("#00F0FF")
            } else {
                Color.parseColor("#B0BEC5")
            }
            setColorFilter(iconTint)
        }

        frame.addView(outerCircle)
        frame.addView(iconView)
        return frame
    }

    private fun resolveDeviceIcon(type: String): Int {
        return when (type.uppercase()) {
            "LIGHT" -> R.drawable.ic_lightbulb
            "FAN" -> R.drawable.ic_fan
            "CAMERA" -> R.drawable.ic_camera
            "AIR_CONDITIONER", "AC" -> R.drawable.ic_ac
            "SMART_PLUG", "PLUG" -> R.drawable.ic_power_plug
            "IRON" -> R.drawable.ic_bolt
            "MULTI_SWITCH" -> R.drawable.ic_tune
            else -> R.drawable.ic_bolt
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
