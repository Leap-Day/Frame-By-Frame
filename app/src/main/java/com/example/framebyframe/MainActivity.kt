package com.example.framebyframe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.framebyframe.ui.theme.FrameByFrameTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.media3.common.util.UnstableApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FrameByFrameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Greeting()
                        ButtonImportMedia()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting() {
    Text(
        text = "Frame by Frame",
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FrameByFrameTheme {
        Greeting()
    }
}

// Import Media Button
@Composable
fun ButtonImportMedia() {
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    // Set up the launcher for the file picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> videoUri = uri}

    // The button launches the file picker when clicked
    ElevatedButton(onClick = {launcher.launch("video/*")}) {
        Text("Import Video")
    }
    videoUri?.let { uri ->
        VideoPlayer(uri)
    }
}

@Composable
@OptIn(UnstableApi::class)
private fun VideoPlayer(uri: Uri) {
    val context = LocalContext.current

    // ExoPlayer
    val player = remember(uri) {
        ExoPlayer.Builder(context.applicationContext).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    // Frame Stepping Logic
    // Obtain Video Frame Rate
    val fps = player.videoFormat?.frameRate ?: 30f
    val singleFrameTime: Float = 1000 / fps

    // Move Video 1 Frame Forward
    fun NextFrame() {
        player.pause()
        player.seekTo((player.currentPosition + singleFrameTime).toLong())
    }
    fun PreviousFrame() {
        player.pause()
        player.seekTo((player.currentPosition - singleFrameTime).toLong())
    }



    // UI Elements
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ElevatedButton(onClick = {PreviousFrame()}) {
                Text("-1 Frame")
            }
            ElevatedButton(onClick = {player.play()}) {
                Text("Play")
            }
            ElevatedButton(onClick = {NextFrame()}) {
                Text("+1 Frame")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ElevatedButton(onClick = {}) {
                Text("Frame Marker 1")
            }
            Text("Frame Difference: ")
            ElevatedButton(onClick = {}) {
                Text("Frame Marker 2")
            }

        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(9 / 16f),
            factory = { context ->
                PlayerView(context).apply {
                    setShowPreviousButton(false)
                    setShowNextButton(false)
                } },
            update  = { it.player = player }
        )
    }


    DisposableEffect(player) {
        onDispose { player.release() }
    }
}
