package com.craftrom.manager.core

import com.craftrom.manager.R
import com.craftrom.manager.core.app.ServiceContext.context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NewsItem(
    var category: String = "",
    var title: String = "",
    var description: String = "",
    var pubDate: String = "",
    var image: String = "",
    var link: String = ""
){
    fun formattedPubDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val currentDate = Date()
        val pubDateTime = inputFormat.parse(pubDate)
        val timeDiff = currentDate.time - pubDateTime!!.time
        val minutes = timeDiff / (1000 * 60)
        val hours = timeDiff / (1000 * 60 * 60)
        val days = timeDiff / (1000 * 60 * 60 * 24)

        return when {
            minutes < 60 -> context.getString(R.string.minutes_ago, minutes)
            hours < 24 -> context.getString(R.string.hours_ago, hours)
            days <= 7 -> context.getString(R.string.days_ago, days)
            else -> outputFormat.format(pubDateTime)
        }
    }
}