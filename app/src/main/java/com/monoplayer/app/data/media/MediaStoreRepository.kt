package com.monoplayer.app.data.media

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.monoplayer.app.data.db.TrackDao
import com.monoplayer.app.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class MediaStoreRepository(private val context: Context, private val stateDao: TrackDao) {
    private val refreshes = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }
    private val observer = object : android.database.ContentObserver(null) { override fun onChange(selfChange: Boolean) { refresh() } }
    init { context.contentResolver.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer) }
    fun refresh() { refreshes.tryEmit(Unit) }
    val tracks: Flow<List<Track>> = refreshes.flatMapLatest { flow { emit(queryTracks()) } }.combine(stateDao.states()) { media, states ->
        val map = states.associateBy { it.mediaId }; media.map { t -> map[t.id]?.let { s -> t.copy(favorite=s.favorite, playCount=s.playCount, lastPlayed=s.lastPlayed) } ?: t }
    }.flowOn(Dispatchers.IO).stateIn(kotlinx.coroutines.CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5_000), emptyList())
    private suspend fun queryTracks() = withContext(Dispatchers.IO) {
        val base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val columns = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.TRACK, MediaStore.Audio.Media.YEAR, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.DATA)
        context.contentResolver.query(base, columns, "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 15000", null, "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE")?.use { c ->
            fun ix(name: String) = c.getColumnIndexOrThrow(name)
            buildList { while (c.moveToNext()) add(Track(c.getLong(ix(MediaStore.Audio.Media._ID)), c.getString(ix(MediaStore.Audio.Media.TITLE)) ?: "Unknown title", c.getString(ix(MediaStore.Audio.Media.ARTIST)) ?: "Unknown artist", c.getString(ix(MediaStore.Audio.Media.ALBUM)) ?: "Unknown album", c.getString(ix(MediaStore.Audio.Media.ALBUM_ARTIST)), c.getLong(ix(MediaStore.Audio.Media.DURATION)), c.getLong(ix(MediaStore.Audio.Media.ALBUM_ID)), c.getInt(ix(MediaStore.Audio.Media.TRACK)) % 1000, c.getInt(ix(MediaStore.Audio.Media.YEAR)).takeIf { it > 0 }, c.getLong(ix(MediaStore.Audio.Media.DATE_ADDED))*1000, ContentUris.withAppendedId(base, c.getLong(ix(MediaStore.Audio.Media._ID))), c.getString(ix(MediaStore.Audio.Media.MIME_TYPE)), c.getLong(ix(MediaStore.Audio.Media.SIZE)), c.getString(ix(MediaStore.Audio.Media.DATA)))) }
        } ?: emptyList()
    }
    fun albumArt(id: Long) = ContentUris.withAppendedId(android.net.Uri.parse("content://media/external/audio/albumart"), id)
    suspend fun setFavorite(id: Long, favorite: Boolean) { stateDao.favorite(id, favorite) }
}
