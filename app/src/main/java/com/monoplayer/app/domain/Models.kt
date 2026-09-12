package com.monoplayer.app.domain

import android.net.Uri

data class Track(
    val id: Long, val title: String, val artist: String, val album: String, val albumArtist: String?,
    val duration: Long, val albumId: Long, val trackNumber: Int, val year: Int?, val dateAdded: Long,
    val uri: Uri, val mimeType: String?, val size: Long, val path: String?, val genre: String? = null,
    val favorite: Boolean = false, val playCount: Int = 0, val lastPlayed: Long? = null
)
data class Album(val id: Long, val title: String, val artist: String, val year: Int?, val artwork: Uri?, val trackCount: Int, val duration: Long)
data class Artist(val name: String, val songCount: Int, val albumCount: Int)
data class Playlist(val id: Long, val name: String, val createdAt: Long, val songCount: Int = 0)
enum class SongSort { TITLE, ARTIST, ALBUM, DATE_ADDED, DURATION, MOST_PLAYED, RECENTLY_PLAYED }
enum class MonoTheme { BLACK, LIGHT, SYSTEM }
fun Long.asTime(): String { val total = this / 1000; return "%d:%02d".format(total / 60, total % 60) }
