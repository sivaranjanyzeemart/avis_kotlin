package zeemart.asia.buyers.helper.camera

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Created by Muthumari on 6/04/2020.
 */
class ShakeDetector : SensorEventListener {
    private var mListener: OnShakeListener? = null
    private var mShakeTimestamp: Long = 0
    private val mShakeCount = 0
    fun setOnShakeListener(listener: OnShakeListener?) {
        mListener = listener
    }

    interface OnShakeListener {
        fun onShake()
        fun onNoShake()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // ignore
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (mListener != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH
            val now = System.currentTimeMillis()

            // gForce will be close to 1 when there is no movement.
            val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                // ignore shake events too close to each other
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return
                }
                mShakeTimestamp = now
                mListener!!.onShake()
            } else {
                // reset the shake count after 5 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mListener!!.onNoShake()
                }
            }
        }
    }

    companion object {
        /*
     * The gForce that is necessary to register as shake.
     * Must be greater than 1G (one earth gravity unit).
     * You can install "G-Force", by Blake La Pierre
     * from the Google Play Store and run it to see how
     *  many G's it takes to register a shake
     */
        private const val SHAKE_THRESHOLD_GRAVITY = 1.2f
        private const val SHAKE_SLOP_TIME_MS = 100
        private const val SHAKE_COUNT_RESET_TIME_MS = 2000
    }
}