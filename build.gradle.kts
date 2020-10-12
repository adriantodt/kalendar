import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("multiplatform") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    application
}
group = "net.adriantodt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
    maven { url = uri("https://dl.bintray.com/nanoflakes/maven") }
    maven { url = uri("https://kotlin.bintray.com/js-externals") }
}
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }
    js {
        useCommonJs()
        browser {
            binaries.executable()
            webpackTask {
                cssSupport.enabled = true
            }
            runTask { cssSupport.enabled = true }
//            testTask {
//                useKarma {
//                    useChromeHeadless()
//                    webpackConfig.cssSupport.enabled = true
//                }
//            }
        }
    }
    sourceSets {
        val kotlinxHtmlVersion = "0.7.2"
        val nanoflakesVersion = "1.2.1"
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
                implementation("com.github.nanoflakes:nanoflakes-kotlin:$nanoflakesVersion")
                implementation("com.github.nanoflakes:nanoflakes-kotlin-metadata:$nanoflakesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            val jdbiVersion = "3.16.0"
            val jacksonVersion = "2.11.3"
            dependencies {
                implementation(kotlin("reflect"))
                implementation("io.javalin:javalin:3.11.0")
                implementation("ch.qos.logback:logback-classic:1.2.3")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinxHtmlVersion")
                implementation("com.github.nanoflakes:nanoflakes-kotlin-jvm:$nanoflakesVersion")
                implementation("com.auth0:java-jwt:3.11.0")
                implementation("org.jdbi:jdbi3-core:$jdbiVersion")
                implementation("org.jdbi:jdbi3-kotlin:$jdbiVersion")
                implementation("org.jdbi:jdbi3-postgres:$jdbiVersion")
                implementation("org.jdbi:jdbi3-json:$jdbiVersion")
                implementation("org.jdbi:jdbi3-jackson2:$jdbiVersion")
                implementation("org.postgresql:postgresql:42.2.8")
                implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinxHtmlVersion")
                implementation("com.github.nanoflakes:nanoflakes-kotlin-js:$nanoflakesVersion")
                implementation("kotlin.js.externals:kotlin-js-jquery:2.0.0-0")
                implementation(npm("moment", "2.29.1", true))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
application {
    mainClassName = "net.adriantodt.calendar.ServerKt"
}
tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack") {
    outputFileName = "app.js"
}
//tasks.getByName<Jar>("jvmJar") {
//    dependsOn(tasks.getByName("jsBrowserProductionWebpack"))
//    val jsBrowserProductionWebpack = tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack")
//    from(File(jsBrowserProductionWebpack.destinationDirectory, jsBrowserProductionWebpack.outputFileName)) {
//        into("static")
//    }
//}
tasks.getByName<ProcessResources>("jvmProcessResources") {
    dependsOn(tasks.getByName("jsBrowserProductionWebpack"))
    val jsBrowserProductionWebpack = tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack")
    from(File(jsBrowserProductionWebpack.destinationDirectory, jsBrowserProductionWebpack.outputFileName)) {
        into("static")
    }
    from(File(jsBrowserProductionWebpack.destinationDirectory, jsBrowserProductionWebpack.outputFileName + ".map")) {
        into("static")
    }
}
tasks.getByName<JavaExec>("run") {
    dependsOn(tasks.getByName<Jar>("jvmJar"))
    //classpath(tasks.getByName<Jar>("jvmJar"))
}