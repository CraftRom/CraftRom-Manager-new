package com.craftrom.manager.core.utils.interfaces

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface EmptyCheckListener {
    fun checkEmpty(recyclerView: RecyclerView, emptyView: View)
}
