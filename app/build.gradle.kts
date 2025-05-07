plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "tt.rxble"
    compileSdk = 36

    defaultConfig {
        applicationId = "tt.rxble"
        minSdk = 24
        targetSdk = 36
        versionCode = 111
        versionName = "1.1.1"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.ttbase)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)
    implementation(project(":rxble"))
}