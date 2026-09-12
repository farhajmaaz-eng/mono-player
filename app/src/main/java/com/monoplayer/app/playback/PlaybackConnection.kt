package com.monoplayer.app.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.monoplayer.app.data.db.TrackDao
import com.monoplayer.app.domain.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaybackState(val current: Track? = null, val isPlaying: Boolean = false, val position: Long = 0, val duration: Long = 0, val repeatMode: Int = Player.REPEAT_MODE_OFF, val shuffled: Boolean = false, val queue: List<Track> = emptyList(), val index: Int = -1)
class PlaybackConnection(context: Context, private val stateDao: TrackDao) {
    private val _state = MutableStateFlow(PlaybackState()); val state = _state.asStateFlow()
    private var queue = emptyList<Track>()
    private val controllerFuture = MediaController.Builder(context, SessionToken(context, ComponentName(context, MonoPlaybackService::class.java))).buildAsync()
    private var controller: MediaController? = null
    init { controllerFuture.addListener({ controller = controllerFuture.get().also { c -> c.addListener(listener); publish(c) } }, MoreExecutors.directExecutor()) }
    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) { publish(player) }
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { mediaItem?.mediaId?.toLongOrNull()?.let { id -> kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { stateDao.played(id, 0) } }; publish(controller ?: return) }
    }
    private fun publish(player: Player) { val i = player.currentMediaItemIndex; _state.value = PlaybackState(queue.getOrNull(i), player.isPlaying, player.currentPosition, player.duration.coerceAtLeast(0), player.repeatMode, player.shuffleModeEnabled, queue, i) }
    fun play(tracks: List<Track>, at: Int = 0) { queue = tracks; controller?.apply { setMediaItems(tracks.map { it.item() }, at, 0); prepare(); play() } }
    fun toggle() { controller?.let { if (it.isPlaying) it.pause() else it.play() } }
    fun next() { controller?.seekToNextMediaItem() }; fun previous() { controller?.seekToPreviousMediaItem() }; fun seek(position: Long) { controller?.seekTo(position) }
    fun shuffle() { controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }; fun cycleRepeat() { controller?.let { it.repeatMode = when(it.repeatMode) { Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL; Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE; else -> Player.REPEAT_MODE_OFF } } }
    fun addNext(track: Track) { val c = controller ?: return; val pos = (c.currentMediaItemIndex + 1).coerceAtLeast(0); queue = queue.toMutableList().also { it.add(pos, track) }; c.addMediaItem(pos, track.item()) }
    fun add(track: Track) { queue = queue + track; controller?.addMediaItem(track.item()) }
    fun remove(index: Int) { queue = queue.toMutableList().also { it.removeAt(index) }; controller?.removeMediaItem(index) }
    fun move(from: Int, to: Int) { queue = queue.toMutableList().also { it.add(to, it.removeAt(from)) }; controller?.moveMediaItem(from, to) }
    fun clear() { queue = emptyList(); controller?.clearMediaItems() }
    private fun Track.item() = MediaItem.Builder().setMediaId(id.toString()).setUri(uri).setMediaMetadata(MediaMetadata.Builder().setTitle(title).setArtist(artist).setAlbumTitle(album).setArtworkUri(android.content.ContentUris.withAppendedId(android.net.Uri.parse("content://media/external/audio/albumart"), albumId)).build()).build()
}
