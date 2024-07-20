package com.craftrom.manager.core.utils.hwinfo

import android.os.Build
import android.util.Log
import com.craftrom.manager.core.utils.Constants.TAG
import java.io.RandomAccessFile


open class CPUInfo  {

    companion object {

        private const val CPU_INFO_DIR = "/sys/devices/system/cpu/"

        /**
         * Read max/min frequencies for specific [coreNumber]. Return [Pair] with min and max frequency
         * or [Pair] with -1.
         */
        fun getMinMaxFreq(coreNumber: Int): Pair<Long, Long> {
            val minPath = "${CPU_INFO_DIR}cpu$coreNumber/cpufreq/cpuinfo_min_freq"
            val maxPath = "${CPU_INFO_DIR}cpu$coreNumber/cpufreq/cpuinfo_max_freq"
            return try {
                val minMhz = RandomAccessFile(minPath, "r").use { it.readLine().toLong() / 1000 }
                val maxMhz = RandomAccessFile(maxPath, "r").use { it.readLine().toLong() / 1000 }
                Pair(minMhz, maxMhz)
            } catch (e: Exception) {
                Log.e(
                    TAG,"getMinMaxFreq() - cannot read file", e)
                Pair(-1, -1)
            }
        }

        fun getSystemChip(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.BOARD.let { board ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val socManufacturer = Build.SOC_MANUFACTURER
                    val socModel = Build.SOC_MODEL
                    val validManufacturer = socManufacturer != Build.UNKNOWN
                    val validModel = socModel != Build.UNKNOWN

                    if (validManufacturer && validModel) {
                        "$socManufacturer $socModel ($board)"
                    } else if (validManufacturer) {
                        "$socManufacturer ($board)"
                    } else if (validModel) {
                        "$socModel ($board)"
                    } else board
                } else board
            }
        } else {
            errorResult()
        }

        private fun errorResult() = "n/a"
    }
}