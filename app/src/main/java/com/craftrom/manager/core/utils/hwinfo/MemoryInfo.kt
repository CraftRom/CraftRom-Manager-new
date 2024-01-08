package com.craftrom.manager.core.utils.hwinfo

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import com.craftrom.manager.core.app.ServiceContext
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


open class MemoryInfo  {

    companion object {
        private val activityManager: ActivityManager by lazy {
            ServiceContext.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        }

        private val memoryInfo: ActivityManager.MemoryInfo by lazy {
            ActivityManager.MemoryInfo()
        }

        private fun loadMemoryInfo() {
            activityManager.getMemoryInfo(memoryInfo)
        }

        fun totalRAM(): String {
            loadMemoryInfo()
            return getFileSizeVal(memoryInfo.totalMem)
        }

        fun availableRAM(): String {
            loadMemoryInfo()
            return getFileSizeVal(memoryInfo.availMem)
        }

        fun usedRAM(): String {
            loadMemoryInfo()
            val total = memoryInfo.totalMem
            val available = memoryInfo.availMem
            val result = total - available
            return getFileSizeVal(result)
        }

        fun usedMemoryPercentage(): Int {
            val total = memoryInfo.totalMem
            val available = memoryInfo.availMem
            val used = total - available
            val percentage = (used.toDouble() / total.toDouble()) * 100
            return percentage.toInt()
        }

        fun getStorageSpace(dir: File): Pair<String, String> {
            val statsFs = StatFs(dir.absolutePath)
            val total = (statsFs.blockCountLong * statsFs.blockSizeLong)
            val free = (statsFs.availableBlocksLong * statsFs.blockSizeLong)
            return  Pair(getFileSizeVal(total), getFileSizeVal(free))
        }

        fun getSdCardStorage(): Pair<String, String> {
            val sdCardPath = getExternalSDMounts().firstOrNull()?.takeIf { it.isNotEmpty() } ?: return Pair(
                errorResult(), errorResult()
            )

            val truncatedPath = sdCardPath.substringBefore(":")
            val externalFilePath = File(truncatedPath)

            return if (externalFilePath.exists()) {
                val storageTotal = externalFilePath.totalSpace
                val availableSpace = externalFilePath.usableSpace

                if (storageTotal > 0) {
                    val storageUsed = storageTotal - availableSpace
                    Pair(getFileSizeVal(storageTotal), getFileSizeVal(availableSpace))
                } else {
                    Pair(errorResult(), errorResult())
                }
            } else {
                Pair(errorResult(), errorResult())
            }
        }

        private fun getFileSizeVal(size: Long): String {
            if (size <= 0) return "0"

            val units: Array<String> = arrayOf("B", "KB", "MB", "GB", "TB");
            val digitGroups: Int = (log10(size.toDouble()) / log10(1024.0)).toInt()
            return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
        }

        private fun getFileSize(size: Long): String {
            if (size <= 0) return "0"

            val digitGroups: Int = (log10(size.toDouble()) / log10(1024.0)).toInt()
            return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble()))
        }


        /**
         * And there the magic starts :) TBH I'm not so sure that this is the only good solution but
         * from my testing it is the working one for most of the phones.
         */
        private fun getExternalSDMounts(): List<String> {
            val sdDirList = mutableListOf<String>()
            try {
                val dis = DataInputStream(FileInputStream("/proc/mounts"))
                val br = BufferedReader(InputStreamReader(dis))
                val externalDir = Environment.getExternalStorageDirectory().path
                while (true) {
                    val strLine = br.readLine()
                    if (strLine == null) {
                        break
                    } else if (!(strLine.contains("asec")
                                || strLine.contains("legacy")
                                || strLine.contains("Android/obb"))
                    ) {
                        if (strLine.startsWith("/dev/block/vold/")
                            || strLine.startsWith("/dev/block/sd")
                            || strLine.startsWith("/dev/fuse")
                            || strLine.startsWith("/mnt/media_rw")
                        ) {
                            val lineElements = strLine
                                .split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            val path = File(lineElements[1])
                            if ((path.exists() || path.isDirectory || path.canWrite())
                                && path.exists()
                                //&& path.canRead()
                                && !path.path.contains("/system")
                                && !sdDirList.contains(lineElements[1])
                                && lineElements[1] != externalDir
                                && lineElements[1] != "/storage/emulated"
                                && !sdDirList.any {
                                    it.endsWith(
                                        lineElements[1]
                                            .substring(
                                                lineElements[1].lastIndexOf("/"),
                                                lineElements[1].length
                                            )
                                    )
                                }
                            ) {
                                sdDirList.add(lineElements[1])
                            }
                        }
                    }
                }
                dis.close()
            } catch (e: Exception) {
                Log.d("SdCard", e.toString())
            }
            return sdDirList
        }

        private fun errorResult(): String = "n/a"
    }
}