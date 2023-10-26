package com.oguzhaneksi.media3demo

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerView
import com.oguzhaneksi.media3demo.ui.theme.Media3DemoTheme
import com.oguzhaneksi.media3demo.ui.theme.MediaSessionCallback

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Media3DemoTheme {
                // A surface container using the 'background' color from the theme
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
    var mediaSession: MediaSession? by remember {
        mutableStateOf(null)
    }
    val playerView = createPlayerView(player)

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                if (Build.VERSION.SDK_INT > 23) {
                    player = initPlayer(context)
                    val forwardingPlayer = initForwardingPlayer(context, player!!)
                    mediaSession = initMediaSession(context, forwardingPlayer)
                    playerView.onResume()
                }
            }
            Lifecycle.Event.ON_RESUME -> {
                if (Build.VERSION.SDK_INT <= 23) {
                    player = initPlayer(context)
                    val forwardingPlayer = initForwardingPlayer(context, player!!)
                    mediaSession = initMediaSession(context, forwardingPlayer)
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
                    mediaSession?.release()
                    mediaSession = null
                }
            }
            Lifecycle.Event.ON_STOP -> {
                if (Build.VERSION.SDK_INT > 23) {
                    playerView.apply {
                        player?.release()
                        onPause()
                        player = null
                    }
                    mediaSession?.release()
                    mediaSession = null
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

@androidx.annotation.OptIn(UnstableApi::class)
fun initForwardingPlayer(context: Context, player: Player): ForwardingPlayer {
    return object : ForwardingPlayer(player) {
        override fun play() {
            Toast.makeText(context, "Video oynatıldı", Toast.LENGTH_LONG).show()
            super.play()
        }

        override fun pause() {
            Toast.makeText(context, "Video durduruldu", Toast.LENGTH_LONG).show()
            super.pause()
        }

        override fun seekBack() {
            Toast.makeText(context, "Video geri sarıldı", Toast.LENGTH_LONG).show()
            super.seekBack()
        }

        override fun seekForward() {
            Toast.makeText(context, "Video ileri sarıldı", Toast.LENGTH_LONG).show()
            super.seekForward()
        }
    }
}

fun initMediaSession(context: Context, player: Player): MediaSession {
    return MediaSession.Builder(context, player)
        .setCallback(MediaSessionCallback())
        .build()
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