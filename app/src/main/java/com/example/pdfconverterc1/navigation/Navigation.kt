package com.example.pdfconverterc1.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pdfconverterc1.screens.HomeScreen
import com.example.pdfconverterc1.screens.ImageScreen
import com.example.pdfconverterc1.screens.PdfViewer
import com.example.pdfconverterc1.viewmodel.PdfViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Navigation() {
    val navHostController = rememberNavController()
    val viewModel: PdfViewModel = hiltViewModel()


    NavHost(navController = navHostController, startDestination = "Home Screen") {
        composable(route = "Home Screen") {
            HomeScreen(navHostController, viewModel)
        }
        composable(route = "Image Screen") {
            ImageScreen(navHostController, viewModel)
        }
        composable(route = "PDF View Screen") {
            PdfViewer(navHostController, viewModel)
        }
    }

}