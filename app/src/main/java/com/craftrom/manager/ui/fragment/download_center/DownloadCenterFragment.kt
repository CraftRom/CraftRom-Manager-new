package com.craftrom.manager.ui.fragment.download_center

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.craftrom.manager.MainActivity
import com.craftrom.manager.R
import com.craftrom.manager.core.FileInfo
import com.craftrom.manager.core.utils.hwinfo.DeviceSystemInfo
import com.craftrom.manager.databinding.FragmentDownloadCenterBinding
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

class DownloadCenterFragment : Fragment() {

    private lateinit var filesAdapter: FilesAdapter
    private var _binding: FragmentDownloadCenterBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        setUpToolbar()
    }

    override fun onResume() {
        super.onResume()
        setUpToolbar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadCenterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Ініціалізація RecyclerView та адаптера
        filesAdapter = FilesAdapter()
        val recyclerView = binding.recyclerFileList
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = filesAdapter
        // Отримання інформації про пристрій та версію CraftRom
        val deviceInfo = DeviceSystemInfo.deviceCode()
        val craftromVersion = DeviceSystemInfo.craftromVersion()
        val url = "https://sourceforge.net/projects/craftrom/files/$deviceInfo/$craftromVersion/"

        // Перевірка наявності Інтернет-підключення
        if (isNetworkAvailable()) {
            // Отримання HTML-коду сторінки та відображення інформації про файли в RecyclerView
            Thread {
                try {
                    val document = Jsoup.connect(url).get()
                    val filesList = extractFilesList(document)
                    requireActivity().runOnUiThread {
                        filesAdapter.setItems(filesList)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Помилка завантаження даних", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        } else {
            // Повідомлення про відсутність Інтернет-підключення
            Toast.makeText(requireContext(), "Немає підключення до Інтернету", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun extractFilesList(document: org.jsoup.nodes.Document): List<FileInfo> {
        val files = mutableListOf<FileInfo>()
        val rows = document.select("tr.file")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        for (row in rows) {
            val name = row.selectFirst("span.name")!!.text().trim()
            val size = row.selectFirst("td.opt[headers=files_size_h]")!!.text().trim()
            val lastUpdatedStr = row.selectFirst("td.opt[headers=files_date_h] abbr")!!.attr("title")
            val lastUpdatedDate = dateFormat.parse(lastUpdatedStr)
            val lastUpdated = outputFormat.format(lastUpdatedDate)
            val downloadLink = row.selectFirst("a")!!.attr("href")
            files.add(FileInfo(name, size, lastUpdated, downloadLink))
        }
        return files
    }

    private fun setUpToolbar() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.setToolbarText(getTitle(), getSubtitle())
    }

    private fun getTitle() = getString(R.string.title_dcenter)
    private fun getSubtitle() = getString(R.string.subtitle_dcenter)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
