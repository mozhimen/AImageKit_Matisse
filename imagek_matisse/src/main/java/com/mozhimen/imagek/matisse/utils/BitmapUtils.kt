@file:JvmName("BitmapUtils")

package com.matisse.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import java.io.IOException

/**
 * 获取图片的旋转角度
 *
 * @param path 图片绝对路径
 * @return 图片的旋转角度
 */
fun getBitmapDegree(path: String?): Int {
    if (path == null) return 0
    var degree = 0
    try {
        // 从指定路径下读取图片，并获取其EXIF信息
        val exifInterface = ExifInterface(path)
        // 获取图片的旋转信息
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
            ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
            ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return degree
}

// 通过uri加载图片
fun getBitmapFromUri(context: Context, uri: Uri, opts: BitmapFactory.Options): Bitmap? {
    return try {
        // mode："r" 表示只读 "w"表示只写
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts)
        parcelFileDescriptor?.close()
        image
    } catch (e: Exception) {
        null
    }
}