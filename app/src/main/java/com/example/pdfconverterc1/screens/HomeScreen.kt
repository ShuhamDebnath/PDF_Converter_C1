package com.example.pdfconverterc1.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pdfconverterc1.R
import com.example.pdfconverterc1.model.PdfData
import com.example.pdfconverterc1.utils.Constants
import com.example.pdfconverterc1.utils.Methords
import com.example.pdfconverterc1.viewmodel.PdfViewModel
import com.example.pdfconverterc1.viewmodel.events.PdfEvent
import java.io.File
import java.util.concurrent.Executors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navHostController: NavHostController, viewModel: PdfViewModel) {


    val context = LocalContext.current
    val pdfList = viewModel.getPdfEventFlow.value

    viewModel.onEvent(PdfEvent.GetPDF)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),

        floatingActionButton = {

            FloatingActionButton(onClick = {
                navHostController.navigate(route = "Image Screen")
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add image")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Home ")
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

            if (pdfList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.pdf_logo),
                        contentDescription = "logo",
                        modifier = Modifier.align(Alignment.Center)

                    )
                    Text(
                        text = "No Pdf created",
                        modifier = Modifier.align(Alignment.BottomCenter),
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    )
                }
            } else {
                LazyColumn() {

                    items(pdfList) {
                        PdfBox(pdf = it, viewModel, onBoxClicked = {
                            //navigate to pdf view page
                            viewModel.setPdf(it)
                            navHostController.navigate(route = "PDF View Screen")
                        }, onMoreClicked = {
                            Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show()
                        })
                    }

                }
            }


        }

    }


}

@Composable
fun PdfBox(pdf: PdfData, viewModel: PdfViewModel, onBoxClicked: () -> Unit, onMoreClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(12.dp)
            .background(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
            .clickable {
                onBoxClicked()
            }
    ) {


        val name = pdf.file.name
        val timestamp = pdf.file.lastModified()
        val date = Methords.formatTimeStamp(timestamp)
        val size = viewModel.loadFileSize(pdf)

        Row(
            Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_pdf),
                contentDescription = "pdf",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )

            Column(modifier = Modifier.weight(4f)) {

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(text = name, modifier = Modifier.align(Alignment.CenterStart))

                        IconButton(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            onClick = onMoreClicked
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "more"
                            )
                        }
                    }

                }
                Row(modifier = Modifier.fillMaxWidth()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 2.dp)
                    ) {
                        Text(text = size, Modifier.align(Alignment.CenterStart))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = date, Modifier.align(Alignment.CenterEnd))
                    }
                }
            }
        }
    }
}



