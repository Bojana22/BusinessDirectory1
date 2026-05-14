plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.businessdirectory1"
    compileSdk = 34 // Променив на 34 бидејќи 36 е пренова верзија и може да ти прави проблеми

    defaultConfig {
        applicationId = "com.example.businessdirectory1"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    implementation("com.google.android.gms:play-services-location:21.0.1")

    // ROOM библиотеки
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    testImplementation("junit:junit:4.13.2")
}