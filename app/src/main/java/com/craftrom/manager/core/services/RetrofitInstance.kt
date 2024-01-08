package com.craftrom.manager.core.services

import android.util.Log
import com.craftrom.manager.core.rss.RssConverterFactory
import com.craftrom.manager.core.rss.RssFeed
import retrofit2.Call
import retrofit2.Retrofit

object RetrofitInstance {

    fun setupRetrofitCall(feedsBaseUrl: String, feedsUrlEndPoint: String): Call<RssFeed> {
        Log.d("RetrofitInstance", "Setting up Retrofit call with base URL: $feedsBaseUrl and endpoint: $feedsUrlEndPoint")

        val retrofit = Retrofit.Builder()
            .baseUrl(feedsBaseUrl)
            .addConverterFactory(RssConverterFactory.create())
            .build()

        val service = retrofit.create(RssService::class.java)

        val call = service.getRss(feedsUrlEndPoint)
        Log.d("RetrofitInstance", "Retrofit call created successfully")
        return call
    }
}
