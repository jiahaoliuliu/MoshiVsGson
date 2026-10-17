package com.jiahaoliuliu.moshivsgson

import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object Comparison {
    val gson: Gson = Gson()
    val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    val scenarios = linkedMapOf(
        "Complete response" to """{"city":"Madrid","condition":"Clear","temperature":25.0,"station":"Airport"}""",
        "Missing required city" to """{"condition":"Clear"}""",
        "Explicit null city" to """{"city":null}""",
        "Missing optional properties" to """{"city":"Madrid"}""",
        "Explicit nullable station" to """{"city":"Madrid","condition":"Clear","temperature":25.0,"station":null}""",
        "Explicit null condition" to """{"city":"Madrid","condition":null}""",
        "Unknown field" to """{"city":"Madrid","condition":"Clear","temperature":25.0,"station":"Airport","extra":true}""",
    )

    fun <T : Any> moshi(json: String, type: Class<T>): T =
        requireNotNull(moshi.adapter(type).fromJson(json))

    fun <T : Any> gson(json: String, type: Class<T>): T = gson.fromJson(json, type)
}
