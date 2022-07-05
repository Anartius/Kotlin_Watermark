package watermark

import java.io.File
import javax.imageio.ImageIO

fun main() {
    println("Input the image filename:")
    val fileName = readln()
    val imageFile = File(fileName)

    if (imageFile.exists()) {
        printImageInfo(fileName)
    } else {
        println("The file $fileName doesn't exist.")
        return
    }
}


fun printImageInfo(fileName: String) {
    val transparency = mapOf(1 to "OPAQUE", 2 to "BITMASK", 3 to "TRANSLUCENT")
    val image = ImageIO.read(File(fileName))
    println("""
        Image file: $fileName
        Width: ${image.width}
        Height: ${image.height}
        Number of components: ${image.colorModel.numComponents}
        Number of color components: ${image.colorModel.numColorComponents}
        Bits per pixel: ${image.colorModel.pixelSize}
        Transparency: ${transparency[image.transparency]}
    """.trimIndent())
}