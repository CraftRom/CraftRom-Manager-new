plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    kotlin("kapt")
}
kapt {
    correctErrorTypes = true
    useBuildCache = true
    mapDiagnosticLocations = true
    javacOptions {
        option("-Xmaxerrs", 1000)
    }
}
android {
    namespace = "com.craftrom.manager"
    compileSdk = 34

    signingConfigs {
        create("sign") {
            storeFile = rootProject.file("CraftRom_Key.jks")
            storePassword = "11111991"
            keyAlias = "craftrom_key0"
            keyPassword = "11111991"
        }
    }

    defaultConfig {
        applicationId = "com.craftrom.manager"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "beta-3"
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            debugSymbolLevel = "FULL"
        }
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }

    buildTypes {
        debug {
            namespace = "com.craftrom.manager"
            versionNameSuffix = "-debug"
            applicationIdSuffix = ".debug"
            proguardFiles("proguard-rules.pro")
            isJniDebuggable = true
            signingConfig = signingConfigs.getByName("sign")
        }
        release {
            versionNameSuffix = "-release"
            applicationIdSuffix = ".release"
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
            signingConfig = signingConfigs.getByName("sign")

        }

        // Customize APK name
        applicationVariants.all {
                outputs.all {
                    (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                        "${namespace}-${defaultConfig.versionName}-${buildTypes}.apk"
                }
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        dataBinding =  true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    lint {
        disable += "MissingTranslation"
        checkReleaseBuilds = false
    }

    dependenciesInfo {
        includeInApk = false
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.compose.ui:ui-unit-android:1.6.8")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.core:core-animation:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")
    implementation("javax.inject:javax.inject:1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Image loading (remove one at a time to test conflicts)
    implementation("com.squareup.picasso:picasso:2.71828")
    // implementation("io.coil-kt:coil-compose:2.7.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("org.simpleframework:simple-xml:2.7.1") {
        exclude(group = "stax", module = "stax-api")
        exclude(group = "xpp3", module = "xpp3")
    }

    implementation("androidx.glance:glance:1.1.0")
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("org.commonmark:commonmark:0.22.0")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")
    implementation("org.commonjava.googlecode.markdown4j:markdown4j:2.2-cj-1.1")
}

