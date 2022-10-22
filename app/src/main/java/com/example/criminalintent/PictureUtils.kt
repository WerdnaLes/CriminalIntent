package com.example.criminalintent

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlin.math.roundToInt

// Rescale taken image:
fun getScaledBitmap(
    path: String,
    destWidth: Int,
    destHeight: Int
): Bitmap {
    // Read in the dimensions of the image on disk:
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    // Figure out how much to scale down by
    val sampleSize = if (srcHeight <= destHeight
        && srcWidth <= destWidth
    ) {
        1
    } else {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        minOf(heightScale, widthScale).roundToInt()
    }

    // Read in and create final bitmap:
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    })
}

// Correct the orientation of the taken image:
fun correctImageOrientation(fileName: String, bitImage: Bitmap): Bitmap {
    // Setup to rotate selected image:
    val ei = ExifInterface(fileName)
    val orientation = ei.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )

    var rotatedBitImage = bitImage
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 ->
            rotatedBitImage = rotateImage(bitImage, 90.0f)
        ExifInterface.ORIENTATION_ROTATE_180 ->
            rotatedBitImage = rotateImage(bitImage, 180.0f)
        ExifInterface.ORIENTATION_ROTATE_270 ->
            rotatedBitImage = rotateImage(bitImage, 270.0f)
    }

    return rotatedBitImage
}

// Rotate Image if needed:
fun rotateImage(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(
        source, 0, 0, source.width, source.height,
        matrix, true
    )
}