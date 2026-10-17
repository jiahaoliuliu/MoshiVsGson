package com.jiahaoliuliu.moshivsgson

import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object Comparison {
    val gson: Gson = Gson()
    val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    fun <T : Any> moshi(json: String, type: Class<T>): T =
        requireNotNull(moshi.adapter(type).fromJson(json))

    fun <T : Any> gson(json: String, type: Class<T>): T = gson.fromJson(json, type)
}
