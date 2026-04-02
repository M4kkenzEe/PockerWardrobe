package com.ownstd.project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.ownstd.project.card.internal.deeplink.DeepLinkManager

private val DARK_GREY_COLOR = Color(0xFF2F3033)

class MainActivity : ComponentActivity() {
    private val deepLinkManager = DeepLinkManager.getInstance()

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.navigationBarColor = DARK_GREY_COLOR.toArgb()

        // Handle initial deep link
        handleDeepLink(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle new deep link
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val uri = intent.data
        Log.d("MainActivity", "handleDeepLink - uri: $uri")

        uri?.toString()?.let { url ->
            val handled = deepLinkManager.handleDeepLinkUrl(url)
            Log.d("MainActivity", "Deep link handled: $handled")
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
