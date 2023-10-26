package com.oguzhaneksi.media3demo.ui.theme

import androidx.media3.common.Player
import androidx.media3.session.MediaSession

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class MediaSessionCallback: MediaSession.Callback {
    override fun  onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val sessionCommands = connectionResult.availableSessionCommands
        val playerCommands = Player.Commands.Builder()
            .add(Player.COMMAND_PLAY_PAUSE)
            .add(Player.COMMAND_SEEK_BACK)
            .add(Player.COMMAND_SEEK_FORWARD)
            .build()
        return MediaSession.ConnectionResult.accept(
            sessionCommands, playerCommands
        )
    }
}