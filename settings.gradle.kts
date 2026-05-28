pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    // Se você tiver acesso homologado ao SDK real, descomente o bloco abaixo:
    // maven {
    //   url = uri("https://raw.githubusercontent.com/pagseguromaster/plugpag/master-3.x/android")
    // }
  }
}

rootProject.name = "abastecIA"

include(":app")
