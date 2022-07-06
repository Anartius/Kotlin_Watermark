package watermark

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

fun main() {
    println("Input the image filename:")
    val imageFileName = readln()
    checkImageFile(imageFileName, "image")

    println("Input the watermark image filename:")
    val watermarkFileName = readln()
    checkImageFile(watermarkFileName, "watermark")

    compareImages(imageFileName, watermarkFileName)
    val weight = getWeight()

    println("Input the output image filename (jpg or png extension):")
    val outputFileName = readln()
    val extRegExp = """^[^<>:;,?"*|/]+.(jpg|png)$""".toRegex()
    if (!(extRegExp).matches(outputFileName)) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exitProcess(0)
    }

    val outputImage = blendImages(imageFileName, watermarkFileName, weight)
    saveImage(outputImage, outputFileName)
}


fun checkImageFile(fileName: String, type: String) {
    val file = File(fileName)
    if (!file.exists()) {
        println("The file $fileName doesn't exist.")
        exitProcess(0)
    }

    val image = ImageIO.read(file)
    if (image.colorModel.numComponents != 3) {
        println("The Number of $type color components isn't 3.")
        exitProcess(0)
    } else if (!(image.colorModel.pixelSize == 24 ||
                image.colorModel.pixelSize == 32)) {
        println("The $type isn't 24 or 32-bit.")
        exitProcess(0)
    }
}


fun compareImages(imageFileName: String, watermarkFileName: String) {
    val image = ImageIO.read(File(imageFileName))
    val watermark = ImageIO.read(File(watermarkFileName))

    if (!(image.width == watermark.width &&
                image.height == watermark.height &&
                image.colorModel.pixelSize == watermark.colorModel.pixelSize &&
                image.colorModel.numComponents ==
                watermark.colorModel.numComponents &&
                image.colorModel.numColorComponents ==
                watermark.colorModel.numColorComponents)) {

        println("The image and watermark dimensions are different.")
        exitProcess(0)
    }
}


fun getWeight() : Int {
    println("Input the watermark transparency percentage (Integer 0-100):")
    val weight = try {
        readln().toInt()
    } catch (e:NumberFormatException) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(0)
    }
    if (weight !in 0..100) {
        println("The transparency percentage is out of range.")
        exitProcess(0)
    }
    return weight
}


fun blendImages(imageFileName: String,
                watermarkFileName: String,
                weight: Int) : BufferedImage {

    val image = ImageIO.read(File(imageFileName))
    val watermark = ImageIO.read(File(watermarkFileName))

    val outputImage = BufferedImage(image.width, image.height, image.type)

    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val iRGB = Color(image.getRGB(x, y))
            val wRGB = Color(watermark.getRGB(x, y))

            val color = Color(
                (weight * wRGB.red + (100 - weight) * iRGB.red) / 100,
                (weight * wRGB.green + (100 - weight) * iRGB.green) / 100,
                (weight * wRGB.blue + (100 - weight) * iRGB.blue) / 100
            )
            outputImage.setRGB(x, y, color.rgb)
        }
    }

    return outputImage
}


fun saveImage(image: BufferedImage, fileName: String) {
    val file = File(fileName)
    val extension = fileName.substring(fileName.length - 3).uppercase()
    ImageIO.write(image, extension,file)
    println("The watermarked image $fileName has been created.")
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