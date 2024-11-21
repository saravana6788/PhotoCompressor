package com.skcodes.photocompressor

import android.app.ComponentCaller
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.compose.AsyncImage
import com.skcodes.photocompressor.backgroundwork.PhotoCompressor
import com.skcodes.photocompressor.ui.theme.PhotoCompressorTheme


class MainActivity : ComponentActivity() {
    private lateinit var workManager:WorkManager
    val viewModel by viewModels<ActivityViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workManager = WorkManager.getInstance(applicationContext)
        setContent {
            PhotoCompressorTheme {
                val workResult = viewModel.workId?.let{
                    workManager.getWorkInfoByIdLiveData(it).observeAsState().value
                }
                LaunchedEffect(key1 = workResult?.outputData) {
                    val filePath = workResult?.outputData.let{
                            it?.getString(PhotoCompressor.COMPRESSED_FILE_OUTPUT_PATH)
                    }

                    filePath?.let {
                        val compressedBitmap = BitmapFactory.decodeFile(it)
                        viewModel.updateCompressedBitmap(compressedBitmap)
                    }
                }
                    Column(modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        viewModel.unCompressedPhotoUri?.let{
                            Text(text = "Uncompressed Image:",
                                fontSize = 16.sp)

                            Spacer(modifier = Modifier.height(2.dp))

                            AsyncImage(model = it , contentDescription ="" )

                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        viewModel.compressedPhotoBitmap?.let{
                            Text(text = "Compressed Image:",
                                fontSize = 16.sp )

                            Spacer(modifier = Modifier.height(2.dp))

                            Image(bitmap = it.asImageBitmap() , contentDescription ="" )

                        }

                    }
                }
            }
        }



     override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("Main Activity","intent received")
       val uri = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        }else {
           intent.getParcelableExtra(Intent.EXTRA_STREAM)
       }
                ?:return
        Log.d("Main Activity","uri received $uri")
        viewModel.updateUncompressedPhotoUri(uri)
        val workRequest = OneTimeWorkRequestBuilder<PhotoCompressor>()
            .setInputData(
                workDataOf(
                    PhotoCompressor.KEY_IMAGE_URI to uri.toString(),
                    PhotoCompressor.IMAGE_COMPRESSION_THRESHOLD_LIMIT to 1024*20L
                )
            )
            /*.setConstraints(
                Constraints(
                    requiresStorageNotLow = true
                )
            )*/
            .build()
        viewModel.updateWorkerId(workRequest.id)
        workManager.enqueue(workRequest)

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PhotoCompressorTheme {
        Greeting("Android")
    }
}



