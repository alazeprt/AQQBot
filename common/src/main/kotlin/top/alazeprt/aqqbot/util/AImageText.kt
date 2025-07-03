package top.alazeprt.aqqbot.util

class AImageText(override val data: String, x: Double, y: Double, val size: Int, val font: String, val color: String, val bold: Boolean, val italic: Boolean) : AImageElement(AImageElementType.TEXT, data, x, y) {
}