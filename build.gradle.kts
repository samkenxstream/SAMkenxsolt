plugins {
    kotlin("multiplatform") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
}
group = "com.github.hjubb.solcsjw"
version = "0.1.0"

repositories {
    mavenCentral()
    maven {
        setUrl("https://kotlin.bintray.com/kotlinx")
    }
    maven {
        setUrl("https://repo.binom.pw/releases")
    }
}
kotlin {
    macosX64 {
        binaries {
            executable {
                entryPoint = "com.github.hjubb.solcsjw.main"
            }
        }
    }

    linuxX64 {
        binaries {
            executable {
                entryPoint = "com.github.hjubb.solcsjw.main"
            }
        }
    }

    mingwX64 {
        binaries {
            executable {
                entryPoint = "com.github.hjubb.solcsjw.main"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
                api("pw.binom.io:file:0.1.22")
                api("pw.binom.io:core:0.1.22")
            }
        }
        val commonTest by getting
    }
}
