import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.example"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("com.alibaba:easyexcel:3.3.2")
                implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.7")
                implementation("org.apache.logging.log4j:log4j-core:2.7")
                implementation("androidx.datastore:datastore-preferences-core:1.1.0-dev01")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "biu"
            packageVersion = "1.0.0"
            windows {
                iconFile.set(File("./heibao.ico"))
            }
        }
    }
}
