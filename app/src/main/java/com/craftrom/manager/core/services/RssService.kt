package com.craftrom.manager.core.services

import com.craftrom.manager.core.rss.RssFeed
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface RssService {
    @GET
    fun getRss(@Url url: String): Call<RssFeed>
}