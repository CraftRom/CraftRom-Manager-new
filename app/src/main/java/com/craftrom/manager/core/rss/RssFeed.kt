package com.craftrom.manager.core.rss

import com.craftrom.manager.core.NewsItem

/**
 * RSS Feed response model
 */

data class RssFeed(var items: List<NewsItem>? = null)