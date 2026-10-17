package com.jiahaoliuliu.moshivsgson

// A required parameter prevents Kotlin from generating a no-argument constructor.
data class Weather(
    val city: String,
    val condition: String = "Unknown",
    val temperature: Double = 20.0,
    val station: String? = "Not reported",
)

// Control: Kotlin generates a no-argument constructor when all parameters default.
data class DefaultWeather(
    val city: String = "Madrid",
    val condition: String = "Unknown",
)

// Constructor validation is distinct from JSON type validation.
data class ValidatedWeather(val city: String) {
    init { require(city.isNotBlank()) { "City must not be blank" } }
}
