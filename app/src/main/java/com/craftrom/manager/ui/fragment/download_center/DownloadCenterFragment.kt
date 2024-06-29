package com.craftrom.manager.ui.fragment.download_center

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.craftrom.manager.R
import com.craftrom.manager.core.FileInfo
import com.craftrom.manager.core.utils.Constants.isInternetAvailable
import com.craftrom.manager.core.utils.RecyclerViewUtils
import com.craftrom.manager.core.utils.ToolbarTitleUtils
import com.craftrom.manager.core.utils.hwinfo.DeviceSystemInfo
import com.craftrom.manager.core.utils.interfaces.ToolbarTitleProvider
import com.craftrom.manager.databinding.FragmentDownloadCenterBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.markdown4j.Markdown4jProcessor
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DownloadCenterFragment : Fragment(), MenuProvider, ToolbarTitleProvider {

    private lateinit var filesAdapter: FilesAdapter
    private var _binding: FragmentDownloadCenterBinding? = null
    private val binding get() = _binding!!

    private var downloadJob: Job? = null
    private var isChangelogLoading = false

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
        val cardFiles = binding.filesCard
        val recyclerView = binding.recyclerFileList
        val empty = binding.emptyHelp
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = filesAdapter
        // Отримання інформації про пристрій та версію CraftRom
        val deviceInfo = DeviceSystemInfo.deviceCode()
        val craftromVersion = DeviceSystemInfo.craftromVersion()
        val url = "https://sourceforge.net/projects/craftrom/files/$deviceInfo/$craftromVersion/"

        RecyclerViewUtils.checkEmpty(recyclerView, empty, getString(R.string.empty_file_list))

        if (DeviceSystemInfo.isAnyCraftromProperty()) {
            cardFiles.visibility = View.VISIBLE // Показуємо карточку файлів
            binding.romInfo.root.visibility = View.VISIBLE
            binding.romInfoShort.root.visibility = View.GONE
            // Перевірка наявності Інтернет-підключення
            if (isInternetAvailable(requireContext())) {
                // Отримання HTML-коду сторінки та відображення інформації про файли в RecyclerView
                downloadJob = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val document = Jsoup.connect(url).get()
                        val filesList = extractFilesList(document)
                        withContext(Dispatchers.Main) {
                            filesAdapter.setItems(filesList)
                            RecyclerViewUtils.checkEmpty(recyclerView, empty, null)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            RecyclerViewUtils.checkEmpty(recyclerView, empty, getString(R.string.failed_to_add_files))
                        }
                    }
                }
            } else {
                RecyclerViewUtils.checkEmpty(recyclerView, empty, getString(R.string.no_internet_connection))
            }
        } else {
            cardFiles.visibility = View.GONE // Робимо карточку файлів невидимою
            binding.romInfo.root.visibility = View.GONE
            binding.romInfoShort.root.visibility = View.VISIBLE
        }

        romInfo()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Виклик функції setToolbarText для задання тексту в панелі інструментів
        ToolbarTitleUtils.setToolbarText(
            requireActivity() as AppCompatActivity,
            getTitle(),
            getSubtitle()
        )
    }

    private fun romInfo() {
        
        if (DeviceSystemInfo.isAnyCraftromProperty()) {

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

        } else {
            binding.romInfoShort.romDeviceCodenameInfo.text = getString(R.string.rom_codename_title_info, DeviceSystemInfo.device() , DeviceSystemInfo.deviceCode())
            binding.romInfoShort.homeSecurityInfo.text = Build.VERSION.SECURITY_PATCH
        }
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
            val lastUpdated = outputFormat.format(lastUpdatedDate!!)
            val downloadLink = row.selectFirst("a")!!.attr("href")
            files.add(FileInfo(name, size, lastUpdated, downloadLink))
        }
        return files
    }
    override fun getTitle(): String {
        return getString(R.string.title_dcenter)
    }

    override fun getSubtitle(): String {
        return getString(R.string.subtitle_dcenter)
    }

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

        fun colorToHex(color: Int): String {
            return String.format("#%06X", 0xFFFFFF and color)
        }

        val backgroundColor = colorToHex(getColor(requireContext(), R.color.bgHtml))
        val textColor = colorToHex(getColor(requireContext(), R.color.h2Html))
        val h2Color = colorToHex(getColor(requireContext(), R.color.strongHtml))
        val borderColor = colorToHex(getColor(requireContext(), R.color.dividerHtml))
        val listItemColor = colorToHex(getColor(requireContext(), R.color.textHtml))
        val listItemMarkerColor = colorToHex(getColor(requireContext(), R.color.strongHtml))
        val strongTextColor = colorToHex(getColor(requireContext(), R.color.strongHtml))

        """
<html>
<head>
    <style>
        :root {
            --background-color: $backgroundColor; /* Чорний фон */
            --text-color: $textColor; /* Білий текст для контрасту */
            --h2-color: $h2Color; /* Колір заголовків */
            --border-color: $borderColor; /* Колір нижньої границі заголовків */
            --list-item-color: $listItemColor; /* Колір пунктів списку */
            --list-item-marker-color: $listItemMarkerColor; /* Колір маркерів пунктів списку */
            --strong-text-color: $strongTextColor; /* Колір сильних тегів */
        }

        body {
            background-color: var(--background-color);
            color: var(--text-color);
        }

        h2 {
            color: var(--h2-color);
            padding-bottom: 0.3em;
            font-size: 1.3em;
            border-bottom: 1px solid var(--border-color);
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
            color: var(--list-item-color);
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
            color: var(--list-item-marker-color); /* Колір крапки */
            font-size: 0.75em; /* Розмір шрифту крапки */
            margin-right: 0.5em; /* Відстань між крапкою і текстом */
        }

        li strong {
            font-size: 1.1em;
            color: var(--strong-text-color); /* колір тексту, який використовується для сильних тегів */
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
        downloadJob?.cancel()
        _binding = null
    }
}
