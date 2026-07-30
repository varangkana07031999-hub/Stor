package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.WarehouseViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.FiveMTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WarehouseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FiveMTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
