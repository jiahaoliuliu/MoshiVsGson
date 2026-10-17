package com.jiahaoliuliu.moshivsgson

private fun show(label: String, decode: () -> Any) {
    val result = try {
        decode().toString()
    } catch (exception: Exception) {
        "${exception.javaClass.simpleName}: ${exception.message}"
    }
    println("$label: $result")
}

fun main() {
    println("Moshi 1.15.2 (Kotlin reflection) vs Gson 2.13.2 (default reflection)")
    Comparison.scenarios.forEach { (name, json) ->
        println("\n$name\nJSON: $json")
        show("Moshi") { Comparison.moshi(json, Weather::class.java) }
        show("Gson ") { Comparison.gson(json, Weather::class.java) }
    }
    println("\nAll-default constructor control: {}")
    show("Moshi") { Comparison.moshi("{}", DefaultWeather::class.java) }
    show("Gson ") { Comparison.gson("{}", DefaultWeather::class.java) }
    println("\nConstructor validation: blank city")
    val blankCity = """{"city":""}"""
    show("Moshi") { Comparison.moshi(blankCity, ValidatedWeather::class.java) }
    show("Gson ") { Comparison.gson(blankCity, ValidatedWeather::class.java) }
    println("\nLate failure when using a non-null property populated by Gson")
    val invalid = Comparison.gson("{}", Weather::class.java)
    show("Gson city.length") { invalid.city.length }
    println("\nGson with Unsafe allocation disabled")
    show("Gson") {
        com.google.gson.GsonBuilder().disableJdkUnsafe().create()
            .fromJson("""{"city":"Madrid"}""", Weather::class.java)
    }
}
