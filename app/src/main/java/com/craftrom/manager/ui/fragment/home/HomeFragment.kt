package com.craftrom.manager.ui.fragment.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.craftrom.manager.MainActivity
import com.craftrom.manager.R
import com.craftrom.manager.core.NewsItem
import com.craftrom.manager.core.rss.RssFeed
import com.craftrom.manager.core.services.RetrofitInstance.setupRetrofitCall
import com.craftrom.manager.core.utils.Constants
import com.craftrom.manager.core.utils.Constants.DEFAULT_NEWS_SOURCE
import com.craftrom.manager.core.utils.Constants.TAG
import com.craftrom.manager.core.utils.ToolbarTitleProvider
import com.craftrom.manager.core.utils.ToolbarTitleUtils
import com.craftrom.manager.core.utils.hwinfo.DeviceSystemInfo
import com.craftrom.manager.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import java.util.Locale

class HomeFragment : Fragment(), MenuProvider, ToolbarTitleProvider {

    private var _binding: FragmentHomeBinding? = null
    private var job: Job? = null // Для відміни корутини
    private lateinit var adapterRecyclerViewRssContent: NewsAdapter
    private val arrayListContent = ArrayList<NewsItem>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupContentRecyclerView()
        setupViews()
        deviceInfo()
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

    private fun deviceInfo() {
        val android_custom: LinearLayout = binding.homeDeviceWrapper.homeAndroidCustom
        val deviceCard: CardView = binding.homeDeviceWrapper.homeDeviceAll

        val device: TextView = binding.homeDeviceWrapper.homeDeviceTitle
        val icDevice: ImageView = binding.homeDeviceWrapper.homeDeviceIcon
        val android_version:TextView = binding.homeDeviceWrapper.homeAndroidInfo
        val android_custom_title:TextView = binding.homeDeviceWrapper.homeCustomTitle
        val android_custom_version:TextView = binding.homeDeviceWrapper.homeCustomInfo
        val codename:TextView = binding.homeDeviceWrapper.homeDeviceCodenameInfo
        val platform:TextView = binding.homeDeviceWrapper.homeDevicePlatformInfo
        val kernel:TextView = binding.homeDeviceWrapper.homeDeviceKernelInfo
        val security_patch: TextView = binding.homeDeviceWrapper.homeSecurityInfo

        device.text = "${DeviceSystemInfo.manufacturer()} ${DeviceSystemInfo.model()}"
        icDevice.setImageResource(brandIcon(DeviceSystemInfo.manufacturer()))
        android_version.text = "${DeviceSystemInfo.releaseVersion()} (API ${DeviceSystemInfo.apiLevel()})"

        if (DeviceSystemInfo.craftromVersion().isNotEmpty()) {
            android_custom.visibility = View.VISIBLE
            android_custom_title.text = "CraftRom"
            android_custom_version.text = "${DeviceSystemInfo.craftromVersion()} (${DeviceSystemInfo.craftMaintainer()})"
        } else {
            android_custom.visibility = View.GONE
        }
        security_patch.text = Build.VERSION.SECURITY_PATCH
        codename.text = DeviceSystemInfo.device()
        platform.text = "${DeviceSystemInfo.board()} (${DeviceSystemInfo.hardware()}-${DeviceSystemInfo.arch()})"
        kernel.text = "${DeviceSystemInfo.osName()} ${DeviceSystemInfo.kernelVersion()}"

        deviceCard.setOnClickListener { view ->
            view?.let {
                val mainActivity = requireActivity() as MainActivity
                val navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main)
                navController.navigate(R.id.action_navigation_home_to_navigation_device)
            }
        }

    }

    private fun setupViews() {
        // Налаштування інтерфейсу
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewNews.layoutManager = layoutManager

        adapterRecyclerViewRssContent = NewsAdapter(arrayListContent)

        binding.recyclerViewNews.adapter = adapterRecyclerViewRssContent

        val internetAvailable = Constants.isInternetAvailable(requireContext())
        updateNewsData()
    }

    private fun setupContentRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerViewNews.layoutManager = layoutManager

        adapterRecyclerViewRssContent = NewsAdapter(arrayListContent)

        binding.recyclerViewNews.adapter = adapterRecyclerViewRssContent
    }

    private fun updateNewsData() {
        arrayListContent.clear()
        job = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                fetchNewsItems()
            } catch (e: Exception) {
                // Обробка помилок отримання новин
                e.printStackTrace()
            }
        }
    }

    private fun fetchNewsItems() {
        val call: Call<RssFeed> = setupRetrofitCall(DEFAULT_NEWS_SOURCE, "feed.xml")

        try {
            val response = call.execute()

            val rssFeed: RssFeed? = response.body()
            val listItems: List<NewsItem>? = rssFeed?.items

            listItems?.let { items ->
                // Создайте новий список для обновлення даних в адаптері
                val updatedItems = items.map { item ->
                    NewsItem(
                        category = item.category,
                        author= item.author,
                        description = item.description,
                        image = item.image,
                        link = item.link,
                        pubDate = item.pubDate,
                        title = item.title
                    )
                }

                activity?.runOnUiThread {
                    // Очищайте список та додавайте нові елементи в одному місці
                    arrayListContent.clear()
                    arrayListContent.addAll(updatedItems)
                    adapterRecyclerViewRssContent.notifyDataSetChanged()
                }
            } ?: run {
                Log.d("RssFeed", "Empty or null response body")
                showToast("Error fetching news!")
            }

        } catch (e: Exception) {
            Log.d("RssFeed", "Exception: ${e.message}")
            showToast("Error fetching news!")
        }
    }

    private fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun brandIcon(brandName: String): Int {
        return when (brandName.lowercase(Locale.getDefault())) {
            "xiaomi", "redmi", "poco" -> R.drawable.ic_xiaomi
            "google" -> R.drawable.ic_google
            "oneplus" -> R.drawable.ic_oneplus
            else -> R.drawable.ic_device // Іконка за замовчуванням, якщо бренд не визначено
        }
    }

    override fun getTitle(): String {
        return getString(R.string.title_home)// Повертаємо потрібний заголовок
    }
    override fun getSubtitle(): String {
        return "Welcome to our app" // Повертаємо потрібний підзаголовок
    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_home, menu)
        // Do stuff...
    }


    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_about -> {
                findNavController().navigate(
                    R.id.action_navigation_home_to_aboutFragment, null
                )
                true
            }
            R.id.action_settings -> {
                findNavController().navigate(
                    R.id.action_navigation_home_to_settingsActivity, null
                )
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}