package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import kotlin.math.max

object ImageUtils {

    fun uriToBase64(context: Context, uri: Uri, maxDimension: Int = 1024): String? {
        return try {
            val originalBitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }

            if (originalBitmap == null) return null

            // Handle orientation from EXIF
            val rotatedBitmap = rotateBitmapIfRequired(context, uri, originalBitmap)
            bitmapToBase64(rotatedBitmap, maxDimension)
        } catch (e: Exception) {
            Log.w("AnTamAI", "Failed to decode and convert image URI ($uri) to base64", e)
            null
        }
    }

    fun bitmapToBase64(bitmap: Bitmap, maxDimension: Int = 1024): String {
        val scaledBitmap = scaleBitmap(bitmap, maxDimension)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxSide = max(width, height)

        if (maxSide <= maxDimension) {
            return bitmap
        }

        val ratio = maxDimension.toFloat() / maxSide.toFloat()
        val targetWidth = (width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (height * ratio).toInt().coerceAtLeast(1)

        return bitmap.scale(targetWidth, targetHeight)
    }

    private fun rotateBitmapIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: return bitmap

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (_: Exception) {
            bitmap
        }
    }
}
