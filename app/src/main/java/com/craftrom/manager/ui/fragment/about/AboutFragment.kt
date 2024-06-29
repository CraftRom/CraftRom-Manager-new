package com.craftrom.manager.ui.fragment.about

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.craftrom.manager.R
import com.craftrom.manager.core.Link
import com.craftrom.manager.core.utils.interfaces.ToolbarTitleProvider
import com.craftrom.manager.databinding.FragmentAboutBinding

class AboutFragment : Fragment(), ToolbarTitleProvider {
    private lateinit var versionApp: TextView
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)

        versionApp = binding.version

        try {
            val packageInfo =
                requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    PackageManager.GET_META_DATA
                )
            val versionName = packageInfo.versionName
            versionApp.text = versionName
        } catch (e: PackageManager.NameNotFoundException) {
            // Handle the exception if needed
        }

        binding.aboutDonate.setOnClickListener {
            openDonate()
        }

        binding.aboutZsuDonate.setOnClickListener {
            openZSUDonate()
        }

        // Динамічне додавання гіперпосилань
        val linksContainer = binding.linksContainer
        val inflater = LayoutInflater.from(requireContext())

        val iconColor = ContextCompat.getColor(requireContext(), R.color.icobg)

        for (link in links) {
            val linkView = inflater.inflate(R.layout.item_link, linksContainer, false)
            val iconImageView = linkView.findViewById<ImageView>(R.id.iconImageView)
            val labelTextView = linkView.findViewById<TextView>(R.id.labelTextView)

            iconImageView.setImageResource(link.iconResId)
            // Задаємо колір для іконки
            iconImageView.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
            labelTextView.text = getString(link.labelResId)
            linkView.setOnClickListener {
                openUrl(link.url)
            }

            // Додавання View до контейнера
            linksContainer.addView(linkView)
        }



        return binding.root
    }

    private val links = listOf(
        Link(
            iconResId = R.drawable.ic_new_releases,
            labelResId = R.string.tg_news,
            url = "https://t.me/craftrom_news",
        ),
        Link(
            iconResId = R.drawable.ic_help,
            labelResId = R.string.tg_chat,
            url = "https://t.me/craftrom",
        ),
        Link(
            iconResId = R.drawable.ic_github,
            labelResId = R.string.github,
            url = "https://github.com/craftrom-os",
        ),
        Link(
            iconResId = R.drawable.ic_website,
            labelResId = R.string.website,
            url = "https://www.craft-rom.pp.ua/",
        ),
    )

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
    private fun openDonate() {
        val uri = Uri.parse("https://www.craft-rom.pp.ua/donate/")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun openZSUDonate() {
        val uri = Uri.parse("https://bank.gov.ua/en/news/all/natsionalniy-bank-vidkriv-spetsrahunok-dlya-zboru-koshtiv-na-potrebi-armiyi")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTitle(): String {
        return getString(R.string.title_about)
    }

    override fun getSubtitle(): String {
        return getString(R.string.subtitle_about)
    }
}
