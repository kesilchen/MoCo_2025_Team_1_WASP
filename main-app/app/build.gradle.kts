plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "io.moxd.mocohands_on"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.moxd.mocohands_on"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    flavorDimensions += listOf("version", "data", "visualization")
    productFlavors {
        create("demo") {
            dimension = "version"
            versionNameSuffix = "-demo"
            buildConfigField("Boolean", "SHOW_DEBUG_SCREEN", "true")
        }
        create("full") {
            dimension = "version"
            versionNameSuffix = "-full"
            buildConfigField("Boolean", "SHOW_DEBUG_SCREEN", "false")
        }
        create("fake") {
            dimension = "data"
            versionNameSuffix = "-fake"
            buildConfigField("Boolean", "USE_FAKE_DATA", "true")
        }
        create("real") {
            dimension = "data"
            versionNameSuffix = "-real"
            buildConfigField("Boolean", "USE_FAKE_DATA", "false")
        }
        create("compass") {
            dimension = "visualization"
            versionNameSuffix = "-compass"
            buildConfigField("String", "VISUALIZATION_TYPE", "\"COMPASS\"")
        }
        create("pov") {
            dimension = "visualization"
            versionNameSuffix = "-pov"
            buildConfigField("String", "VISUALIZATION_TYPE", "\"POV\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.uwb)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.kotlinx.coroutines.guava)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}