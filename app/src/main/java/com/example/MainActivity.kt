package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.view.BotDashboardScreen
import com.example.ui.viewmodel.BotViewModel

class MainActivity : ComponentActivity() {
  private val botViewModel: BotViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )
    setContent {
      val isAmoled by botViewModel.isAmoled.collectAsStateWithLifecycle()
      val accentColor by botViewModel.accentColorHex.collectAsStateWithLifecycle()
      val highContrast by botViewModel.highContrastOn.collectAsStateWithLifecycle()
      val reduceMotion by botViewModel.reduceMotionOn.collectAsStateWithLifecycle()

      MyApplicationTheme(
        isAmoled = isAmoled,
        accentColor = accentColor,
        highContrast = highContrast,
        reduceMotion = reduceMotion
      ) {
        BotDashboardScreen(
          viewModel = botViewModel,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
