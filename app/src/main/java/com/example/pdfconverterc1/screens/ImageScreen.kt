package com.example.pdfconverterc1.screens

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.pdfconverterc1.R
import com.example.pdfconverterc1.model.ImageData
import com.example.pdfconverterc1.utils.Constants
import com.example.pdfconverterc1.viewmodel.PdfViewModel
import com.example.pdfconverterc1.viewmodel.events.PdfEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(navHostController: NavHostController, viewModel: PdfViewModel) {

    val context = LocalContext.current

    var imageList by remember { mutableStateOf(emptyList<ImageData>()) }
    var showPopUpMenu by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var save by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            Log.d("TAG", "ImageScreen: uri $uris ")
            val list = arrayListOf<ImageData>()
            list.addAll(imageList)
            uris.forEach { uri ->
                list.add(ImageData(uri, false))
            }
            imageList = list
        }
    )


    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = {

            val uri = it?.let { bitmap ->
                viewModel.saveBitmapToMediaStore(bitmap)
            }

            Log.d("TAG", "ImageScreen: uri $uri ")
            if (uri != null) {
                val list = arrayListOf<ImageData>()
                list.addAll(imageList)
                list.add(ImageData(uri, false))
                imageList = list
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            if (it) {
                Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),

        floatingActionButton = {
            FloatingActionButton(onClick = {
                showPopUpMenu = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_a_photo_24),
                    contentDescription = "Add image"
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Image Screen")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navHostController.navigateUp()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                    }

                },
                actions = {
                    IconButton(onClick = {
                        Log.d("TAG", "ImageScreen: IconButton onClick ")

                        if (imageList.isEmpty()) {
                            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
                        } else {
                            save = true
                        }

                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_picture_as_pdf_24),
                            contentDescription = "PDF"
                        )
                    }
                    IconButton(onClick = {
                        if (imageList.isEmpty()) {
                            Toast.makeText(context, "Nothing to delete", Toast.LENGTH_SHORT).show()
                        } else {
                            val list = arrayListOf<ImageData>()
                            imageList.forEach { image ->
                                if (!image.checked) {
                                    list.add(image)
                                }
                            }
                            if (list.isEmpty()) {
                                Toast.makeText(context, "Nothing selected", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                imageList = list
                            }

                        }


                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = {
                        imageList = emptyList()
                        Toast.makeText(context, "Page cleared...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Delete All")
                    }

                },
                scrollBehavior = scrollBehavior
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {


            if (save) {
                PdfSaveDialog(
                    title,
                    onNameChanged = {
                        title = it
                    },
                    onDismissListener = {
                        save = false
                        loading = false
                    },
                    onSaveClickedListener = {
                        loading = true
                        save = false
                        CoroutineScope(Dispatchers.IO).launch {

                            val list = arrayListOf<ImageData>()
                            imageList.forEach { image ->
                                if (image.checked) {
                                    list.add(image)
                                }
                            }
                            if (list.size != 0) {
                                imageList = list
                            }

                            viewModel.onEvent(PdfEvent.CreatePDF(title, imageList) {


                            })

                            withContext(Dispatchers.Main) {
                                loading = false
                                Toast.makeText(context, "PDF converted", Toast.LENGTH_SHORT).show()
                                navHostController.navigateUp()
                            }
                        }
                    })
            }
        }
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                CircularProgressIndicator(
                    Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
        }

        if (showPopUpMenu) {
            ShowPopUpMenu(onCameraClicked = {

                showPopUpMenu = false
                val permissionCheekResult =
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    )
                if (permissionCheekResult == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch()
                } else {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }

            }, onGalleryClicked = {

                showPopUpMenu = false
                val permissionCheekResult =
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    )
                if (permissionCheekResult == PackageManager.PERMISSION_GRANTED) {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            }, onDismissListener = {
                showPopUpMenu = false
            })
        }


        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth()
        ) {

            items(imageList) { imageData ->
                SelectedBox(
                    update,
                    modifier = Modifier,
                    imageData,
                    selected = imageData.checked
                ) {
                    Log.d("TAG", "ImageScreen: imageData.checked ${imageData.checked} ")
                    imageData.checked = !imageData.checked
                    update = !update
                    Log.d("TAG", "ImageScreen: imageData.checked ${imageData.checked} ")

                }
            }
        }
    }
}


@Composable
fun ShowPopUpMenu(
    onCameraClicked: () -> Unit,
    onGalleryClicked: () -> Unit,
    onDismissListener: () -> Unit
) {
    Box {
        AlertDialog(
            title = {
                Text(
                    text = "Choose from...",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
            },
            onDismissRequest = onDismissListener,
            dismissButton = {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = onCameraClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Yellow,
                    )
                ) {
                    Text(
                        text = "Open Camera",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = onGalleryClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Yellow,
                    )
                ) {
                    Text(
                        text = "Open Gallery",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    )
                }

            }
        )

    }

}

fun saveBitmapToMediaStore(context: Context, bitmap: Bitmap): Uri? {
    val contentResolver: ContentResolver = context.contentResolver
    val contentValues = ContentValues()
    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "my_image.jpg")
    contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

    // Use MediaStore.Images.Media.EXTERNAL_CONTENT_URI for saving to external storage
    val imageUri =
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    try {
        if (imageUri != null) {
            val outputStream: OutputStream? = contentResolver.openOutputStream(imageUri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    return imageUri
}

@Composable
fun SelectedBox(
    update: Boolean,
    modifier: Modifier,
    imageData: ImageData,
    selected: Boolean,
    onClick: () -> Unit
) {
    Log.d("TAG", "SelectedBox: box created ")

    Box(
        modifier = modifier
            .height(200.dp)
            .height(150.dp)
            .clickable {
                onClick()
            }
    ) {
        AsyncImage(
            model = imageData.imageUri,
            contentDescription = null,
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentScale = ContentScale.Fit
        )
        if (selected) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .background(color = Color(0x7700C6F7))
            )
        }


    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfSaveDialog(
    name: String,
    onNameChanged: (String) -> Unit,
    onDismissListener: () -> Unit,
    onSaveClickedListener: () -> Unit
) {
    AlertDialog(
        title = {
            Text(text = "Details")
        },
        text = {
            OutlinedTextField(value = name, onValueChange = onNameChanged, placeholder = {
                Text(text = "Pdf Title")
            })
        },
        onDismissRequest = onDismissListener,
        confirmButton = {
            Button(onClick = onSaveClickedListener, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Convert to pdf ")
            }
        }
    )
}
