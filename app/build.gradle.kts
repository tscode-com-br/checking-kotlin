import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
}

val releaseVersionName = providers.gradleProperty("checking.versionName").orElse("2.0.0-alpha")
val releaseVersionCode = providers.gradleProperty("checking.versionCode").map(String::toInt).orElse(17)

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

val requiresReleaseSigning = gradle.startParameter.taskNames.any { name ->
    val lower = name.lowercase()
    lower.contains("bundlerelease") || lower.contains("assemblerelease") || lower.contains("publish")
}

fun requireKeystoreProperty(name: String): String {
    val value = keystoreProperties.getProperty(name, "").trim()
    require(value.isNotEmpty()) { "Missing keystore.properties '$name' for release build." }
    require(value != "change-me") { "keystore.properties still has placeholder for '$name'." }
    return value
}

android {
    namespace = "br.com.tscode.checking"
    compileSdk = 36

    defaultConfig {
        // applicationId matches the production Flutter app for in-place Play Store cutover (§23.0/§23.16-Q6).
        applicationId = "com.br.checking"
        // minSdk 24 (Android 7.0) matches the previous Flutter release — raising it would drop
        // ~API 24/25 device models from the Play catalog. Safe because core library desugaring
        // (below) backports java.time etc. lintVitalRelease guards any unguarded API 26+ calls.
        minSdk = 24
        targetSdk = 36
        versionCode = releaseVersionCode.get()
        versionName = releaseVersionName.get()

        testInstrumentationRunner = "br.com.tscode.checking.HiltTestRunner"

        // Common BuildConfig fields (§5).
        buildConfigField("String", "API_PREFIX", "\"/api/web\"")
        buildConfigField("String", "WHATSAPP_SUPPORT_NUMBER", "\"\"") // TODO: fill before release
    }

    signingConfigs {
        create("release") {
            if (requiresReleaseSigning) {
                storeFile = rootProject.file(requireKeystoreProperty("storeFile"))
                storePassword = requireKeystoreProperty("storePassword")
                keyAlias = requireKeystoreProperty("keyAlias")
                keyPassword = requireKeystoreProperty("keyPassword")
            } else {
                val configured = keystoreProperties.getProperty("storeFile", "").trim()
                if (configured.isNotEmpty()) {
                    storeFile = rootProject.file(configured)
                    storePassword = keystoreProperties.getProperty("storePassword", "")
                    keyAlias = keystoreProperties.getProperty("keyAlias", "")
                    keyPassword = keystoreProperties.getProperty("keyPassword", "")
                }
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            buildConfigField("String", "BASE_URL", "\"https://tscode.com.br\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "BASE_URL", "\"https://tscode.com.br\"")
            // Bundle native debug symbols (CameraX ships .so) so Play can symbolicate native
            // crashes/ANRs — resolves the "no debug symbols uploaded" warning.
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)

    coreLibraryDesugaring(libs.desugar.jdk)

    // AndroidX core
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.activity.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Compose
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler.androidx)

    // Networking: OkHttp + Retrofit + kotlinx.serialization
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.sse)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)

    // Persistence
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)

    // Background
    implementation(libs.workmanager)

    // Location (FusedLocationProviderClient + GeofencingClient)
    implementation(libs.play.services.location)

    // CameraX — video recording for accident mode (§12.3)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.video)
    implementation(libs.camera.view)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Unit tests
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)

    // Instrumented tests
    androidTestImplementation(libs.test.core)
    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
