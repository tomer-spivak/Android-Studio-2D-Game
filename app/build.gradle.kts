plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
}

android {
    namespace = "tomer.spivak.androidstudio2dgame"
    compileSdk = 35

    defaultConfig {
        applicationId = "tomer.spivak.androidstudio2dgame"
        minSdk = 26
        targetSdk = 35
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets {
        getByName("main") {
            res {
                srcDirs("src\\main\\res", "src\\main\\res\\layout\\buildings",
                    "src\\main\\res",
                    "src\\main\\res\\layouts", "src\\main\\res", "src\\main\\res\\folder",
                    "src\\main\\res",
                    "src\\main\\res\\layout\\mainn", "src\\main\\res", "src\\main\\res\\layout\\login"
                )
            }
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation (libs.circleimageview)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.glide)

    implementation (libs.recyclerview)

    implementation (libs.firebase.storage)

    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.firestore)
    implementation (libs.firebase.auth)
    implementation (libs.cardview)
    implementation (libs.android.mail)
    implementation (libs.android.activation)
    implementation (libs.play.services.auth)



}
