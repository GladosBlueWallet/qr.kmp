import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.bluewallet"
version = "0.0.1"

kotlin {
    jvm()
    androidLibrary {
        namespace = "io.bluewallet.qr"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava()
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "qr", version.toString())

    pom {
        name = "qr"
        description = "Kotlin Multiplatform QR code decoder (fork of limpbrains/qr)."
        inceptionYear = "2026"
        url = "https://github.com/GladosBlueWallet/qr.kmp/"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "overtorment"
                name = "Overtorment"
                url = "https://github.com/Overtorment/"
            }
        }
        scm {
            url = "https://github.com/GladosBlueWallet/qr.kmp/"
            connection = "scm:git:git://github.com/GladosBlueWallet/qr.kmp.git"
            developerConnection = "scm:git:ssh://git@github.com/GladosBlueWallet/qr.kmp.git"
        }
    }
}
