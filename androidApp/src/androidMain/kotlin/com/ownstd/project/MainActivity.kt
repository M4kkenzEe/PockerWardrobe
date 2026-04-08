package com.ownstd.project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ownstd.project.core.deeplink.DeepLinkManager

class MainActivity : ComponentActivity() {

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleDeepLink(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val uri = intent.data
        Log.d("MainActivity", "handleDeepLink - uri: $uri")
        uri?.toString()?.let { url ->
            DeepLinkManager.emit(url)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
