package com.craftrom.manager.ui.fragment.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.craftrom.manager.R
import com.craftrom.manager.core.NewsItem
import com.craftrom.manager.databinding.ItemNewsBinding

class NewsAdapter(private val newsList: List<NewsItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_CHARACTER = 0
    private val TYPE_DIVIDER = 1

    override fun getItemCount(): Int = newsList.size * 2 - 1

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) {
            TYPE_CHARACTER
        } else {
            TYPE_DIVIDER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_CHARACTER) {
            val binding = ItemNewsBinding.inflate(inflater, parent, false)
            NewsViewHolder(binding)
        } else {
            val dividerView = inflater.inflate(R.layout.item_divider_horizontal, parent, false)
            DividerViewHolder(dividerView)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NewsViewHolder) {
            val currentItemIndex = position / 2
            if (currentItemIndex >= 0 && currentItemIndex < newsList.size) {
                val currentItem = newsList[currentItemIndex]

                val binding = holder.binding
                val formattedDate = currentItem.formattedPubDate()
                val imageUrl = currentItem.image
                val imageUrlEmpty = "https://wow.zamimg.com/uploads/blog/images/35881-why-nerfing-shadow-priest-damage-wont-change-their-importance-in-mythic-guide.jpg"
                binding.categoryText.text = currentItem.category
                binding.titleTextView.text = currentItem.title
                binding.descriptionTextView.text = currentItem.description
                binding.pubDateTextView.text = "$formattedDate \u2022 ${currentItem.author}"
                // Визначаємо URL зображення або placeholder
                val loadUrl = imageUrl.takeIf { it.isNotEmpty() } ?: imageUrlEmpty
                val placeholder = R.drawable.background_gradient_three
                binding.newsImg.setImageWithPerspective(loadUrl, placeholder)


                val colorResId = when (currentItem.category) {
                    "podcast" -> R.color.color_classic
                    "news" -> R.color.color_live
                    "release" -> R.color.color_ptr
                    "greetings" -> R.color.color_tbc
                    "app" -> R.color.color_wow
                    "updates" -> R.color.color_wrath
                    else -> R.color.default_color // You can define a default color if needed
                }
                binding.categoryText.backgroundTintList =
                    ContextCompat.getColorStateList(binding.root.context, colorResId)

                binding.root.setOnClickListener {
                    // Handle the click event on the news item if necessary
                    val uri = Uri.parse(currentItem.link)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    binding.root.context.startActivity(intent)

                    // Використовуйте анімацію для ефекту натискання
                    val clickAnimation = AnimationUtils.loadAnimation(
                        binding.root.context,
                        R.anim.click_animation_scale_down
                    )
                    binding.root.startAnimation(clickAnimation)
                }
            }

        } else if (holder is DividerViewHolder) {
            // Налаштування для відображення роздільника (наприклад, зміна кольору або інші стилі)
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.divider))
        }
    }

    inner class NewsViewHolder(val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class DividerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

