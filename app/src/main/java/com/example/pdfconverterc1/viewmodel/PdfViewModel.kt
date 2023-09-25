package com.example.pdfconverterc1.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pdfconverterc1.R
import com.example.pdfconverterc1.model.PdfData
import com.example.pdfconverterc1.utils.Constants
import com.example.pdfconverterc1.viewmodel.events.PdfEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class PdfViewModel @Inject constructor(val application: Application) : ViewModel() {


    private val _pdfData = mutableStateOf<PdfData?>(null)

    fun setPdf(pdf: PdfData) {
        _pdfData.value = pdf
    }

    fun getPdf(): PdfData? {
        return _pdfData.value
    }


    private val _getPdfEventFlow = mutableStateOf<List<PdfData>>(emptyList<PdfData>())
    val getPdfEventFlow: State<List<PdfData>> = _getPdfEventFlow


    @SuppressLint("ResourceAsColor")
    fun onEvent(event: PdfEvent) {
        when (event) {
            PdfEvent.GetPDF -> {

                val folder = File(application.getExternalFilesDir(null), Constants.PDF_FOLDER)
                if (folder.exists()) {
                    val files = folder.listFiles()
                    val list = arrayListOf<PdfData>()
                    for (fileEntry in files!!) {
                        val uri = Uri.fromFile(fileEntry)
                        val pdfData = PdfData(fileEntry, uri)

                        list.add(pdfData)
                    }
                    _getPdfEventFlow.value = list
                } else {
                    Log.d("TAG", "loadPdfDocument: no Files in folder")
                    Toast.makeText(application, "No Pdf File", Toast.LENGTH_SHORT).show()
                }

            }

            is PdfEvent.CreatePDF -> {

                try {

                    val root = File(application.getExternalFilesDir(null), Constants.PDF_FOLDER)
                    root.mkdirs()
                    Log.d("TAG", "generatePdfFromImages:  try")

                    val timestamp = System.currentTimeMillis()
                    val fileName = if (event.title.isNotEmpty()) {
                        "${event.title}.pdf"
                    } else {
                        "PDF_$timestamp.pdf"
                    }


                    val file = File(root, fileName)


                    Log.d("TAG", "generatePdfFromImages: file $file")
                    val fileOutputStream = FileOutputStream(file)
                    val pdfDocument = PdfDocument()


                    for ((index, image) in event.imageList.withIndex()) {
                        try {
                            var bitmap: Bitmap

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                bitmap = ImageDecoder.decodeBitmap(
                                    ImageDecoder.createSource(
                                        application.contentResolver,
                                        image.imageUri
                                    )
                                )
                            } else {
                                bitmap = MediaStore.Images.Media.getBitmap(
                                    application.contentResolver,
                                    image.imageUri
                                )
                            }
                            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)


                            // Get the screen width
                            val displayMetrics = DisplayMetrics()
                            (application.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(
                                displayMetrics
                            )
                            val screenWidth = displayMetrics.widthPixels


                            // Calculate the new height to maintain aspect ratio based on the screen width
                            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val newWidth = screenWidth
                            val newHeight = (newWidth / aspectRatio).toInt()

                            // Resize the bitmap to have the screen width
                            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

                            val pageInfo =
                                PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1)
                                    .create()

                            val page = pdfDocument.startPage(pageInfo)
                            val paint = Paint()
                            paint.color = R.color.white

                            val canvas = page.canvas
                            canvas.drawPaint(paint)
                            canvas.drawBitmap(bitmap, 0f, 0f, null)

                            pdfDocument.finishPage(page)
                            bitmap.recycle()


                        } catch (e: Exception) {
                            Log.d("TAG", "generatePdfFromImages: e ${e.localizedMessage}")
                            e.printStackTrace()
//                            event.pdfConverted(true)
                        }
                    }

                    pdfDocument.writeTo(fileOutputStream)
                    pdfDocument.close().let {
                        event.pdfConverted()
//                        event.pdfConverted(true)

                    }


                } catch (e: Exception) {
                    Log.d("TAG", "generatePdfFromImages: e ${e.localizedMessage}")
                    e.printStackTrace()
//                    event.pdfConverted(false)
                }


            }

            is PdfEvent.OpenPDF -> {

            }

            is PdfEvent.DeletePDF -> {
                val fileToDelete = event.pdf.file
                event.isDeleted(fileToDelete.delete())
            }
        }

    }


    fun loadFileSize(PDF: PdfData): String {
        val bytes: Double = PDF.file.length().toDouble()
        val kb = bytes / 1024;
        val mb = kb / 1024;
        var size = ""
        if (mb >= 1) {
            size = String.format("%.2f", mb) + " MB"
        } else if (kb >= 1) {
            size = String.format("%.2f", kb) + " KB"
        } else {
            size = String.format("%.2f", bytes) + " bytes"
        }
        return size
    }

     fun loadThumbnailFromPDF(PDF: PdfData, page: (Int) -> Unit): Bitmap? {
        Log.d("TAG", "loadThumbnailFromPDF: ")
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        var thumbnailBitmap: Bitmap? = null
        var pageCount = 0;
        executorService.execute {
            try {
                val parcelFileDescriptor =
                    ParcelFileDescriptor.open(PDF.file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfReader = PdfRenderer(parcelFileDescriptor)
                pageCount = pdfReader.pageCount
                if (pageCount <= 0) {

                    Log.d("TAG", "loadThumbnailFromPDF: No page")

                } else {

                    val currentPage = pdfReader.openPage(0)
                    thumbnailBitmap = Bitmap.createBitmap(
                        currentPage.width,
                        currentPage.height,
                        Bitmap.Config.ARGB_8888
                    )
                    currentPage.render(
                        thumbnailBitmap!!,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                }


            } catch (e: Exception) {
                Log.d("TAG", "loadThumbnailFromPDF: e ${e.localizedMessage}")
            }

            page(pageCount)
        }
        return thumbnailBitmap
    }


    fun saveBitmapToMediaStore( bitmap: Bitmap): Uri? {
        val contentResolver: ContentResolver = application.contentResolver
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

}