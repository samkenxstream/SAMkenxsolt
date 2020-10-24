plugins {
    kotlin("multiplatform") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
}

group = "com.github.hjubb"
version = "0.4.0"

val binomVersion = "0.1.22"
val ktorVersion = "1.4.0"

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
                entryPoint = "com.github.hjubb.solt.main"
            }
        }
    }

    linuxX64 {
        binaries {
            executable {
                entryPoint = "com.github.hjubb.solt.main"
            }
        }
    }

    mingwX64 {
        binaries {
            executable {
                entryPoint = "com.github.hjubb.solt.main"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
                api("pw.binom.io:file:$binomVersion")
                api("pw.binom.io:core:$binomVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9-native-mt") {
                    version {
                        strictly("1.3.9-native-mt")
                    }
                }
            }
        }
        val commonTest by getting
    }
}
