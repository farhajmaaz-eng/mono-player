package com.monoplayer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monoplayer.app.data.db.PlaylistDao
import com.monoplayer.app.data.db.PlaylistEntity
import com.monoplayer.app.data.db.PlaylistTrackEntity
import com.monoplayer.app.data.media.MediaStoreRepository
import com.monoplayer.app.data.prefs.SettingsStore
import com.monoplayer.app.domain.*
import com.monoplayer.app.playback.PlaybackConnection
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryUiState(val tracks: List<Track> = emptyList(), val query: String = "", val sort: SongSort = SongSort.TITLE, val loading: Boolean = true)
class LibraryViewModel(private val library: MediaStoreRepository, private val playlists: PlaylistDao, val playback: PlaybackConnection, val settings: SettingsStore) : ViewModel() {
    private val query = MutableStateFlow(""); private val sort = MutableStateFlow(SongSort.TITLE)
    val state = combine(library.tracks, query, sort) { tracks, q, s -> LibraryUiState(sortTracks(tracks, s).filter { q.isBlank() || listOf(it.title,it.artist,it.album).any { x -> x.contains(q, true) } }, q, s, false) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())
    val allTracks = library.tracks
    val playlistItems = playlists.playlists()
    fun setQuery(value: String) { query.value = value }; fun setSort(value: SongSort) { sort.value = value }; fun rescan() = library.refresh()
    fun play(list: List<Track>, index: Int = 0) = playback.play(list, index)
    fun favorite(track: Track) = viewModelScope.launch { library.setFavorite(track.id, !track.favorite) }
    fun createPlaylist(name: String) = viewModelScope.launch { if (name.isNotBlank()) playlists.create(PlaylistEntity(name = name.trim())) }
    fun deletePlaylist(p: Playlist) = viewModelScope.launch { playlists.delete(PlaylistEntity(p.id, p.name, p.createdAt)) }
    fun addToPlaylist(id: Long, track: Track) = viewModelScope.launch { playlists.add(PlaylistTrackEntity(id, track.id, Int.MAX_VALUE)) }
    private fun sortTracks(list: List<Track>, s: SongSort) = when(s) { SongSort.TITLE -> list.sortedBy { it.title.lowercase() }; SongSort.ARTIST -> list.sortedBy { it.artist.lowercase() }; SongSort.ALBUM -> list.sortedBy { it.album.lowercase() }; SongSort.DATE_ADDED -> list.sortedByDescending { it.dateAdded }; SongSort.DURATION -> list.sortedByDescending { it.duration }; SongSort.MOST_PLAYED -> list.sortedByDescending { it.playCount }; SongSort.RECENTLY_PLAYED -> list.sortedByDescending { it.lastPlayed ?: 0 } }
}
class LibraryViewModelFactory(private val library: MediaStoreRepository, private val playlists: PlaylistDao, private val playback: PlaybackConnection, private val settings: SettingsStore) : androidx.lifecycle.ViewModelProvider.Factory { @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = LibraryViewModel(library, playlists, playback, settings) as T }
