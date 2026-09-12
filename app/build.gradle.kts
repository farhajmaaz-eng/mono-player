plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android { namespace = "com.monoplayer.app"; compileSdk = 35
    defaultConfig { applicationId = "com.monoplayer.app"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0.0" }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation(libs.androidx.core.ktx); implementation(libs.androidx.lifecycle.runtime); implementation(libs.androidx.lifecycle.compose); implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose); implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.compose.bom)); implementation(libs.compose.ui); implementation(libs.compose.ui.preview); implementation(libs.compose.material3); implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.media3.exoplayer); implementation(libs.media3.session); implementation(libs.media3.ui); implementation(libs.coil.compose)
    implementation(libs.androidx.room.runtime); implementation(libs.androidx.room.ktx); ksp(libs.androidx.room.compiler)
    testImplementation("junit:junit:4.13.2")
}
