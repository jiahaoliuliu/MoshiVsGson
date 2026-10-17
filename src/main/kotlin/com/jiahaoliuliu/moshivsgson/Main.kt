package com.jiahaoliuliu.moshivsgson

// Print it out the result or the exception
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

    // Complete response
    val completeResponse = """{"city":"Madrid","condition":"Clear","temperature":25.0,"station":"Airport"}"""
    println("\nComplete response\nJSON: $completeResponse")
    show("Moshi") { Comparison.moshi(completeResponse, Weather::class.java) }
    show("Gson ") { Comparison.gson(completeResponse, Weather::class.java) }

    // Missing required city
    val missingRequiredCity = """{"condition":"Clear"}"""
    println("\nMissing required city\nJSON: $missingRequiredCity")
    show("Moshi") { Comparison.moshi(missingRequiredCity, Weather::class.java) }
    show("Gson ") { Comparison.gson(missingRequiredCity, Weather::class.java) }

    // Explicit null city
    val nullCity = """{"city":null}"""
    println("\nExplicit null city\nJSON: $nullCity")
    show("Moshi") { Comparison.moshi(nullCity, Weather::class.java) }
    show("Gson ") { Comparison.gson(nullCity, Weather::class.java) }

    // Missing optional properties
    val missingOptionalProperties = """{"city":"Madrid"}"""
    println("\nMissing optional properties\nJSON: $missingOptionalProperties")
    show("Moshi") { Comparison.moshi(missingOptionalProperties, Weather::class.java) }
    show("Gson ") { Comparison.gson(missingOptionalProperties, Weather::class.java) }

    // Explicit nullable station
    val nullableStation = """{"city":"Madrid","condition":"Clear","temperature":25.0,"station":null}"""
    println("\nExplicit nullable station\nJSON: $nullableStation")
    show("Moshi") { Comparison.moshi(nullableStation, Weather::class.java) }
    show("Gson ") { Comparison.gson(nullableStation, Weather::class.java) }

    // Explicit null condition
    val nullCondition = """{"city":"Madrid","condition":null}"""
    println("\nExplicit null condition\nJSON: $nullCondition")
    show("Moshi") { Comparison.moshi(nullCondition, Weather::class.java) }
    show("Gson ") { Comparison.gson(nullCondition, Weather::class.java) }

    // Unknown field
    val unknownField = """{"city":"Madrid","condition":"Clear","temperature":25.0,"station":"Airport","extra":true}"""
    println("\nUnknown field\nJSON: $unknownField")
    show("Moshi") { Comparison.moshi(unknownField, Weather::class.java) }
    show("Gson ") { Comparison.gson(unknownField, Weather::class.java) }

    // When all the fields are default
    println("\nAll-default constructor control: {}")
    show("Moshi") { Comparison.moshi("{}", DefaultWeather::class.java) }
    show("Gson ") { Comparison.gson("{}", DefaultWeather::class.java) }

    // When the required field is blank
    println("\nConstructor validation: blank city")
    val blankCity = """{"city":""}"""
    show("Moshi") { Comparison.moshi(blankCity, ValidatedWeather::class.java) }
    show("Gson ") { Comparison.gson(blankCity, ValidatedWeather::class.java) }

    // Data constructed with a non-null property
    println("\nLate failure when using a non-null property populated by Gson")
    val invalid = Comparison.gson("{}", Weather::class.java)
    show("Gson city.length") { invalid.city.length }

    // Gson disabling the unsafe allocation
    println("\nGson with Unsafe allocation disabled")
    show("Gson") {
        com.google.gson.GsonBuilder().disableJdkUnsafe().create()
            .fromJson("""{"city":"Madrid"}""", Weather::class.java)
    }
}
