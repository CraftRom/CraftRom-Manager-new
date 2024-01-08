package com.craftrom.manager.core.app

import android.annotation.SuppressLint
import android.content.Context

val AppContext: Context inline get() = ServiceContext.context

@SuppressLint("StaticFieldLeak")
object ServiceContext {

    lateinit var context: Context
    val deContext by lazy { context.deviceProtectedContext }
}

val Context.deviceProtectedContext: Context
    get() =
        this