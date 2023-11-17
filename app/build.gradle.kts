plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {

    namespace = "dev.brianchuquiruna.clonaciondevoces"
    compileSdk = 34

    signingConfigs {
        create("keystoretest") {
            storeFile = file("../ClonacionDeVoces.keystore")
            storePassword = "123456"
            keyAlias = "ClonacionDeVoces"
            keyPassword = "123456"
        }
    }

    defaultConfig {
        applicationId = "dev.brianchuquiruna.clonaciondevoces"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("keystoretest")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Image Picker
    implementation ("androidx.activity:activity:1.6.1")

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    //Glide
    implementation ("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")

    //DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    //NavComponent
    var navVersion = "2.7.1"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation ("androidx.navigation:navigation-ui-ktx:$navVersion")

    // Import the Firebase BoM
    // Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    //Firebase analitics
    implementation ("com.google.firebase:firebase-analytics-ktx")
    //Firebase Auth
    implementation("com.google.firebase:firebase-auth-ktx")
    // Firebase for the Google Play services Auth
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    //Firebase Store
    implementation("com.google.firebase:firebase-firestore-ktx")

    //MP Android Chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")



}