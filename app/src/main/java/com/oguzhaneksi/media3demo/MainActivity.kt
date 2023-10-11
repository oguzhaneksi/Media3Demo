package com.oguzhaneksi.media3demo

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.oguzhaneksi.media3demo.ui.theme.Media3DemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Media3DemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Player()
                }
            }
        }
    }
}

@Composable
fun Player() {
    val context = LocalContext.current

    var player: Player? by remember {
        mutableStateOf(null)
    }
    val playerView = createPlayerView(player)

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                if (Build.VERSION.SDK_INT > 23) {
                    player = initPlayer(context)
                    playerView.onResume()
                }
            }
            Lifecycle.Event.ON_RESUME -> {
                if (Build.VERSION.SDK_INT <= 23) {
                    player = initPlayer(context)
                    playerView.onResume()
                }
            }
            Lifecycle.Event.ON_PAUSE -> {
                if (Build.VERSION.SDK_INT <= 23) {
                    playerView.apply {
                        player?.release()
                        onPause()
                        player = null
                    }
                }
            }
            Lifecycle.Event.ON_STOP -> {
                if (Build.VERSION.SDK_INT > 23) {
                    playerView.apply {
                        player?.release()
                        onPause()
                        player = null
                    }
                }
            }
            else -> {}
        }
    }

    AndroidView(
        factory = { playerView }
    )
}

@Composable
fun ComposableLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

fun initPlayer(context: Context): Player {
    return ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
        playWhenReady = true
        prepare()
    }
}

@Composable
fun createPlayerView(player: Player?): PlayerView {
    val context = LocalContext.current
    val playerView = remember {
        PlayerView(context).apply {
            this.player = player
        }
    }
    DisposableEffect(key1 = player) {
        playerView.player = player
        onDispose {
            playerView.player = null
        }
    }
    return playerView
}