package com.craftrom.manager.core.rss

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * A [converter][Converter.Factory] which uses [XMLParser] to parse RSS feeds.
 */
class RssConverterFactory private constructor() : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        Log.d("RssConverterFactory", "Creating responseBodyConverter")
        return RssResponseBodyConverter()
    }

    companion object {
        /**
         * Creates an instance
         *
         * @return instance
         */
        fun create(): RssConverterFactory {
            Log.d("RssConverterFactory", "Creating RssConverterFactory")
            return RssConverterFactory()
        }
    }
}
