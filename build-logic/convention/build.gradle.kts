import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.uchan.rrule.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("jvmLibrary") {
            id = libs.plugins.rrule.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("root") {
            id = libs.plugins.rrule.root.get().pluginId
            implementationClass = "RootPlugin"
        }
    }
}
