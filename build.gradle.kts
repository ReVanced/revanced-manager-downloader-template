plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    publishing
    signing
}

dependencies {
    implementation(libs.compose.activity)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)

    compileOnly(project(":downloader-plugin"))
}

android {
    val packageName = "app.revanced.manager.plugin.downloader.example"

    namespace = packageName
    compileSdk = 34

    defaultConfig {
        applicationId = packageName
        minSdk = 26
        targetSdk = 34
        versionName = version.toString()
        versionCode = versionName!!.filter { it.isDigit() }.toInt()

        buildConfigField("String", "PLUGIN_PACKAGE_NAME", "\"$packageName\"")
    }

    buildTypes {
        release {
            if ("CI" in System.getenv()) {
                signingConfig = signingConfigs.create("release") {
                    storeFile = file("keystore.jks")
                    storePassword = System.getenv("KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("KEYSTORE_ENTRY_ALIAS")
                    keyPassword = System.getenv("KEYSTORE_ENTRY_PASSWORD")
                }
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    applicationVariants.all {
        outputs.all {
            this as com.android.build.gradle.internal.api.ApkVariantOutputImpl

            outputFileName = "${rootProject.name}-$version.apk"
        }
    }
}

tasks {
    val assembleReleaseSignApk by registering {
        dependsOn("assembleRelease")

        val apk = layout.buildDirectory.file("outputs/apk/release/${rootProject.name}-$version.apk")

        inputs.file(apk).withPropertyName("input")
        outputs.file(apk.map { it.asFile.resolveSibling("${it.asFile.name}.asc") })

        doLast {
            signing {
                useGpgCmd()
                sign(*inputs.files.files.toTypedArray())
            }
        }
    }

    // Used by gradle-semantic-release-plugin.
    // Tracking: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435.
    publish {
        dependsOn(assembleReleaseSignApk)
    }
}
