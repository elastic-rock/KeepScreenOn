import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.aboutlibs)
    alias(libs.plugins.aboutlibs.android)
}

extensions.configure<ApplicationExtension> {
    namespace = "com.elasticrock.keepscreenon"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.elasticrock.keepscreenon"
        minSdk = 24
        targetSdk = 37
        versionCode = 60
        versionName = "1.28.0"
    }

    androidResources {
        localeFilters += listOf("cs", "en-rUS", "en-rGB","el-rGR", "fr-rFR", "zh-rCN", "ar", "ja", "tr")
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    flavorDimensions += listOf("target")
    productFlavors {
        create("play") {
            dimension = "target"
            applicationId = "eu.davidweis.keepscreenon"
        }
        create("general") {
            dimension = "target"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.androidx.glance.appwidget)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.dagger.hilt)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.aboutlibs)
    implementation(libs.taskerplugin)
    implementation(libs.androidx.glance.material3)
    implementation(libs.material3)
    "playImplementation"(libs.play.review)
    "playImplementation"(libs.play.review.ktx)
}
