package com.craftrom.manager.core.utils.hwinfo

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.UNKNOWN
import com.craftrom.manager.R
import com.craftrom.manager.core.app.ServiceContext.context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
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
            Build.BOARD.let { board ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val socManufacturer = Build.SOC_MANUFACTURER
                    val socModel = Build.SOC_MODEL
                    val validManufacturer = socManufacturer != UNKNOWN
                    val validModel = socModel != UNKNOWN

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
            return securityProviders.joinToString("\n")
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

        fun isAnyCraftromProperty(): Boolean {
            val propertiesToCheck = listOf(
                "org.craftrom.version",
                "ro.craftrom.maintainer",
                "org.craftrom.build_date"
            )

            return propertiesToCheck.any { getSystemProperty(it)!!.isNotEmpty() }
        }

        fun craftromVersion(): String {
            val exodusVersion = getSystemProperty("org.craftrom.version")
            return exodusVersion.toString()
        }

        fun craftMaintainer(): String {
            val exodusMaintainer = getSystemProperty("ro.craftrom.maintainer")
            return if (!exodusMaintainer.isNullOrEmpty()) exodusMaintainer else errorResult()
        }

        fun craftBuildDate(): String{

            val buildDate = getSystemProperty("org.craftrom.build_date")
            return if (!buildDate.isNullOrEmpty()) buildDate else errorResult()
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
        data class Version(
            val version_code: String,
            val xda_thread: String,
            val stable: Boolean,
            val deprecated: Boolean
        )

        suspend fun deviceCode(): String? {
            return withContext(Dispatchers.IO) {
                try {
                    val url = "https://raw.githubusercontent.com/craftrom-os/official_devices/master/devices.json"
                    val jsonString = URL(url).readText()

                    val devicesArray = JSONArray(jsonString)

                    val device = device() // Потрібно замінити на фактичний спосіб отримання поточного device

                    var codename: String? = null

                    for (i in 0 until devicesArray.length()) {
                        val deviceObject = devicesArray.getJSONObject(i)
                        val variantNames = deviceObject.getJSONArray("variant_name")

                        for (j in 0 until variantNames.length()) {
                            if (variantNames.getString(j) == device) {
                                codename = deviceObject.getString("codename")
                                break
                            }
                        }

                        if (codename != null) {
                            break
                        }
                    }

                    codename
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
        fun code(): String {
            var deviceInfo: String?
            runBlocking {
                deviceInfo = deviceCode()
                deviceInfo?.let {
                    println("Device info: $deviceInfo")
                }
            }
            return deviceInfo ?: device()
        }

    private fun getSystemProperty(propName: String): String? {
        val line: String
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