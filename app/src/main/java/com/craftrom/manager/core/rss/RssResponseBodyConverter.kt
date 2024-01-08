package com.craftrom.manager.core.rss


import android.util.Log
import okhttp3.ResponseBody
import org.xml.sax.InputSource
import retrofit2.Converter
import java.io.InputStreamReader
import javax.xml.parsers.SAXParserFactory

internal class RssResponseBodyConverter : Converter<ResponseBody, RssFeed> {

    override fun convert(value: ResponseBody): RssFeed {
        val rssFeed = RssFeed()
        try {
            val parser = XMLParser()
            val parserFactory = SAXParserFactory.newInstance()
            val saxParser = parserFactory.newSAXParser()
            val xmlReader = saxParser.xmlReader

            // Включаем логирование
            xmlReader.setFeature("http://xml.org/sax/features/namespaces", true)
            xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", false)

            xmlReader.contentHandler = parser
            val inputSource = InputSource(InputStreamReader(value.byteStream()))
            xmlReader.parse(inputSource)
            val items = parser.getFilteredNewsItems()
            rssFeed.items = items

            // Додаємо логування успішного парсингу
            Log.d("RssResponseBodyConverter", "Successfully parsed RSS response. Items count: ${items.size}")

        } catch (e: Exception) {
            e.printStackTrace()
            // Логуємо помилку
            Log.e("RssResponseBodyConverter", "Error converting RSS response: ${e.message}")
        }

        return rssFeed
    }
}



