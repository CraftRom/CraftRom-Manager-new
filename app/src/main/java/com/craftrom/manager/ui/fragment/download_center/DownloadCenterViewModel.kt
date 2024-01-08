package com.craftrom.manager.ui.fragment.download_center

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DownloadCenterViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is DCenter Fragment"
    }
    val text: LiveData<String> = _text
}