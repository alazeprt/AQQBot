package top.alazeprt.aqqbot.drivers

import com.alessiodp.libby.Library
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.impl.driver.Driver
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.ScreenshotType
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.ACompressUtil
import top.alazeprt.aqqbot.util.LogLevel
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit


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
            ACompressUtil.unzip(driverFile, tmpFolder.toFile())
            val decompressedDriverFolder = tmpFolder.resolve("driver").resolve(platformDir())
            decompressedDriverFolder.toFile().copyRecursively(driverFolder)
            plugin.log(LogLevel.INFO, "Decompressed drivers to: ${driverFolder.absolutePath}")
        }
        if (driverFolder.resolve("node").isFile && !driverFolder.resolve("node").canExecute()) {
            driverFolder.resolve("node").setExecutable(true)
        }
        if (driverFolder.resolve("node.exe").isFile && !driverFolder.resolve("node.exe").canExecute()) {
            driverFolder.resolve("node.exe").setExecutable(true)
        }
        plugin.log(LogLevel.INFO, "Downloading the webkit ...")
        try {
            val pb: ProcessBuilder = createProcessBuilder()
            pb.command().add("install")
            pb.redirectError(ProcessBuilder.Redirect.INHERIT)
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
            val p = pb.start()
            val result = p.waitFor(10, TimeUnit.MINUTES)
            if (!result) {
                p.destroy()
                throw java.lang.RuntimeException("Timed out waiting for browsers to install")
            }
            if (p.exitValue() != 0) {
                throw java.lang.RuntimeException("Failed to install browsers, exit code: " + p.exitValue())
            }
        } catch (e: Exception) {
            plugin.log(LogLevel.ERROR, "Failed to install browsers: ${e.message}")
            return
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

    fun createProcessBuilder(): ProcessBuilder {
        val node = if (System.getProperty("os.name").lowercase(Locale.getDefault())
                .contains("windows")
        ) "node.exe" else "node"
        val nodePath = driverFolder.resolve(node).absolutePath
        val pb = ProcessBuilder(nodePath)
        pb.command().add(driverFolder.resolve("package").resolve("cli.js").absolutePath)
        pb.environment().put("PW_LANG_NAME", "java")
        pb.environment().put("PW_LANG_NAME_VERSION", getMajorJavaVersion())
        val version = Driver::class.java.getPackage().implementationVersion
        if (version != null) {
            pb.environment().put("PW_CLI_DISPLAY_VERSION", version)
        }
        return pb
    }

    private fun getMajorJavaVersion(): String {
        val version = System.getProperty("java.version")
        if (version.startsWith("1.")) {
            return version.substring(2, 3)
        }
        val dot = version.indexOf(".")
        if (dot != -1) {
            return version.substring(0, dot)
        }
        return version
    }
}