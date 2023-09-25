package com.example.pdfconverterc1.viewmodel.events


import com.example.pdfconverterc1.model.ImageData
import com.example.pdfconverterc1.model.PdfData

sealed class PdfEvent {
    object GetPDF : PdfEvent()
    data class CreatePDF(
        val title: String,
        val imageList: List<ImageData>,
        val pdfConverted: () -> Unit
    ) : PdfEvent()

    data class OpenPDF(val pdf: PdfData) : PdfEvent()
    data class DeletePDF(val pdf: PdfData,val isDeleted: (Boolean) -> Unit) : PdfEvent()

}