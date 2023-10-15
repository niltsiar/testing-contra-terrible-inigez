import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    application
    id(libs.plugins.kotlin.jvm.pluginId)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ben.manes.versions)
}

application {
    mainClass by "dev.niltsiar.terribleiniguez.TemplateKt"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "${JavaVersion.VERSION_17}"
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }

    // disallow release candidates as upgradable versions from stable versions
    withType<DependencyUpdatesTask>().configureEach {
        rejectVersionIf {
            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }
    }
}

// https://github.com/ben-manes/gradle-versions-plugin
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

dependencies {
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.datetime)
    implementation(libs.bundles.arrow)
    implementation(libs.bundles.ktor.client)
}
