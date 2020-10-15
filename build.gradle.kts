plugins {
    kotlin("multiplatform") version "1.4.10"
}
group = "com.github.hjubb.solinput"
version = "0.1.0"

repositories {
    mavenCentral()
    maven { setUrl("https://kotlin.bintray.com/kotlinx") }
}
kotlin {
    macosX64 {
        binaries {
            executable {
                entryPoint = "com.github.hjubb.solinput.main"
            }
        }
    }

    linuxX64 {
        binaries {
            executable {
                entryPoint = "com.github.hjubb.solinput.main"
            }
        }
    }

    mingwX64 {
        binaries {
            executable {
                entryPoint = "com.github.hjubb.solinput.main"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
                implementation("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0-M1")
            }
        }
        val commonTest by getting
    }
}