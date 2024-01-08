package com.craftrom.manager.ui.fragment.download_center

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.craftrom.manager.MainActivity
import com.craftrom.manager.R
import com.craftrom.manager.databinding.FragmentDownloadCenterBinding


class DownloadCenterFragment : Fragment() {

    private var _binding: FragmentDownloadCenterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onStart() {
        super.onStart()
        val mainActivity = requireActivity() as MainActivity
        mainActivity.setToolbarText(getTitle(), getSubtitle())
    }
    override fun onResume() {
        super.onResume()
        val mainActivity = requireActivity() as MainActivity
        mainActivity.setToolbarText(getTitle(), getSubtitle())
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val dViewModel =
            ViewModelProvider(this)[DownloadCenterViewModel::class.java]

        _binding = FragmentDownloadCenterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        dViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    private fun getTitle() = getString(R.string.title_dcenter)
    private fun getSubtitle() = getString(R.string.subtitle_dcenter)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}