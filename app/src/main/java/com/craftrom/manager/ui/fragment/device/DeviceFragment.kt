package com.craftrom.manager.ui.fragment.device

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.craftrom.manager.MainActivity
import com.craftrom.manager.R
import com.craftrom.manager.core.time.CoroutineTimer
import com.craftrom.manager.core.utils.Constants
import com.craftrom.manager.core.utils.Constants.INTERVAL_1_FPS
import com.craftrom.manager.core.utils.ToolbarTitleUtils
import com.craftrom.manager.core.utils.hwinfo.BatteryInfo
import com.craftrom.manager.core.utils.hwinfo.CPUInfo
import com.craftrom.manager.core.utils.hwinfo.DeviceSystemInfo
import com.craftrom.manager.core.utils.hwinfo.DisplayInfo
import com.craftrom.manager.core.utils.hwinfo.MemoryInfo
import com.craftrom.manager.databinding.FragmentDeviceBinding

class DeviceFragment : Fragment() {
    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    private val intervalometer = CoroutineTimer {
        BatteryInfo.updateBatteryInfo()
        battery()
        memory()
    }

    override fun onStart() {
        super.onStart()
        intervalometer.interval(INTERVAL_1_FPS)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolbarTitleUtils.setToolbarText(
            requireActivity() as AppCompatActivity,
            getTitle(),
            getSubtitle()
        )
        deviceInfo()
        display()
        memory()
        socInfo()
        batteryInfo()
    }

    @SuppressLint("SetTextI18n")
    private fun deviceInfo() {
        val gfs: TextView = binding.homeDevWrapper.homeGfsInfo
        val encrypt: TextView = binding.homeDevWrapper.homeEncryptionInfo
        val securityProvider: TextView = binding.homeDevWrapper.homeSecurityProvidersInfo
        val root: TextView =binding.homeDevWrapper.homeRootInfo
        val device: TextView = binding.homeDevWrapper.homeDevTitle
        val openGL: TextView = binding.homeDevWrapper.homeOpenglInfo
        val vulkan: TextView = binding.homeDevWrapper.homeVulkanInfo
        val java: TextView = binding.homeDevWrapper.homeJavaInfo

        gfs.text = DeviceSystemInfo.getGsfAndroidId()
        encrypt.text = DeviceSystemInfo.getDeviceEncryptionStatus()
        securityProvider.text = DeviceSystemInfo.getSecurityData().toString()
        root.text = Constants.getYesNoString(DeviceSystemInfo.isDeviceRooted())
        openGL.text = DeviceSystemInfo.getOpenGLESVersion()
        vulkan.text = DeviceSystemInfo.getVulkanVersion()
        java.text = DeviceSystemInfo.getJavaVMVersion()
        device.text = "${DeviceSystemInfo.manufacturer()} ${DeviceSystemInfo.model()}"
    }

    private fun memory() {
        val ram: TextView = binding.homeMemoryWrapper.homeRamInfo
        val freeRam: TextView = binding.homeMemoryWrapper.homeFreeRamInfo
        val internal: TextView = binding.homeMemoryWrapper.homeInternalInfo
        val external: TextView = binding.homeMemoryWrapper.homeExternalInfo

        ram.text = "${MemoryInfo.usedRAM()} / ${MemoryInfo.totalRAM()} - ${MemoryInfo.usedMemoryPercentage()}%"
        freeRam.text = MemoryInfo.availableRAM()
        internal.text = "${MemoryInfo.getStorageSpace(Environment.getDataDirectory()).second} / ${MemoryInfo.getStorageSpace(Environment.getDataDirectory()).first}"
        external.text = "${MemoryInfo.getSdCardStorage().second} / ${MemoryInfo.getSdCardStorage().first}"
    }

    private fun display() {
        val resolution: TextView = binding.homeDisplayWrapper.homeScreenResolutionInfo
        val destiny: TextView = binding.homeDisplayWrapper.homeDensityInfo
        val size: TextView = binding.homeDisplayWrapper.homeScreenSizeInfo

        resolution.text = "${ DisplayInfo.getScreenResolution() } pixels"
        destiny.text = "${ DisplayInfo.getScreenDensity() } dpi"
        size.text = "${ DisplayInfo.getScreenSize() } inches"
    }
    @SuppressLint("SetTextI18n")
    private fun socInfo() {
        val soc: TextView = binding.homeSocWrapper.homeSocTitle
        val socAbi: TextView = binding.homeSocWrapper.homeSocAbi
        val machine: TextView = binding.homeSocWrapper.homeMachineInfo
        val mode: TextView = binding.homeSocWrapper.homeModeInfo
        val instructions: TextView = binding.homeSocWrapper.homeInstructionsInfo
        val cores: TextView = binding.homeSocWrapper.homeCoreInfo
        val cl_speed: TextView = binding.homeSocWrapper.homeCoreSpeedInfo

        cores.text = DeviceSystemInfo.numberOfCores().toString()
        soc.text = "${DeviceSystemInfo.socManufacturer()} ${DeviceSystemInfo.socModel()}"
        socAbi.text = DeviceSystemInfo.soc()
        machine.text = DeviceSystemInfo.arch()
        mode.text = if (DeviceSystemInfo.is64Bit()) {
            "64-bit"
        } else {
            "32-bit"
        }
        val supportedAbis = DeviceSystemInfo.supportedAbis()
        val supportedAbisText = supportedAbis.joinToString(", ")
        instructions.text = supportedAbisText

        var minFreqMHz = Long.MAX_VALUE
        var maxFreqMHz = Long.MIN_VALUE
        for (i in 0 until DeviceSystemInfo.numberOfCores()) {
            val (min, max) = CPUInfo.getMinMaxFreq(i)

            // Перевіряємо чи поточні значення менше чи більше глобальних мінімального та максимального значень
            if (min < minFreqMHz) {
                minFreqMHz = min
            }

            if (max > maxFreqMHz) {
                maxFreqMHz = max
            }
        }
        val cpuFrequencies = "$minFreqMHz - $maxFreqMHz MHz"
        cl_speed.text = cpuFrequencies
    }

    private fun batteryInfo() {
        val technology: TextView= binding.homeBatteryWrapper.homeTechnologyInfo
        val health: TextView = binding.homeBatteryWrapper.homeHealthInfo
        val cycle: TextView = binding.homeBatteryWrapper.homeCycleInfo

        technology.text = BatteryInfo.technology()
        health.text = BatteryInfo.getHealth()
        cycle.text = BatteryInfo.cycleCount()

    }

    private fun battery() {
        val level: TextView = binding.homeBatteryWrapper.homeLevelInfo
        val volt: TextView = binding.homeBatteryWrapper.homeVoltageInfo
        val temp: TextView = binding.homeBatteryWrapper.homeTemperatureInfo
        val status: TextView = binding.homeBatteryWrapper.homeStatusInfo
        val conect: TextView = binding.homeBatteryWrapper.homeConectInfo
        val sysUptime: TextView = binding.homeBatteryWrapper.homeSysUptimeInfo

        level.text = "${BatteryInfo.getBatteryLevel()}%"
        temp.text = "${BatteryInfo.temp()} °C"
        volt.text = "${BatteryInfo.voltage()} mV"
        status.text = BatteryInfo.getStatus()
        conect.text = BatteryInfo.getConnection()
        sysUptime.text = BatteryInfo.getSystemUptime()
    }

    private fun getTitle() = getString(R.string.title_device)
    private fun getSubtitle() = "DEV"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        intervalometer.stop()
    }
}