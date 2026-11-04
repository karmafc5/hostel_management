import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localPropertiesFile = rootProject.file("local.properties")
val localApiBaseUrl = if (localPropertiesFile.exists()) {
    val localProperties = Properties()
    localPropertiesFile.inputStream().use { localProperties.load(it) }
    localProperties.getProperty("api.baseUrl")
} else {
    null
}
val configuredApiBaseUrl = (
    providers.gradleProperty("apiBaseUrl").orNull
        ?: providers.environmentVariable("HOSTEL_API_BASE_URL").orNull
        ?: localApiBaseUrl
        ?: "http://10.0.2.2:8080/"
    ).trim()
require(configuredApiBaseUrl.startsWith("http://") || configuredApiBaseUrl.startsWith("https://")) {
    "Set apiBaseUrl, HOSTEL_API_BASE_URL, or api.baseUrl in local.properties to an HTTP(S) URL."
}
val apiBaseUrl = configuredApiBaseUrl.let { if (it.endsWith("/")) it else "$it/" }

android {
    namespace = "com.hostelmaintenance"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hostelmaintenance"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

tasks.register("printApiBaseUrl") {
    group = "help"
    description = "Prints the API URL embedded in this Android build."
    doLast {
        println("API_BASE_URL=$apiBaseUrl")
    }
}
