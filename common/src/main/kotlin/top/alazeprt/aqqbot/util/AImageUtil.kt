package top.alazeprt.aqqbot.util

import top.alazeprt.aqqbot.AQQBot
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO

object AImageUtil {
    fun addTextToImage(
        base64Image: String,
        text: String,
        x: Double,
        y: Double,
        fontSize: Int = 12,
        fontName: String = "Microsoft YaHei",
        color: String,
        bold: Boolean = false,
        italic: Boolean = false,
        plugin: AQQBot
    ): String {
        val imageBytes = try {
            Base64.getDecoder().decode(cleanBase64String(base64Image))
        } catch (e: Exception) {
            plugin.debugModule?.debugLogger?.log("Failed to decode base64 image data: $base64Image")
            throw IllegalArgumentException("Invalid base64 image data: ${e.message}")
        }

        val inputStream = ByteArrayInputStream(imageBytes)
        val originalImage: BufferedImage = ImageIO.read(inputStream)

        val newImage = BufferedImage(
            originalImage.width,
            originalImage.height,
            BufferedImage.TYPE_INT_ARGB
        )

        val graphics: Graphics2D = newImage.createGraphics().apply {
            drawImage(originalImage, 0, 0, null)

            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)

            val textColor = parseColor(color, plugin)

            var fontStyle = Font.PLAIN
            if (bold) fontStyle = fontStyle or Font.BOLD
            if (italic) fontStyle = fontStyle or Font.ITALIC

            font = getFont(fontName, fontSize, fontStyle, plugin)
            this.color = textColor
        }

        graphics.drawString(text, x.toFloat(), y.toFloat())
        graphics.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(newImage, "PNG", outputStream)

        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    private fun cleanBase64String(base64Str: String): String {
        return if (base64Str.contains("base64,")) {
            base64Str.substringAfter("base64,")
        } else {
            base64Str
        }
    }

    private fun getFont(fontName: String, fontSize: Int, fontStyle: Int, plugin: AQQBot): Font {
        return try {
            Font(fontName, fontStyle, fontSize)
        } catch (e: Exception) {
            e.printStackTrace()
            plugin.debugModule?.debugLogger?.log("Failed to load font: $fontName, size: $fontSize, style: $fontStyle: $e")
            Font(Font.SANS_SERIF, Font.PLAIN, fontSize)
        }
    }

    fun getImageBase64(imageFile: File): String {
        println(imageFile.absolutePath)
        val image = ImageIO.read(imageFile)
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "PNG", outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.getEncoder().encodeToString(imageBytes)
    }

    private fun parseColor(colorStr: String, plugin: AQQBot): Color {
        return when {
            colorStr.startsWith("#") -> {
                val hex = colorStr.substring(1)
                when (hex.length) {
                    6 -> Color(
                        hex.substring(0, 2).toInt(16),
                        hex.substring(2, 4).toInt(16),
                        hex.substring(4, 6).toInt(16)
                    )
                    8 -> Color(
                        hex.substring(0, 2).toInt(16),
                        hex.substring(2, 4).toInt(16),
                        hex.substring(4, 6).toInt(16),
                        hex.substring(6, 8).toInt(16)
                    )
                    else -> throw IllegalArgumentException("Invalid hex color: $colorStr")
                }
            }
            else -> try {
                val field = Color::class.java.getField(colorStr.toLowerCase())
                field.get(null) as Color
            } catch (e: Exception) {
                plugin.debugModule?.debugLogger?.log("Failed to parse color: $colorStr: $e")
                Color.WHITE
            }
        }
    }
}