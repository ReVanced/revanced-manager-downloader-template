rootProject.name = "revanced-manager-downloader-template"

pluginManagement.repositories {
    gradlePluginPortal()
    google()
}

dependencyResolutionManagement.repositories {
    mavenCentral()
    google()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/revanced/registry")
        credentials(PasswordCredentials::class)
    }
}
