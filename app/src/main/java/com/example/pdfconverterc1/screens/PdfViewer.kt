package com.example.pdfconverterc1.screens


import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.pdfconverterc1.model.PdfData
import com.example.pdfconverterc1.viewmodel.PdfViewModel
import com.example.pdfconverterc1.viewmodel.events.PdfEvent
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewer(navHostController: NavHostController, viewModel: PdfViewModel) {

    val context = LocalContext.current
    val pdf: PdfData? = viewModel.getPdf()
    var share by remember { mutableStateOf(false) }


    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            TopAppBar(
                title = {
                    Text(text = pdf!!.file.name)
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

                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = {
                        viewModel.onEvent(PdfEvent.DeletePDF(pdf!!) { delete ->
                            if (delete) {
                                Toast.makeText(context, "Pdf Deleted", Toast.LENGTH_SHORT).show()
                                navHostController.navigateUp()
                            } else {
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })


                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }

    ) { paddingValues ->

        Column(modifier = Modifier.padding(paddingValues)) {


            if (pdf != null) {
                PdfView(pdf.file)
            } else {
                Toast.makeText(LocalContext.current, "null", Toast.LENGTH_SHORT).show()
            }

            if (share) {
                SharePdfButton(pdf!!.uri, onButtonClick = {
                    share = true
                })
            }


        }
    }
}

@Composable
fun PdfView(pdfFile: File) {
    val context = LocalContext.current
    val pdfView = remember { PDFView(context, null) }

    var currentPage by remember { mutableStateOf(0) }
    var pageCount by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        pdfView.fromFile(pdfFile)
            .defaultPage(0)
            .onPageChange { page, newPageCount ->
                currentPage = page
                pageCount = newPageCount
            }
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onLoad(object : OnLoadCompleteListener {
                override fun loadComplete(numberOfPages: Int) {
                    pageCount = numberOfPages
                }
            })
            .scrollHandle(DefaultScrollHandle(context))
            .load()

        onDispose {
            pdfView.recycle()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (pageCount > 0) {
            Text(
                text = "Page ${currentPage + 1} of $pageCount",
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.Black)
                    .padding(4.dp)
                    .clickable {
                        // Handle click event if needed
                    },
                color = Color.White
            )
        }

        AndroidView(
            factory = { pdfView },
            modifier = Modifier.fillMaxSize()
        )
    }
}


@Composable
fun SharePdfButton(pdfUri: Uri, onButtonClick: () -> Unit) {
    val context = LocalContext.current
    val activity = (context as? ComponentActivity)
    val shareLauncher = remember {
        activity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            // Handle the result if needed

        }
    }
    Button(

        onClick = {
            onButtonClick()
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri)
            shareLauncher?.launch(Intent.createChooser(intent, "Share PDF"))
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Share PDF")
    }
}

//@Composable
//fun SharePdf(pdfFile: File,context) {
//    val contentUri = FileProvider.getUriForFile(
//        context,
//        "${context.packageName}.fileprovider",
//        pdfFile
//    )
//
//    val sharePdfLauncher =
//        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
//            if (uri != null) {
//                val shareIntent = Intent(Intent.ACTION_SEND).apply {
//                    type = "application/pdf"
//                    putExtra(Intent.EXTRA_STREAM, uri)
//                }
//                context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
//            }
//        }
//
//    Column(
//        modifier = Modifier.padding(16.dp)
//    ) {
//        Text(text = "Share PDF Example")
//
//        Spacer(modifier = Modifier.padding(8.dp))
//
//        Button(
//            onClick = {
//                sharePdfLauncher.launch("document.pdf")
//            }
//        ) {
//            Text("Share PDF")
//        }
//    }
//}