rootProject.name = "terrible-iniguez"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }

    repositories {
        mavenCentral()
    }
}
