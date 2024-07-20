package com.craftrom.manager.ui.fragment.home

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.craftrom.manager.R
import com.craftrom.manager.core.NewsItem
import com.craftrom.manager.core.rss.RssFeed
import com.craftrom.manager.core.services.RetrofitInstance.setupRetrofitCall
import com.craftrom.manager.core.utils.Constants
import com.craftrom.manager.core.utils.RecyclerViewUtils
import com.craftrom.manager.core.utils.hwinfo.DeviceSystemInfo
import com.craftrom.manager.core.utils.interfaces.ToolbarTitleProvider
import com.craftrom.manager.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.io.IOException
import java.util.Locale

class HomeFragment : Fragment(), ToolbarTitleProvider {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapterRecyclerViewRssContent: NewsAdapter
    private val arrayListContent = ArrayList<NewsItem>()

    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        deviceInfo()
        RecyclerViewUtils.checkEmpty(binding.recyclerViewNews, binding.emptyHelp, getString(R.string.wait))
        updateNewsData()
        // Add menu provider
        addMenuProvider()
    }

    private fun setupViews() {
        with(binding.recyclerViewNews) {
            layoutManager = LinearLayoutManager(requireContext())
            adapterRecyclerViewRssContent = NewsAdapter(arrayListContent)
            adapter = adapterRecyclerViewRssContent
        }
    }
    private fun deviceInfo() {
        with(binding.homeDeviceWrapper) {
            homeDeviceTitle.text = "${DeviceSystemInfo.manufacturer()} ${DeviceSystemInfo.model()}"
            homeDeviceIcon.setImageResource(brandIcon(DeviceSystemInfo.manufacturer()))
            homeAndroidInfo.text = "${DeviceSystemInfo.releaseVersion()} (API ${DeviceSystemInfo.apiLevel()})"

            if (DeviceSystemInfo.craftromVersion().isNotEmpty()) {
                homeAndroidCustom.visibility = View.VISIBLE
                homeCustomTitle.text = "CraftRom"
                homeCustomInfo.text = "${DeviceSystemInfo.craftromVersion()} (${DeviceSystemInfo.craftMaintainer()})"
            } else {
                homeAndroidCustom.visibility = View.GONE
            }
            homeSecurityInfo.text = Build.VERSION.SECURITY_PATCH
            homeDeviceCodenameInfo.text = DeviceSystemInfo.device()
            homeDevicePlatformInfo.text = "${DeviceSystemInfo.board()} (${DeviceSystemInfo.hardware()}-${DeviceSystemInfo.arch()})"
            homeDeviceKernelInfo.text = "${DeviceSystemInfo.osName()} ${DeviceSystemInfo.kernelVersion()}"

            homeDeviceAll.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_device)
            }
        }
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage")
    private fun updateNewsData() {
        job?.cancel()
        job = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (Constants.isInternetAvailable(requireContext())) {
                    try {
                        fetchNewsItems()
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error updating news data", e)
                        showFetchError()
                    }
                } else {
                    showNoInternetConnection()
                }
            }
        }
    }

    private suspend fun fetchNewsItems() {
        withContext(Dispatchers.IO) {
            val call: Call<RssFeed> = setupRetrofitCall(Constants.DEFAULT_NEWS_SOURCE, "feed.xml")
            try {
                val response = call.execute()
                if (!response.isSuccessful) {
                    throw IOException("Unexpected HTTP code: ${response.code()}")
                }
                val rssFeed: RssFeed? = response.body()
                val listItems: List<NewsItem>? = rssFeed?.items

                listItems?.let { items ->
                    val updatedItems = items.map { item ->
                        NewsItem(
                            category = item.category,
                            author = item.author,
                            description = item.description,
                            image = item.image,
                            link = item.link,
                            pubDate = item.pubDate,
                            title = item.title
                        )
                    }
                    withContext(Dispatchers.Main) {
                        updateRecyclerView(updatedItems)
                    }
                } ?: withContext(Dispatchers.Main) {
                    showFetchError()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching news items", e)
                withContext(Dispatchers.Main) {
                    showFetchError()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun updateRecyclerView(updatedItems: List<NewsItem>) {
        withContext(Dispatchers.Main) {
            arrayListContent.clear()
            arrayListContent.addAll(updatedItems)
            RecyclerViewUtils.checkEmpty(binding.recyclerViewNews, binding.emptyHelp, getString(R.string.wait))
            adapterRecyclerViewRssContent.notifyDataSetChanged()
        }
    }

    private fun showNoInternetConnection() {
        RecyclerViewUtils.checkEmpty(binding.recyclerViewNews, binding.emptyHelp, getString(R.string.no_internet_connection))
    }

    private fun showFetchError() {
        RecyclerViewUtils.checkEmpty(binding.recyclerViewNews, binding.emptyHelp, getString(R.string.failed_to_add_files))
    }

    override fun onResume() {
        super.onResume()
        updateNewsData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        job?.cancel()
    }

    override fun getTitle(): String {
        return getString(R.string.title_home)
    }

    override fun getSubtitle(): String {
        return "Welcome to our app"
    }

    private fun brandIcon(brandName: String): Int {
        return when (brandName.lowercase(Locale.getDefault())) {
            "xiaomi", "redmi", "poco" -> R.drawable.ic_xiaomi
            "google" -> R.drawable.ic_google
            "oneplus" -> R.drawable.ic_oneplus
            else -> R.drawable.ic_device // Default icon if brand is not recognized
        }
    }

    private fun addMenuProvider() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)  // Inflate your menu resource
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_about -> {
                        findNavController().navigate(R.id.action_navigation_home_to_aboutFragment, null)
                        true
                    }
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_settingsActivity, null)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}
