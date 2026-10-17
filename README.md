# Moshi vs Gson: Kotlin constructor guarantees in practice

A small Kotlin/JVM console application comparing **Moshi 1.15.2 with Kotlin reflection** and **Gson 2.13.2 with its default reflective adapters**. No Android SDK, API key, server, or network requests are needed to run the examples. The first Gradle build needs internet access to download dependencies.

## Run

Install JDK 21 and configure `JAVA_HOME`, then:

```sh
./gradlew run
./gradlew test
```

On Windows, use `gradlew.bat`. The committed wrapper uses Gradle 9.6.0; Kotlin is pinned to 2.2.10. Versions are pinned for reproducibility, not claimed to be the newest releases.

The test report is generated at `build/reports/tests/test/index.html`.

## What it demonstrates

| Input or model | Moshi Kotlin reflection | Gson default reflection |
|---|---|---|
| Complete response | Decodes | Decodes |
| Missing required `city` | Rejects | Can construct `city=null` |
| Explicit null non-null property | Rejects | Can assign null |
| Missing defaults with a required constructor parameter | Uses Kotlin defaults | JVM defaults: null and zero |
| Explicit null nullable property | Retains null | Retains null |
| Unknown field | Ignores by default | Ignores by default |
| All constructor parameters have defaults | Uses defaults | Retains defaults through generated no-arg constructor |
| Constructor `init` validation | Executes; reflection wraps its exception | Bypassed for the demonstrated model |
| Gson Unsafe allocation disabled | Not applicable | Rejects the demonstrated model without an accessible no-arg constructor |

These results describe the exact models and configurations in this repository. They are not universal claims about every adapter, Kotlin type, JVM, or library version. In particular, this project does not benchmark performance, cover Moshi code generation, or claim complete validation of collection element nullability.

## Project layout

- `Models.kt`: three model shapes, including the all-default constructor control.
- `Comparison.kt`: shared Moshi and Gson configurations and decoding functions.
- `Main.kt`: explicit JSON examples, side-by-side output, and a safe demonstration of a late failure.
- `ComparisonTest.kt`: 13 tests verifying the observable behaviour.
- [Example output](docs/example-output.txt): captured from `./gradlew run` on JDK 21.
- [Blog article draft](docs/blog-article.md): explanation grounded in these examples.

This intentionally uses one Gradle module and inline JSON to keep the serialization behaviour visible. An application should choose its boundary validation and failure policy deliberately; adding a valid default is a business decision, not just a way to avoid a decoding error.

## References

- [Moshi Kotlin support](https://github.com/square/moshi/tree/moshi-parent-1.15.2#kotlin)
- [Gson Kotlin support limitations](https://google.github.io/gson/)
- [Gson constructor and Unsafe troubleshooting](https://google.github.io/gson/Troubleshooting.html)
