package com.jiahaoliuliu.moshivsgson

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.squareup.moshi.JsonDataException
import kotlin.test.*

class ComparisonTest {
    private fun moshi(json: String) = Comparison.moshi(json, Weather::class.java)
    private fun gson(json: String) = Comparison.gson(json, Weather::class.java)

    @Test fun `both decode complete responses`() {
        val json = Comparison.scenarios.getValue("Complete response")
        assertEquals(Weather("Madrid", "Clear", 25.0, "Airport"), moshi(json))
        assertEquals(moshi(json), gson(json))
    }
    @Test fun `Moshi rejects missing required properties`() {
        val error = assertFailsWith<JsonDataException> { moshi("{}") }
        assertTrue(error.message.orEmpty().contains("city"))
    }
    @Test fun `Gson permits null in a non-null property then usage fails`() {
        val weather = gson("{}")
        // Observe through Any? so the compiler does not assume Kotlin nullability.
        val city: Any? = weather.city
        assertNull(city)
        assertFailsWith<NullPointerException> { weather.city.length }
    }
    @Test fun `Moshi rejects explicit null required property`() {
        assertFailsWith<JsonDataException> { moshi("""{"city":null}""") }
    }
    @Test fun `Gson also accepts explicit null required property`() {
        val city: Any? = gson("""{"city":null}""").city
        assertNull(city)
    }
    @Test fun `Moshi honours missing constructor defaults`() {
        assertEquals(Weather("Madrid"), moshi("""{"city":"Madrid"}"""))
    }
    @Test fun `Gson bypasses defaults without a no-argument constructor`() {
        val weather = gson("""{"city":"Madrid"}""")
        val condition: Any? = weather.condition
        assertNull(condition)
        assertEquals(0.0, weather.temperature)
        assertNull(weather.station)
    }
    @Test fun `explicit nullable null does not select a default`() {
        val json = Comparison.scenarios.getValue("Explicit nullable station")
        assertNull(moshi(json).station)
        assertNull(gson(json).station)
    }
    @Test fun `non-null default does not allow explicit null in Moshi`() {
        assertFailsWith<JsonDataException> { moshi("""{"city":"Madrid","condition":null}""") }
    }
    @Test fun `both ignore unknown fields by default`() {
        val json = Comparison.scenarios.getValue("Unknown field")
        assertEquals(moshi(Comparison.scenarios.getValue("Complete response")), moshi(json))
        assertEquals(moshi(json), gson(json))
    }
    @Test fun `Gson can retain defaults through generated no-argument constructor`() {
        assertEquals(DefaultWeather(), Comparison.gson("{}", DefaultWeather::class.java))
        assertEquals(DefaultWeather(), Comparison.moshi("{}", DefaultWeather::class.java))
    }
    @Test fun `constructor validation executes with Moshi and is bypassed by Gson`() {
        val json = """{"city":""}"""
        val error = assertFailsWith<java.lang.reflect.InvocationTargetException> {
            Comparison.moshi(json, ValidatedWeather::class.java)
        }
        assertIs<IllegalArgumentException>(error.cause)
        assertEquals("", Comparison.gson(json, ValidatedWeather::class.java).city)
    }
    @Test fun `disabling Unsafe prevents silent constructor bypass`() {
        assertFailsWith<JsonIOException> {
            GsonBuilder().disableJdkUnsafe().create().fromJson("{}", Weather::class.java)
        }
    }
}
