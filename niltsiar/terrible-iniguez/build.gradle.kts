import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    id(libs.plugins.kotlin.jvm.pluginId)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ben.manes.versions)
}

application {
    mainClass by "dev.niltsiar.terribleiniguez.MainKt"
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

    withType<Test>().configureEach {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
        testLogging {
            lifecycle {
                events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
                exceptionFormat = TestExceptionFormat.FULL

                showExceptions = true
                showCauses = true
                showStackTraces = false
                showStandardStreams = false
            }
            info.events = lifecycle.events
            info.exceptionFormat = lifecycle.exceptionFormat
        }

        val failedTests = mutableListOf<TestDescriptor>()
        val skippedTests = mutableListOf<TestDescriptor>()

        addTestListener(object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {}

            override fun beforeTest(testDescriptor: TestDescriptor) {}

            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
                when (result.resultType) {
                    TestResult.ResultType.FAILURE -> failedTests.add(testDescriptor)
                    TestResult.ResultType.SKIPPED -> skippedTests.add(testDescriptor)
                    else -> Unit
                }
            }

            override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                if (suite.parent == null) {
                    logger.lifecycle("################ Summary::Start ################")
                    logger.lifecycle("Test result: ${result.resultType}")
                    logger.lifecycle(
                        "Test summary: ${result.testCount} tests, " +
                                "${result.successfulTestCount} succeeded, " +
                                "${result.failedTestCount} failed, " +
                                "${result.skippedTestCount} skipped"
                    )
                    failedTests.takeIf { it.isNotEmpty() }?.prefixedSummary("\tFailed Tests")
                    skippedTests.takeIf { it.isNotEmpty() }?.prefixedSummary("\tSkipped Tests:")
                    logger.lifecycle("################ Summary::End ##################")
                }
            }

            private infix fun List<TestDescriptor>.prefixedSummary(subject: String) {
                logger.lifecycle(subject)
                forEach { test -> logger.lifecycle("\t\t${test.displayName()}") }
            }

            private fun TestDescriptor.displayName() = parent?.let { "${it.name} - $name" } ?: "$name"

        })
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
    implementation(libs.logback)

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk)
}
