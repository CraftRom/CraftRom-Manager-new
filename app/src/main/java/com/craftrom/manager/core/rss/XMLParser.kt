package com.craftrom.manager.core.rss

import android.util.Log
import com.craftrom.manager.core.NewsItem
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.util.*

/**
 * RSS Feed XML parser
 */
internal class XMLParser : DefaultHandler() {

    private var elementOn = false
    private var parsingAuthor = false
    private var parsingCategory = false
    private var parsingTitle = false
    private var parsingDescription = false
    private var parsingPubDate = false
    private var parsingMediaContent = false

    private var elementValue: String? = null
    private var author = EMPTY_STRING
    private var category = EMPTY_STRING
    private var title = EMPTY_STRING
    private var link: String? = null
    private var description: String? = null
    private var pubDate: String? = null
    private var image: String? = null
    private val items = mutableListOf<NewsItem>()
    private var currentItem: NewsItem? = null

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes?) {
        Log.d("Parser", "Start element: $qName, Namespace URI: $uri")
        elementOn = true
        when (localName.lowercase(Locale.getDefault())) {
            ITEM -> currentItem = NewsItem("", "", "", "", "", "", "")
            AUTHOR -> {
                parsingAuthor = true
                author = EMPTY_STRING
            }
            TITLE -> {
                parsingTitle = true
                title = EMPTY_STRING
            }
            CATEGORY -> {
                parsingCategory = true
                category = EMPTY_STRING
            }
            DESCRIPTION -> {
                parsingDescription = true
                description = EMPTY_STRING
            }
            PUB_DATE -> {
                parsingPubDate = true
                pubDate = EMPTY_STRING
            }
            IMG_URL -> {
                parsingMediaContent = true
                image = EMPTY_STRING
            }
            LINK -> if (qName != ATOM_LINK) {
                link = EMPTY_STRING
            }
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        elementOn = false
        currentItem?.let {
            when (localName.lowercase(Locale.getDefault())) {
                ITEM -> {
                    items.add(it)
                    currentItem = null
                }
                AUTHOR -> {
                    parsingAuthor = false
                    if (author.isNotEmpty()) {
                        it.author = author
                    }
                }
                CATEGORY -> {
                    parsingCategory = false
                    category = elementValue?.trim() ?: EMPTY_STRING
                    if (category.isNotEmpty()) {
                        it.category = category
                    }
                }
                TITLE -> {
                    parsingTitle = false
                    title = elementValue?.trim() ?: EMPTY_STRING
                    it.title = title
                }
                DESCRIPTION -> {
                    parsingDescription = false
                    if (description?.isNotEmpty()!!) {
                        it.description = description.toString()
                    }
                }
                PUB_DATE -> {
                    parsingPubDate = false
                    it.pubDate = elementValue?.trim() ?: EMPTY_STRING
                }
                IMG_URL -> {
                    parsingMediaContent = false
                    image = elementValue?.trim() ?: EMPTY_STRING
                    it.image = image as String
                }
                LINK -> if (!elementValue.isNullOrBlank()) {
                    link = elementValue?.trim()
                    it.link = link ?: EMPTY_STRING
                }
            }
        }
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray, start: Int, length: Int) {
        val buff = String(ch, start, length).trim()
        if (elementOn) {
            elementValue = buff
            elementOn = false
        }
        if (parsingAuthor) {
            author += buff
            Log.d("XMLParser", "Author value: $author")
        }
        if (parsingCategory) {
            category += buff
            Log.d("XMLParser", "Category value: $category")
        }
        if (parsingTitle) {
            val builder = StringBuilder(title)
            builder.append(buff)
            title = builder.toString()
            Log.d("XMLParser", "Title value: $title")
        }
        if (parsingDescription) {
            description = buff
        }
        if (parsingPubDate) {
            pubDate = (pubDate?.plus(buff))?.trim()
            Log.d("XMLParser", "PubDate value: $pubDate")
        }
        if (parsingMediaContent) {
            image = (image?.plus(buff))?.trim()
        }
    }

    fun getNewsItems(): List<NewsItem> {
        return items
    }

    fun getFilteredNewsItems(): List<NewsItem> {
        val allowedCategories = listOf("news", "greetings", "podcast", "updates", "release", "app")
        return items.filter { it.category.isNotEmpty() && it.category in allowedCategories }
    }

    companion object {
        private const val EMPTY_STRING = ""
        private const val EMPTY_IMG = "https://wow.zamimg.com/uploads/blog/images/35881-why-nerfing-shadow-priest-damage-wont-change-their-importance-in-mythic-guide.jpg"
        private const val ITEM = "item"
        private const val AUTHOR = "author"
        private const val CATEGORY = "category"
        private const val TITLE = "title"
        private const val DESCRIPTION = "description"
        private const val PUB_DATE = "pubdate"
        private const val IMG_URL = "image"
        private const val LINK = "link"
        private const val ATOM_LINK = "atom:link"
    }
}
