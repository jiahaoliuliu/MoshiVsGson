plugins {
    kotlin("jvm") version "2.2.10"
    application
}
repositories { mavenCentral() }
kotlin { jvmToolchain(21) }
dependencies {
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.google.code.gson:gson:2.13.2")
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
application { mainClass.set("com.jiahaoliuliu.moshivsgson.MainKt") }
tasks.test { useJUnitPlatform() }
