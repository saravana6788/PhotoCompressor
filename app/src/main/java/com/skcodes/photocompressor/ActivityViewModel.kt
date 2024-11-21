package com.skcodes.photocompressor

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.UUID

class ActivityViewModel():ViewModel() {

    var unCompressedPhotoUri by mutableStateOf<Uri?>(null)
        private set

    var workId by mutableStateOf<UUID?>(null)
    private set

    var compressedPhotoBitmap by mutableStateOf<Bitmap?>(null)
        private set

    fun updateUncompressedPhotoUri(uri: Uri?){
        unCompressedPhotoUri = uri
    }

    fun updateWorkerId(id:UUID){
        workId = id
    }

    fun  updateCompressedBitmap(bitmap:Bitmap){
        compressedPhotoBitmap = bitmap
    }


}