package com.skcodes.photocompressor.backgroundwork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

class PhotoCompressor(private val appContext:Context, private val params:WorkerParameters):CoroutineWorker(appContext,params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO){
            Log.e("PhotComressWork","Background Work started")
            val stringUri = params.inputData.getString(KEY_IMAGE_URI)
            val compressionThresHoldLimits = params.inputData.getLong(
                IMAGE_COMPRESSION_THRESHOLD_LIMIT, 0L)
            val imageURI = Uri.parse(stringUri)
            val imageAsBytes = appContext.contentResolver.openInputStream(imageURI)?.use{
                it.readBytes()
            }?:return@withContext Result.failure()

            val imageAsBitmap = BitmapFactory.decodeByteArray(imageAsBytes,0,imageAsBytes.size)
            var quality = 100
            var outputBytes:ByteArray
            do{
                val outputStream = ByteArrayOutputStream()
                outputStream.use {
                    imageAsBitmap.compress(Bitmap.CompressFormat.JPEG,quality,outputStream)
                    outputBytes = outputStream.toByteArray()
                }
                quality-=(quality*0.1).roundToInt()

            }while(quality > 5 && outputStream.size()>compressionThresHoldLimits)

            val file = File(appContext.cacheDir,"${params.id}.jpeg")
            file.writeBytes(outputBytes)

            Result.success(workDataOf(
                COMPRESSED_FILE_OUTPUT_PATH to file.absolutePath
            ))

        }
    }


    companion object {
        const val KEY_IMAGE_URI = "KEY_IMAGE_URI"
        const val IMAGE_COMPRESSION_THRESHOLD_LIMIT = "IMAGE_COMPRESSION_THRESHOLD_LIMIT"
        const val COMPRESSED_FILE_OUTPUT_PATH = "COMPRESSED_FILE_OUTPUT_PATH"
    }
}