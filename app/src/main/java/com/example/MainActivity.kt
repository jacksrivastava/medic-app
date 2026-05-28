package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.MainAppContent
import com.example.ui.MedViewModel
import com.example.ui.MedViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create clinical notification channel on app startup
        NotificationHelper.createNotificationChannel(applicationContext)

        val viewModel: MedViewModel = ViewModelProvider(
            this,
            MedViewModelFactory(applicationContext)
        )[MedViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}
