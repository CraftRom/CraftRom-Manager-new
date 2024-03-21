package com.craftrom.manager.ui.fragment.download_center

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.craftrom.manager.MainActivity
import com.craftrom.manager.R
import com.craftrom.manager.core.FileInfo
import com.craftrom.manager.core.utils.hwinfo.DeviceSystemInfo
import com.craftrom.manager.databinding.FragmentDownloadCenterBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.markdown4j.Markdown4jProcessor
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DownloadCenterFragment : Fragment(), MenuProvider {

    private lateinit var filesAdapter: FilesAdapter
    private var _binding: FragmentDownloadCenterBinding? = null
    private val binding get() = _binding!!


    private var isChangelogLoading = false

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
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

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

        romInfo()
        return root
    }

    private fun romInfo() {
        val originalDateFormat = SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val originalDateString = DeviceSystemInfo.craftBuildDate()
        val originalDate = originalDateFormat.parse(originalDateString)
        val displayDate = displayDateFormat.format(originalDate as Date)

        binding.romInfo.romDeviceCodenameInfo.text = getString(R.string.rom_codename_title_info, DeviceSystemInfo.device() , DeviceSystemInfo.deviceCode())
        binding.romInfo.romVersionInfo.text = getString(R.string.rom_version_info, DeviceSystemInfo.craftromVersion(), DeviceSystemInfo.releaseVersion())
        binding.romInfo.romBuildDateInfo.text = displayDate
        binding.romInfo.homeSecurityInfo.text = Build.VERSION.SECURITY_PATCH
        binding.romInfo.romMaintainerInfo.text = DeviceSystemInfo.craftMaintainer()
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

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_rom, menu)
        // Do stuff...
    }


    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_changelog -> {
                if (!isChangelogLoading) {
                    isChangelogLoading = true
                    changelogView()
                }
                true
            }
            else -> false
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun changelogView() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val markdownUrl = "https://raw.githubusercontent.com/craftrom-os/official_devices/master/changelog_rom.md"
                val texts = getMarkdownAsHtml(markdownUrl)
                showChangelogDialog(texts)
            } catch (e: Exception) {
                // обробка помилок
            } finally {
                isChangelogLoading = false
            }
        }
    }



    private suspend fun getMarkdownAsHtml(url: String): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Failed to download Markdown file: $url")
        }
        val markdownFile = response.body.string()
        """
<html>
<head>
    <style>
        body {
            background-color: #EEEEEE; /* Прозорий фон */
        }

        h2 {
            color: #1f2328;
            padding-bottom: 0.3em;
            font-size: 1.3em;
            border-bottom: 1px solid #d8dee4;
        }
        ol {
        margin-top: 16px;
        }
        
        ul, ol {
            list-style-type: none;
            padding-left: 0; /* замінено на 0, щоб усунути відступи відносно зовнішнього контейнера */
            margin-top: 0;
        }

        ol li {
            margin-bottom: 16px;
        }

        li ul {
            list-style-type: none;
            margin-block-start: 0px;
            margin-block-end: 0px;
        }

        li {
            color: #586069;
            list-style-type: none;
            margin-bottom: 0px;
            padding-left: 8px;
        }

        ul li {
            list-style-type: none;
            margin-bottom: 2px;
        }

        ul li::before { /* використовуємо псевдоелемент ::before для створення крапок */
            content: "\2022"; /* Код символу для крапки */
            color: #2196f3; /* Колір крапки, який використовується на GitHub */
            font-size: 0.75em; /* Розмір шрифту крапки */
            margin-right: 0.5em; /* Відстань між крапкою і текстом */
        }

        li strong {
            font-size: 1.1em;
            color: #2196f3; /* колір тексту, який використовується для сильних тегів */
            margin-bottom: 8px;
        }
    </style>
</head>
<body>
    ${Markdown4jProcessor().process(markdownFile)}
</body>
</html>
""".trimIndent()
    }


    private fun showChangelogDialog(htmlContent: String) {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeBottomSheet)
        val card = LayoutInflater.from(context).inflate(R.layout.dialog_content, null, false)
        val dIcon = card.findViewById<ImageView>(R.id.dialogIcon)
        val dTitle = card.findViewById<TextView>(R.id.dialogTitle)
        val webView = card.findViewById<WebView>(R.id.dialogContentWebView)

        dIcon.setImageResource(R.drawable.ic_list)
        dTitle.text = "Changelog"

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)

        dialog.setCancelable(true)
        dialog.setContentView(card)
        dialog.dismissWithAnimation = true
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
