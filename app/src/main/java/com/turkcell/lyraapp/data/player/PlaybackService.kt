package com.turkcell.lyraapp.data.player

import android.content.Intent
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var playerRepository: PlayerRepository

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        val basePlayer = (playerRepository as DefaultPlayerRepository).player

        val forwardingPlayer = object : ForwardingPlayer(basePlayer) {
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
            }

            override fun seekToNext() {
                serviceScope.launch {
                    playerRepository.skipToNext()
                }
            }

            override fun seekToNextMediaItem() {
                serviceScope.launch {
                    playerRepository.skipToNext()
                }
            }

            override fun seekToPrevious() {
                serviceScope.launch {
                    playerRepository.skipToPrevious()
                }
            }

            override fun seekToPreviousMediaItem() {
                serviceScope.launch {
                    playerRepository.skipToPrevious()
                }
            }
        }

        val intent = Intent(this, com.turkcell.lyraapp.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val session = MediaSession.Builder(this, forwardingPlayer)
            .setSessionActivity(pendingIntent)
            .build()
        
        mediaSession = session
        addSession(session)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null && (!player.playWhenReady || player.playbackState == Player.STATE_IDLE)) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            removeSession(this)
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
