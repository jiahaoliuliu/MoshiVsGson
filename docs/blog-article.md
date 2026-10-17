# A non-null Kotlin property can still be null: Moshi vs Gson in practice

Kotlin makes it easy to express what a model expects. A city name can be required, a station can be nullable, and a missing condition can have a default. But those declarations only help if the code creating the object respects them.

While working on a weather app, I wanted to understand what that means at the JSON boundary. Instead of comparing feature lists, I built a small Kotlin project that gives the same JSON to Moshi and Gson.

The interesting difference was not whether they could read valid JSON. Both could. It was what happened when the response did not match the Kotlin constructor.

The [complete project](https://github.com/jiahaoliuliu/MoshiVsGson) contains runnable examples and 13 tests. The observations below use Kotlin 2.2.10, JDK 21, Moshi 1.15.2 with `KotlinJsonAdapterFactory`, and Gson 2.13.2 with its default adapters. This is a behaviour comparison, not a performance benchmark.

## Start with the same model

```kotlin
data class Weather(
    val city: String,
    val condition: String = "Unknown",
    val temperature: Double = 20.0,
    val station: String? = "Not reported",
)
```

`city` has no default. `condition` and `temperature` have defaults. `station` can be null, but an omitted station has a non-null default. The arbitrary temperature default makes constructor behaviour easy to see; it would be a poor choice for real missing weather data.

The configuration is deliberately small:

```kotlin
val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

val gson = Gson()
```

Moshi uses its Kotlin reflection adapter here. I am not using generated adapters or custom Gson adapters.

## A valid response works with both

For this JSON:

```json
{"city":"Madrid","condition":"Clear","temperature":25.0,"station":"Airport"}
```

both produce the same object. This control matters: the example is about mismatches between incoming data and Kotlin expectations, rather than a general inability to decode a Kotlin class.

## What if a required property is missing?

Give both libraries an empty object:

```json
{}
```

Moshi rejects it because `city` is required. Gson creates an object whose `city` is null, even though its declaration is `String`.

That object can travel through the application before anything goes wrong. This access then fails:

```kotlin
val weather = gson.fromJson("{}", Weather::class.java)
println(weather.city.length) // NullPointerException
```

The compiler has no reason to require a safe call: the property is declared non-null. The deserialized object has violated that expectation.

This is why failure location matters. An error while decoding points toward an invalid response. An error later while rendering a screen can make the same problem harder to trace.

## Missing values and explicit null are different

For this input:

```json
{"city":"Madrid"}
```

Moshi produces:

```text
Weather(city=Madrid, condition=Unknown, temperature=20.0, station=Not reported)
```

Gson produces:

```text
Weather(city=Madrid, condition=null, temperature=0.0, station=null)
```

For this model, Gson bypasses constructor initialization. The missing fields therefore have JVM defaults rather than the defaults written in Kotlin.

Now consider an explicit `"station": null`. Both retain null. With Moshi, a constructor default handles an absent property; it does not replace an explicit null for a nullable property.

Likewise, `condition = "Unknown"` does not make `"condition": null` valid for Moshi. The property is still non-null. Missing and explicitly null are separate cases worth testing.

## The counterexample: Gson can preserve defaults

It would be misleading to conclude that Gson always ignores Kotlin defaults. Change the model:

```kotlin
data class DefaultWeather(
    val city: String = "Madrid",
    val condition: String = "Unknown",
)
```

Every constructor parameter now has a default, so Kotlin generates a no-argument constructor. In this experiment, Gson invokes it and retains the defaults when decoding `{}`. Moshi produces the same result.

The difference comes from how the object can be instantiated. Adding a required constructor parameter can change Gson's behaviour, even for other fields whose declarations have not changed.

Adding defaults everywhere is not automatically a fix. A required city with a fabricated fallback may hide a response problem instead of handling it correctly.

## Constructor validation is another boundary

Consider a model with an invariant:

```kotlin
data class ValidatedWeather(val city: String) {
    init {
        require(city.isNotBlank()) { "City must not be blank" }
    }
}
```

For `{"city":""}`, Moshi invokes the constructor and the validation fails. With the pinned reflection implementation, the underlying `IllegalArgumentException` appears inside an `InvocationTargetException`.

Gson constructs this particular model without running the constructor, so the blank city is accepted. If constructor validation is part of your correctness model, that difference is significant.

This also separates structural and semantic validation. A non-null string may still be blank. A successfully decoded weather response may still need domain validation.

## Make constructor bypass visible

Gson provides a useful diagnostic configuration:

```kotlin
val gson = GsonBuilder()
    .disableJdkUnsafe()
    .create()
```

For the original `Weather` class, decoding then fails because Gson cannot use Unsafe allocation and the model has no accessible no-argument constructor.

This option does not add Kotlin nullability support or synthesize Kotlin constructor calls. It exposes an allocation dependency that could otherwise remain unnoticed. Depending on the application, a custom adapter or an explicitly validated boundary model may be appropriate.

## What this changes in an Android app

With Retrofit, the converter sits between the HTTP response and the repository's DTO-to-domain mapper. The repository interface and Compose screen do not need to know which JSON library is used.

The practical decision is where invalid input becomes an error, and what guarantees the mapper can rely on. Moshi's Kotlin adapter gives useful constructor-level checks in these examples, but the application still needs a policy for absent measurements, invalid conditions, and user-visible failures.

I would test complete JSON, missing required properties, explicit nulls, and optional values using the same configuration as production. Mocking a repository cannot verify these decoding behaviours.

There are limits to this experiment. It does not establish performance, code-generation behaviour, or complete validation of arbitrary generic and nested types. Both libraries also ignore unknown fields by default in the tested configuration, which is useful when a server adds fields an older client does not use.

The lesson I take from the experiment is to verify object construction rather than assume that a Kotlin type declaration makes every deserialized instance valid. Small tests at the JSON boundary can reveal a problem long before a screen tries to use the result.

## Run it yourself

With JDK 21 configured:

```sh
./gradlew run
./gradlew test
```

No Android SDK, API key, or backend is required. The repository includes the Gradle wrapper and captured console output.

## References

- [Moshi 1.15.2 Kotlin support](https://github.com/square/moshi/tree/moshi-parent-1.15.2#kotlin)
- [Gson's documented Kotlin limitations](https://google.github.io/gson/)
- [Gson troubleshooting: constructor initialization and Unsafe](https://google.github.io/gson/Troubleshooting.html)
