package com.craftrom.manager.ui.fragment.download_center

import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.craftrom.manager.R
import com.craftrom.manager.core.FileInfo
import com.craftrom.manager.databinding.ItemFileBinding

class FilesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_FILE = 0
    private val TYPE_DIVIDER = 1

    private var items: List<FileInfo> = emptyList()

    fun setItems(items: List<FileInfo>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size * 2 - 1

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) {
            TYPE_FILE
        } else {
            TYPE_DIVIDER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_FILE) {
            val binding = ItemFileBinding.inflate(inflater, parent, false)
            FileViewHolder(binding)
        } else {
            val dividerView = inflater.inflate(R.layout.item_divider_horizontal, parent, false)
            DividerViewHolder(dividerView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FileViewHolder) {
            val currentItemIndex = position / 2
            val item = items[currentItemIndex]
            holder.bind(item)
        }
    }

    class FileViewHolder(private val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(fileInfo: FileInfo) {
            binding.apply {
                fileNameTextView.text = fileInfo.name
                fileSizeTextView.text = itemView.context.getString(R.string.file_size_format, fileInfo.size)
                lastUpdatedTextView.text = fileInfo.lastUpdated
                downloadLinkBtn.setOnClickListener {
                    val uri = Uri.parse(fileInfo.downloadLink)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    binding.root.context.startActivity(intent)
                }
            }
        }
    }

    inner class DividerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            // Зміна колору фону роздільника
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.divider))

        }
    }

}

