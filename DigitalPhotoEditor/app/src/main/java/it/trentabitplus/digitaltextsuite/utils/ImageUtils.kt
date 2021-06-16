package it.trentabitplus.digitaltextsuite.utils

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.media.Image
import androidx.annotation.ColorInt

/**
 * Utility class for manipulating images.
 */
object ImageUtils {
    private val CHANNEL_RANGE = 0 until (1 shl 18)

    fun convertYuv420888ImageToBitmap(image: Image): Bitmap {
        require(image.format == ImageFormat.YUV_420_888) {
            "Unsupported image format $(image.format)"
        }
        val planes = image.planes

        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        val yuvBytes = planes.map { plane ->
            val buffer = plane.buffer
            val yuvBytes = ByteArray(buffer.capacity())
            buffer[yuvBytes]
            buffer.rewind()  // Be kind…
            yuvBytes
        }

        val yRowStride = planes[0].rowStride
        val uvRowStride = planes[1].rowStride
        val uvPixelStride = planes[1].pixelStride
        val width = image.width
        val height = image.height
        @ColorInt val argb8888 = IntArray(width * height)
        var i = 0
        for (y in 0 until height) {
            val pY = yRowStride * y
            val uvRowStart = uvRowStride * (y shr 1)
            for (x in 0 until width) {
                val uvOffset = (x shr 1) * uvPixelStride
                argb8888[i++] =
                    yuvToRgb(
                        yuvBytes[0][pY + x].toIntUnsigned(),
                        yuvBytes[1][uvRowStart + uvOffset].toIntUnsigned(),
                        yuvBytes[2][uvRowStart + uvOffset].toIntUnsigned()
                    )
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(argb8888, 0, width, 0, 0, width, height)
        return bitmap
    }

    /**
     * This method crops a bitmap only in the height, cutting a
     * percentage of the bitmap from its top and its bottom
     * @param bitmap : the bitmap to be cropped
     * @param cropPercentage : percentage of the top (also bottom) of the image to be cutted
     *
     * @return the new cropped bitmap
     *
     */
    fun cropOnlyHeight(bitmap: Bitmap, cropPercentage: Float) : Bitmap{

        val top = bitmap.height * cropPercentage
        val height = bitmap.height - 2*top
        return Bitmap.createBitmap(bitmap, 0, top.toInt(), bitmap.width, height.toInt())
    }


    /**
     * Rotate a bitmap of given degrees
     * @param bitmap : the bitmap to be rotate
     * @param rotationDegrees : amount of degrees of the rotation
     *
     * @return a new bitmap rotated
     */
    fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int) : Bitmap{
        val matrix = Matrix()
        matrix.preRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    @ColorInt
    private fun yuvToRgb(nY: Int, nU: Int, nV: Int): Int {
        var mynY = nY
        var mynU = nU
        var mynV = nV
        mynY -= 16
        mynU -= 128
        mynV -= 128
        mynY = mynY.coerceAtLeast(0)


        var nR = 1192 * mynY + 1634 * mynV
        var nG = 1192 * mynY - 833 * mynV - 400 * mynU
        var nB = 1192 * mynY + 2066 * mynU

        // Clamp the values before normalizing them to 8 bits.
        nR = nR.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
        nG = nG.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
        nB = nB.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
        return -0x1000000 or (nR shl 16) or (nG shl 8) or nB
    }
}

private fun Byte.toIntUnsigned(): Int {
    return toInt() and 0xFF
}