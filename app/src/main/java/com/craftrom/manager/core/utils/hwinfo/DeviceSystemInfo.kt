package com.craftrom.manager.core.utils.hwinfo

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.craftrom.manager.R
import com.craftrom.manager.core.app.ServiceContext.context
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.security.Security
import java.text.SimpleDateFormat
import java.util.*


open class DeviceSystemInfo {

    companion object {

        fun model(): String = Build.MODEL

        fun product(): String = Build.PRODUCT

        fun display(): String = Build.DISPLAY

        fun id(): String = Build.ID

        fun tags(): String = Build.TAGS

        fun brand(): String = Build.BRAND

        fun device():String = Build.DEVICE

        fun type(): String = Build.TYPE

        fun manufacturer(): String = Build.MANUFACTURER

        fun board(): String = Build.BOARD

        fun hardware(): String = Build.HARDWARE

        fun releaseVersion(): String = Build.VERSION.RELEASE

        fun apiLevel(): Int = Build.VERSION.SDK_INT

        fun user(): String = Build.USER

        fun host(): String = Build.HOST

        fun fingerprint(): String = Build.FINGERPRINT

        fun bootloader(): String = Build.BOOTLOADER

        fun supportedAbis(): Array<String> {
            return Build.SUPPORTED_ABIS
        }

        fun supported32BitAbis(): Array<String> {
            return Build.SUPPORTED_32_BIT_ABIS
        }

        fun supported64BitAbis(): Array<String> {
            return Build.SUPPORTED_64_BIT_ABIS
        }
        fun is64Bit(): Boolean {
            return supported64BitAbis().isNotEmpty()
        }

        // Додано функцію для отримання назви SoC
        fun soc(): String {
            val supportedAbis = supportedAbis()
            if (supportedAbis.isNotEmpty()) {
                return supportedAbis[0]
            }
            return "Information not available"
        }

        fun socManufacturer(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MANUFACTURER
        } else {
            errorResult()
        }

        fun socModel(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL
        } else {
            errorResult()
        }

        fun numberOfCores(): Int {
            return Runtime.getRuntime().availableProcessors()
        }

        fun arch(): String {
            val arch = System.getProperty("os.arch")
            return if (!arch.isNullOrEmpty()) arch else errorResult()
        }

        fun kernelVersion(): String {
            val kernelVersion = System.getProperty("os.version")
            return if (!kernelVersion.isNullOrEmpty()) kernelVersion else errorResult()
        }

        fun osName(): String {
            val kernelVersion = System.getProperty("os.name")
            return if (!kernelVersion.isNullOrEmpty()) kernelVersion else errorResult()
        }

        fun getJavaVMVersion(): String {
            val javaVMVersion: String? = System.getProperty("java.vm.version")
            if (javaVMVersion != null) {
                return "ART $javaVMVersion"
            }

            return "Not Found"
        }

        fun getOpenGLESVersion(): String {
            val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val configurationInfo = activityManager.deviceConfigurationInfo

            return configurationInfo.glEsVersion.toDouble().toString()
        }

        /**
         * Obtain Vulkan version
         */
        fun getVulkanVersion(): String {
            val packageManager: PackageManager = context.packageManager

            val vulkan = packageManager.systemAvailableFeatures.find {
                it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION
            }?.version ?: 0
            if (vulkan == 0) {
                return errorResult()
            }

            // Extract versions from bit field
            // See: https://developer.android.com/reference/android/content/pm/PackageManager#FEATURE_VULKAN_HARDWARE_VERSION
            val major = vulkan shr 22           // Higher 10 bits
            val minor = vulkan shl 10 shr 22    // Middle 10 bits
            val patch = vulkan shl 20 shr 22    // Lower 12 bits
            //
            return "$major.$minor.$patch"
        }

        /**
         * Check if device is rooted. Source:
         * https://stackoverflow.com/questions/1101380/determine-if-running-on-a-rooted-device
         */
        fun isDeviceRooted(): Boolean =
            checkRootMethod1() || checkRootMethod2() || checkRootMethod3()

        private fun checkRootMethod1(): Boolean {
            val buildTags = Build.TAGS
            return buildTags != null && buildTags.contains("test-keys")
        }

        private fun checkRootMethod2(): Boolean {
            val paths = arrayOf(
                "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
                "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su"
            )
            return paths.any { File(it).exists() }
        }

        private fun checkRootMethod3(): Boolean {
            var process: Process? = null
            return try {
                process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
                val br = BufferedReader(InputStreamReader(process.inputStream))
                br.readLine() != null
            } catch (t: Throwable) {
                false
            } finally {
                process?.destroy()
            }
        }

        /**
         * Get information about security providers
         */
        fun getSecurityData(): String {
            val securityProviders = Security.getProviders().map { "${it.name}, v${it.version}"}
            return securityProviders.joinToString("\n") ?: errorResult()
        }

        /**
         * Add information about device encrypted storage status
         */
        fun getDeviceEncryptionStatus(): String {
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager

            return try {
                devicePolicyManager?.let {
                    val statusText = when (it.storageEncryptionStatus) {
                        DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> "Unsupported"
                        DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE -> "Inactive"
                        DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE -> "Active"
                        DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER -> "Active Per User"
                        else -> "Unknown"
                    }
                    statusText
                } ?: "DevicePolicyManager is null"
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }

        fun getGsfAndroidId(): String? {
            val GSF_PROVIDER_URI = Uri.parse("content://com.google.android.gsf.gservices")
            val ANDROID_ID_KEY = "android_id"

            val params = arrayOf(ANDROID_ID_KEY)
            val c = context.contentResolver.query(GSF_PROVIDER_URI, null, null, params, null)
            return if (!c!!.moveToFirst() || c.columnCount < 2) null else try {
                java.lang.Long.toHexString(c.getString(1).toLong())
            } catch (e: NumberFormatException) {
                null
            }
        }

        fun craftromVersion(): String {
            val exodusVersion = getSystemProperty("org.craftrom.version")
            return exodusVersion.toString()
        }

        fun craftMaintainer(): String {
            val exodusMaintainer = getSystemProperty("ro.craftrom.maintainer")
            return if (!exodusMaintainer.isNullOrEmpty()) exodusMaintainer else errorResult()
        }

        fun chidoriVersion(): String = kernelVersion().substring(
            kernelVersion().lastIndexOf(".")).substring(1, 4)

        fun chidoriName(): String = kernelVersion().substring(
            kernelVersion().indexOf("-")).substring(1, 8)

        fun tsukuyoumiName(): String = kernelVersion().substring(
            kernelVersion().indexOf("-")).substring(1, 11)

        @SuppressLint("SimpleDateFormat")
        fun date(): String {
            val date = Date(Build.TIME)
            val dateFormat = SimpleDateFormat("dd/MM/yy")
            return dateFormat.format(date)
        }

        fun codeName(): String {
            val fields = Build.VERSION_CODES::class.java.fields
            return fields[Build.VERSION.SDK_INT].name
        }

        fun deviceCode(): String {
            val device = device()
            val code: String
            val chime = mutableListOf("citrus", "lime", "lemon", "pomelo")
            val olives = mutableListOf("olive", "olivewood", "olivelite")
            val onclite = mutableListOf("onc", "onclite")
            val spes = mutableListOf("spes", "spesn")
            val surya = mutableListOf("karna", "surya")
            code = if (isEliminated(device, chime)) {
                "chime"
            } else {
                if (isEliminated(device, onclite)) {
                    "onclite"
                } else {
                    if (isEliminated(device, surya)) {
                            "surya"
                    } else {
                        if (isEliminated(device, olives)) {
                            "olives"
                        } else {
                            if (isEliminated(device, spes)) {
                                "spes"
                            } else {
                                device
                            }
                        }
                    }
                }
            }

            return code
        }



    private fun isEliminated(name: String, device: MutableList<String>): Boolean {
            return name in device
        }

    private fun getSystemProperty(propName: String): String? {
            var line = ""
            var input: BufferedReader? = null
            try {
                val p = Runtime.getRuntime().exec("getprop $propName")
                input = BufferedReader(InputStreamReader(p.inputStream), 1024)
                line = input.readLine()
                input.close()
            } catch (ex: IOException) {
                return null
            } finally {
                if (input != null) {
                    try {
                        input.close()
                    } catch (e: IOException) {
                    }
                }
            }
            return line
    }

    private fun errorResult() = context.getString(R.string.common_empty_result)

    }
}