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
    val image = ImageIO.read(File(imageFileName))
    var watermark = ImageIO.read(File(watermarkFileName))
    if (watermark.width > image.width|| watermark.height > image.height) {
        println("The watermark's dimensions are larger.")
        exitProcess(0)
    }

    var useAlpha = false
    if (ImageIO.read(File(watermarkFileName)).transparency == 3) {
        println("Do you want to use the watermark's Alpha channel?")
        if (readln().lowercase() == "yes") useAlpha = true
    } else {
        println("Do you want to set a transparency color?")
        if (readln().lowercase() == "yes") {
            watermark = addTransparency(watermark)
            useAlpha = true
        }
    }

    val weight = getWeight()
    val positions = getWatermarkPositions(image, watermark)

    println("Input the output image filename (jpg or png extension):")
    val outputFileName = readln()
    val extRegExp = """^[^<>:;,?"*|/]+.(jpg|png)$""".toRegex()
    if (!(extRegExp).matches(outputFileName)) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exitProcess(0)
    }

    var outImage = blendImages(image, watermark , weight, useAlpha, positions[0])
    if (positions.size > 1) {
        for (i in 1 until positions.size) {
            outImage = blendImages(outImage, watermark, weight, useAlpha, positions[i])
        }
    }
    saveImage(outImage, outputFileName)
}


fun checkImageFile(fileName: String, type: String) {
    val file = File(fileName)
    if (!file.exists()) {
        println("The file $fileName doesn't exist.")
        exitProcess(0)
    }

    val image = ImageIO.read(file)
    if (image.colorModel.numColorComponents != 3) {
        println("The Number of $type color components isn't 3.")
        exitProcess(0)
    } else if (!(image.colorModel.pixelSize == 24 ||
                image.colorModel.pixelSize == 32)) {
        println("The $type isn't 24 or 32-bit.")
        exitProcess(0)
    }
}


fun addTransparency(watermark: BufferedImage) : BufferedImage {
    val image = BufferedImage(
        watermark.width,
        watermark.height,
        BufferedImage.TYPE_INT_ARGB
    )

    println("Input a transparency color ([Red] [Green] [Blue]):")
    val color = try {
        val input = readln().split(" ").map { it.toInt() }.toList()
        if (input.size != 3) throw NumberFormatException()
        input.forEach { if (it !in 0..255) throw NumberFormatException() }
        input
    } catch (e: NumberFormatException) {
        println("The transparency color input is invalid.")
        exitProcess(0)
    }

    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val wRGB = Color(watermark.getRGB(x, y))

            if (
                wRGB.red == color[0] &&
                wRGB.green == color[1] &&
                wRGB.blue == color[2]
            ) {
                image.setRGB(x, y, 0)
            } else {
                image.setRGB(x, y, wRGB.rgb)
            }
        }
    }

    return image
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


fun getWatermarkPositions(image: BufferedImage, watermark: BufferedImage) :
        MutableList<List<Int>> {

    println("Choose the position method (single, grid):")
    return when (readln()) {
        "single" -> {
            val xMax = image.width - watermark.width
            val yMax = image.height - watermark.height
            println("Input the watermark position ([x 0-$xMax] [y 0-$yMax]):")

            return try {
                val position = readln().split(" ").map { it.toInt() }
                    .toList()
                if (position.size != 2) throw NumberFormatException()
                if (position[0] !in 0..xMax || position[1] !in 0..yMax) {
                    println("The position input is out of range.")
                    exitProcess(0)
                }
                mutableListOf(position)
            } catch (e: NumberFormatException) {
                println("The position input is invalid.")
                exitProcess(0)
            }
        }

        "grid" -> {
            getPositions(image, watermark)
        }

        else -> {
            println("The position method input is invalid.")
            exitProcess(0)
        }
    }
}


fun getPositions(image: BufferedImage, watermark: BufferedImage) :
        MutableList<List<Int>> {

    val result = mutableListOf<List<Int>>()
    val xSteps = image.width / watermark.width
    val ySteps = image.height / watermark.height

    for (i in 0..xSteps) {
        for (j in 0..ySteps) {
            result.add(listOf(i * watermark.width, j * watermark.height))
        }
    }
    return result
}


fun blendImages(
    image: BufferedImage,
    watermark: BufferedImage,
    weight: Int,
    useAlpha: Boolean,
    position: List<Int>
): BufferedImage {

    val xMax = if (position[0] + watermark.width < image.width) {
        position[0] + watermark.width
    } else image.width - 1

    val yMax = if (position[1] + watermark.height < image.height) {
        position[1] + watermark.height
    } else image.height - 1

    for (x in position[0] until xMax) {
        for (y in position[1] until yMax) {
            val iRGB = Color(image.getRGB(x, y), useAlpha)
            val wRGB = Color(
                watermark.getRGB(x - position[0], y - position[1]),
                useAlpha
            )

            val color = if (wRGB.alpha == 0) {
                Color(iRGB.red, iRGB.green, iRGB.blue)
            } else {
                Color(
                    (weight * wRGB.red + (100 - weight) * iRGB.red) / 100,
                    (weight * wRGB.green + (100 - weight) * iRGB.green) / 100,
                    (weight * wRGB.blue + (100 - weight) * iRGB.blue) / 100
                )
            }
            image.setRGB(x, y, color.rgb)
        }
    }

    return image
}


fun saveImage(image: BufferedImage, fileName: String) {
    val file = File(fileName)
    val extension = fileName.substring(fileName.length - 3).uppercase()
    ImageIO.write(image, extension,file)
    println("The watermarked image $fileName has been created.")
}