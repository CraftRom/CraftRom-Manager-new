package com.craftrom.manager.core.utils.hwinfo

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

        private fun errorResult() = "n/a"
    }
}