package top.alazeprt.aqqbot.drivers

import com.alessiodp.libby.Library
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.ScreenshotType
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.ACompressUtil
import top.alazeprt.aqqbot.util.LogLevel
import java.io.File
import java.nio.file.Files
import java.util.*


class Web2ImageDriver(val plugin: AQQBot) {

    private val driverFolder = plugin.getDataFolder().resolve("lib").resolve("drivers")
    private val libFolder = plugin.getDataFolder().resolve("lib")

    fun loadDependencies() {
        val library = Library.builder()
            .groupId("com{}microsoft{}playwright")
            .artifactId("playwright")
            .version("1.53.0")
            .resolveTransitiveDependencies(true)
            .build()
        plugin.libraryManager.loadLibraries(library)
    }

    fun downloadDrivers() {
        if (!driverFolder.exists() || !driverFolder.isDirectory || driverFolder.list().isEmpty()) {
            plugin.log(LogLevel.INFO, "Decompressing drivers ...")
            val driverFile = libFolder.resolve("com").resolve("microsoft").resolve("playwright")
                .resolve("driver-bundle").resolve("1.53.0").resolve("driver-bundle-1.53.0.jar")
            val tmpFolder = Files.createTempDirectory("aqqbot-driver")
            ACompressUtil.unzipJar(driverFile, tmpFolder.toFile())
            val decompressedDriverFolder = tmpFolder.resolve("driver").resolve(platformDir())
            decompressedDriverFolder.toFile().copyRecursively(driverFolder)
            plugin.log(LogLevel.INFO, "Decompressed drivers to: ${driverFolder.absolutePath}")
        }
        plugin.log(LogLevel.INFO, "Testing the driver ...")
        updateProperty()
        try {
            Playwright.create().chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
        } catch (e: Exception) {
            e.printStackTrace()
            plugin.log(LogLevel.ERROR, "Failed to launch the driver: ${e.message}")
        }
    }

    fun updateProperty() {
        System.setProperty("playwright.cli.dir", driverFolder.absolutePath)
    }

    private fun platformDir(): String {
        val name = System.getProperty("os.name").lowercase(Locale.getDefault())
        val arch = System.getProperty("os.arch").lowercase(Locale.getDefault())

        if (name.contains("windows")) {
            return "win32_x64"
        }
        if (name.contains("linux")) {
            if (arch == "aarch64") {
                return "linux-arm64"
            } else {
                return "linux"
            }
        }
        if (name.contains("mac os x")) {
            if (arch == "aarch64") {
                return "mac-arm64"
            } else {
                return "mac"
            }
        }
        throw RuntimeException("Unexpected os.name value: $name")
    }

    fun convertToImage(file: File, width: Int, height: Int, timeout: Long = 2000): ByteArray? {
        updateProperty()
        try {
            val playwright = Playwright.create().chromium().launch(BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(listOf("--no-sandbox", "--disable-dev-shm-usage")))
            val page = playwright.newPage(Browser.NewPageOptions().setViewportSize(width, height))
            page.navigate("file://${file.resolve("index.html").absolutePath}")
            page.waitForLoadState(LoadState.NETWORKIDLE)
            val options = Page.ScreenshotOptions()
                .setFullPage(true)
                .setOmitBackground(true)
                .setType(ScreenshotType.PNG)
            val bytes = page.screenshot(options)
            playwright.close()
            return bytes
        } catch (e: Exception) {
            plugin.log(LogLevel.ERROR, "Failed to convert web page to image: ${e.message}")
        }
        return null
    }
}