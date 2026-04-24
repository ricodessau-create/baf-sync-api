plugins {
    kotlin("jvm") version "1.9.0"
    application
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.6")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.6")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-serialization-gson:2.3.6")

    implementation("com.google.firebase:firebase-admin:9.2.0")
}

application {
    mainClass.set("eu.baf.syncapi.MainKt")
}
