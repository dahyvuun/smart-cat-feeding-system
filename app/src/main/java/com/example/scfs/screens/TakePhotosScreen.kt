package com.example.scfs.screens

import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scfs.R
import com.example.scfs.data.CatTrainingPhotoInsert
import com.example.scfs.data.SupabaseManager
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun TakePhotosScreen(
    catId: String,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedIndex by remember { mutableStateOf(1) }
    var showPickerDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val photos = remember {
        mutableStateListOf<Bitmap?>(null, null, null, null)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            photos[selectedIndex - 1] = bitmap

            scope.launch {
                uploadTrainingPhoto(bitmap, catId, selectedIndex) {
                    message = it
                }
            }
        } else {
            message = "No camera photo taken"
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                uri
            )

            photos[selectedIndex - 1] = bitmap

            scope.launch {
                uploadTrainingPhoto(bitmap, catId, selectedIndex) {
                    message = it
                }
            }
        } else {
            message = "No gallery image selected"
        }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_paw),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(55.dp))

            Text(
                text = "Take 4 Photos",
                fontFamily = Hanuman,
                fontSize = 34.sp,
                color = Color(0xFF3F3F3F)
            )

            Spacer(Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoBoxWithCamera(1, photos[0]) {
                    selectedIndex = 1
                    showPickerDialog = true
                }

                PhotoBoxWithCamera(2, photos[1]) {
                    selectedIndex = 2
                    showPickerDialog = true
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoBoxWithCamera(3, photos[2]) {
                    selectedIndex = 3
                    showPickerDialog = true
                }

                PhotoBoxWithCamera(4, photos[3]) {
                    selectedIndex = 4
                    showPickerDialog = true
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "The better the photos, the\neasier it is to identify the cats!",
                fontFamily = Harmattan,
                fontSize = 22.sp
            )

            if (message.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    color = Color(0xFF8A3A3A),
                    fontFamily = Harmattan,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.weight(1f))

            SCFSButton(
                text = "Finish Setup",
                onClick = onFinish
            )

            Spacer(Modifier.height(40.dp))
        }

        if (showPickerDialog) {
            AlertDialog(
                onDismissRequest = {
                    showPickerDialog = false
                },
                title = {
                    Text("Choose photo source")
                },
                text = {
                    Text("Take a new photo or choose one from gallery.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPickerDialog = false
                            cameraLauncher.launch(null)
                        }
                    ) {
                        Text("Camera")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPickerDialog = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text("Gallery")
                    }
                }
            )
        }
    }
}

@Composable
fun PhotoBoxWithCamera(
    index: Int,
    bitmap: Bitmap?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 125.dp, height = 135.dp)
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(
                    when (index) {
                        1 -> R.drawable.cat_placeholder_1
                        2 -> R.drawable.cat_placeholder_2
                        3 -> R.drawable.cat_placeholder_3
                        else -> R.drawable.cat_placeholder_4
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }

        Image(
            painter = painterResource(R.drawable.camera_icon),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(36.dp)
                .clickable {
                    onClick()
                }
        )
    }
}

suspend fun uploadTrainingPhoto(
    bitmap: Bitmap,
    catId: String,
    photoIndex: Int,
    onMessage: (String) -> Unit
) {
    try {
        val bytes = bitmap.toJpegBytes()

        val path = "machines/001/cats/$catId/training/$photoIndex.jpg"

        SupabaseManager.client.storage
            .from("cat-images")
            .upload(
                path = path,
                data = bytes
            ) {
                upsert = true
            }

        SupabaseManager.client
            .from("cat_training_photos")
            .upsert(
                CatTrainingPhotoInsert(
                    machine_id = "001",
                    cat_id = catId,
                    image_path = path,
                    photo_index = photoIndex
                )
            )

        onMessage("Photo $photoIndex saved")

    } catch (e: Exception) {
        onMessage("Upload error: ${e.message}")
    }
}

fun Bitmap.toJpegBytes(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 90, stream)
    return stream.toByteArray()
}