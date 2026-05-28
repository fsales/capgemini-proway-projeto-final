plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val mastraEmulatorBaseUrl = providers.gradleProperty("MASTRA_EMULATOR_BASE_URL")
    .orElse(providers.environmentVariable("MASTRA_EMULATOR_BASE_URL"))
    .orElse("http://10.0.2.2:4111/api/")
val mastraDeviceBaseUrl = providers.gradleProperty("MASTRA_DEVICE_BASE_URL")
    .orElse(providers.environmentVariable("MASTRA_DEVICE_BASE_URL"))
    .orElse("http://192.168.0.100:4111/api/")
val mastraAgentId = providers.gradleProperty("MASTRA_AGENT_ID")
    .orElse(providers.environmentVariable("MASTRA_AGENT_ID"))
    .orElse("card-spending-agent")
val chatUseMock = providers.gradleProperty("CHAT_USE_MOCK")
    .orElse(providers.environmentVariable("CHAT_USE_MOCK"))
    .orElse("false")

android {
    namespace = "com.app.gerenciadorcartoes"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    signingConfigs {
        named("debug") {
            storeFile     = file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias      = "androiddebugkey"
            keyPassword   = "android"
        }
    }

    defaultConfig {
        applicationId             = "com.app.gerenciadorcartoes"
        minSdk                    = 28
        targetSdk                 = 36
        versionCode               = 1
        versionName               = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MASTRA_EMULATOR_BASE_URL", "\"${mastraEmulatorBaseUrl.get()}\"")
        buildConfigField("String", "MASTRA_DEVICE_BASE_URL", "\"${mastraDeviceBaseUrl.get()}\"")
        buildConfigField("String", "MASTRA_AGENT_ID", "\"${mastraAgentId.get()}\"")
        buildConfigField("boolean", "CHAT_USE_MOCK", chatUseMock.get())
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Serialização (Kotlinx)
    implementation(libs.kotlinx.serialization.json)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Hilt (Injeção de dependências)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // WorkManager + Hilt Work (para sincronização em background)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // Credential Manager / Google Sign-In
    implementation(libs.google.credential.manager)
    implementation(libs.google.credential.play)
    implementation(libs.google.identity.googleid)
    implementation(libs.google.play.services.auth)

    // Rede (Retrofit + OkHttp)
    implementation(libs.retrofit)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    // Testes unitários
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    // ArchUnit - regras de arquitetura (JUnit4 integration)
    testImplementation(libs.archunit.junit4)

    // Testes instrumentados (Android)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
