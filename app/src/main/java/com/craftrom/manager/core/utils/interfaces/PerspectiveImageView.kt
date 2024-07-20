package com.craftrom.manager.core.utils.interfaces

import android.content.Context
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class PerspectiveImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), SensorEventListener {

    private val camera = Camera()
    private val matrix = Matrix()
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var previousRoll = 0f
    private var previousPitch = 0f

    init {
        camera.setLocation(0f, 0f, -8f)
    }

    fun setImageWithPerspective(url: String, placeholder: Int) {
        Picasso.get()
            .load(url)
            .placeholder(placeholder)
            .fit()
            .centerCrop()
            .into(this, object : Callback {
                override fun onSuccess() {
                    updatePerspective(previousRoll, previousPitch)
                    invalidate()
                }

                override fun onError(e: Exception?) {
                    e?.printStackTrace()
                }
            })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> gravity = it.values
                Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = it.values
            }

            if (gravity != null && geomagnetic != null) {
                val R = FloatArray(9)
                val I = FloatArray(9)
                if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(R, orientation)
                    val roll = orientation[2] * (180 / Math.PI).toFloat()
                    val pitch = orientation[1] * (180 / Math.PI).toFloat()
                    updatePerspective(roll, pitch)
                    invalidate()
                }
            }
        }
    }

    private fun updatePerspective(roll: Float, pitch: Float) {
        val alpha = 0.05f
        val rollScale = 0.02f
        val pitchScale = 0.02f

        val smoothedRoll = alpha * roll + (1 - alpha) * previousRoll
        val smoothedPitch = alpha * pitch + (1 - alpha) * previousPitch

        matrix.reset()
        camera.save()

        // Apply perspective
        camera.rotateY(-smoothedRoll * rollScale)
        camera.rotateX(smoothedPitch * pitchScale)
        camera.getMatrix(matrix)
        camera.restore()

        drawable?.let { drawable ->
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight

            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()

            // Calculate scale to fit the view while maintaining aspect ratio
            val scaleX = viewWidth / drawableWidth
            val scaleY = viewHeight / drawableHeight
            val scale = minOf(scaleX, scaleY)

            val offsetX = (viewWidth - drawableWidth * scale) / 2f
            val offsetY = (viewHeight - drawableHeight * scale) / 2f

            matrix.preScale(scale, scale)
            matrix.postTranslate(offsetX, offsetY)
        }

        previousRoll = smoothedRoll
        previousPitch = smoothedPitch
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(matrix)
        super.onDraw(canvas)
        canvas.restore()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
