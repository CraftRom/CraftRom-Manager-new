package com.craftrom.manager.core.utils.hwinfo

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.Nullable
import com.craftrom.manager.R
import com.craftrom.manager.core.app.ServiceContext.context
import com.craftrom.manager.core.utils.Constants.TAG
import com.craftrom.manager.core.utils.Utils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.RandomAccessFile


open class BatteryInfo  {

    companion object {

        private val mBatChgCyc: String = context.getString(R.string.config_batChargeCycle)


        fun updateBatteryInfo(): Intent? {
            return context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        }

        fun voltage(): Int {
            return updateBatteryInfo()?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: -1
        }
        fun temp(): String {
            val temperature = updateBatteryInfo()?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: -1
            return Utils.tenthsToFixedString(temperature)
        }

        fun cycleCount(): String {
            val cycleCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                updateBatteryInfo()?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, 0) ?: -1
            } else {
                parseBatteryCycle(mBatChgCyc)
            }
            return cycleCount.toString()
        }

        private fun parseBatteryCycle(file: String): String {
            try {
                return readLine(file).toInt().toString() + " Cycles"
            } catch (ioe: IOException) {
                try {
                    return readLineWithRoot     (file).toInt().toString() + " Cycles"
                } catch (ioe :IOException) {
                Log.e(
                    TAG, "Cannot read battery cycle from "
                            + file, ioe
                )}
            } catch (nfe: NumberFormatException) {
                Log.e(
                    TAG, "Read a badly formatted battery cycle from "
                            + file, nfe
                )
            }

            return errorResult()
        }

        /**
         * Reads a line from the specified file.
         *
         * @param filename The file to read from.
         * @throws IOException If the file couldn't be read.
         */
        @Throws(IOException::class)
        private fun readLine(filename: String): Long {
            return BufferedReader(FileReader(File(filename))).use { it.readLine().toLong() }
        }
        /**
         * Reads a line from the specified file with root access.
         *
         * @param filename The file to read from.
         * @throws IOException If the file couldn't be read.
         */
        @Throws(IOException::class)
        fun readLineWithRoot(filename: String): Long {
                val process = Runtime.getRuntime().exec("su -c cat $filename")
                val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
                val line = bufferedReader.readLine()
                bufferedReader.close()

                return line?.toLong() ?: throw IOException("Failed to read line from file")
        }
        fun technology(): String {
            return updateBatteryInfo()?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: errorResult()
        }

        fun getHealth(): String {
            when (updateBatteryInfo()?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: -1) {
                BatteryManager.BATTERY_HEALTH_GOOD -> return "Good"
                BatteryManager.BATTERY_HEALTH_UNKNOWN -> return "Unknown"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> return "Overheated"
                BatteryManager.BATTERY_HEALTH_DEAD -> return "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> return "Over-voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> return "Failed"
                BatteryManager.BATTERY_HEALTH_COLD -> return "Cold"
            }
            return "failed to get health"
        }

        fun getStatus(): String {
            when (updateBatteryInfo()?.getIntExtra(BatteryManager.EXTRA_STATUS, 0) ?: -1) {
                BatteryManager.BATTERY_STATUS_UNKNOWN -> return "Unknown"
                BatteryManager.BATTERY_STATUS_CHARGING -> return "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> return "Discharging"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> return "Not-charging"
                BatteryManager.BATTERY_STATUS_FULL -> return "Full"
            }
            return "failed to get status"
        }

        fun getConnection(): String {
            when (updateBatteryInfo()?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: -1) {
                BatteryManager.BATTERY_PLUGGED_AC -> return "Ac Plugged"
                BatteryManager.BATTERY_PLUGGED_USB -> return "Usb Plugged"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> return "Wireless Plugged"
            }
            return "unknown connection"
        }

        fun getBatteryLevel(): Int {
            val level: Int = updateBatteryInfo()?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)!!
            val scale: Int = updateBatteryInfo()?.getIntExtra(BatteryManager.EXTRA_SCALE, 100)!!
                return level * 100 / scale
        }

        fun getSystemUptime(): String {
            val timeMilliSec = SystemClock.uptimeMillis()

            val sec = timeMilliSec / 1000
            val min = sec / 60
            val hour = min / 60
            val days = hour / 24

            if (days.toInt() == 0) {
                return (hour % 24).toString() + ":" + min % 60 + ":" + sec % 60
            }
            return days.toString() + "days, " + (hour % 24).toString() + ":" + min % 60 + ":" + sec % 60
        }

        private fun errorResult(): String = "n/a"
    }
}