package com.example.pdfconverterc1.utils

import android.text.format.DateFormat
import java.util.Calendar
import java.util.Locale

object Methords {

    fun formatTimeStamp(timeStamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timeStamp

        return DateFormat.format("dd/MM/yyyy",calendar).toString()
    }
}