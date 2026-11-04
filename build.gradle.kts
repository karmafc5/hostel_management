plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    id("io.ktor.plugin") version "3.0.3" apply false
}

val isWindowsOneDriveWorkspace =
    System.getProperty("os.name").startsWith("Windows", ignoreCase = true) &&
        rootDir.absolutePath.split(java.io.File.separatorChar).any { it.equals("OneDrive", ignoreCase = true) }

if (isWindowsOneDriveWorkspace) {
    val localBuildRoot = java.io.File(
        System.getenv("LOCALAPPDATA") ?: System.getProperty("java.io.tmpdir"),
        "HostelMaintenance\\gradle-build"
    )
    allprojects {
        val moduleBuildPath = project.path.replace(":", "_").ifBlank { "root" }
        layout.buildDirectory.set(java.io.File(localBuildRoot, moduleBuildPath))
    }
}
