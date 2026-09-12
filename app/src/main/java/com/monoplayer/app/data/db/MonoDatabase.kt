package com.monoplayer.app.data.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "track_state") data class TrackState(@PrimaryKey val mediaId: Long, val favorite: Boolean = false, val playCount: Int = 0, val lastPlayed: Long? = null, val lastPosition: Long = 0)
@Entity(tableName = "playlists") data class PlaylistEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val createdAt: Long = System.currentTimeMillis())
@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "mediaId"]) data class PlaylistTrackEntity(val playlistId: Long, val mediaId: Long, val position: Int)

@Dao interface TrackDao {
    @Query("SELECT * FROM track_state") fun states(): Flow<List<TrackState>>
    @Query("SELECT * FROM track_state WHERE mediaId = :id") suspend fun state(id: Long): TrackState?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(state: TrackState)
    @Transaction suspend fun favorite(id: Long, active: Boolean) { val s = state(id) ?: TrackState(id); put(s.copy(favorite = active)) }
    @Transaction suspend fun played(id: Long, position: Long) { val s = state(id) ?: TrackState(id); put(s.copy(playCount = s.playCount + 1, lastPlayed = System.currentTimeMillis(), lastPosition = position)) }
}
@Dao interface PlaylistDao {
    @Query("SELECT p.*, count(pt.mediaId) AS songCount FROM playlists p LEFT JOIN playlist_tracks pt ON p.id = pt.playlistId GROUP BY p.id ORDER BY p.name") fun playlists(): Flow<List<PlaylistWithCount>>
    @Insert suspend fun create(item: PlaylistEntity): Long
    @Update suspend fun update(item: PlaylistEntity)
    @Delete suspend fun delete(item: PlaylistEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun add(track: PlaylistTrackEntity)
    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND mediaId = :mediaId") suspend fun remove(playlistId: Long, mediaId: Long)
    @Query("SELECT mediaId FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position") suspend fun mediaIds(playlistId: Long): List<Long>
}
data class PlaylistWithCount(val id: Long, val name: String, val createdAt: Long, val songCount: Int)
@Database(entities = [TrackState::class, PlaylistEntity::class, PlaylistTrackEntity::class], version = 1, exportSchema = false)
abstract class MonoDatabase : RoomDatabase() { abstract fun trackDao(): TrackDao; abstract fun playlistDao(): PlaylistDao
    companion object { fun create(context: Context) = Room.databaseBuilder(context, MonoDatabase::class.java, "mono-player.db").fallbackToDestructiveMigration().build() }
}
