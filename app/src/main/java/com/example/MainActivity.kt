package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.view.BotDashboardScreen
import com.example.ui.viewmodel.BotViewModel

class MainActivity : ComponentActivity() {
  private val botViewModel: BotViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isAmoledState = botViewModel.isAmoled.collectAsState()
      val accentColorState = botViewModel.accentColorHex.collectAsState()

      MyApplicationTheme(
        isAmoled = isAmoledState.value,
        accentColor = accentColorState.value
      ) {
        BotDashboardScreen(
          viewModel = botViewModel,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
