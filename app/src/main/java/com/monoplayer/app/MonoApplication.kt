package com.monoplayer.app

import android.app.Application
import com.monoplayer.app.data.db.MonoDatabase
import com.monoplayer.app.data.media.MediaStoreRepository
import com.monoplayer.app.data.prefs.SettingsStore
import com.monoplayer.app.playback.PlaybackConnection

class MonoApplication : Application() {
    val database by lazy { MonoDatabase.create(this) }
    val settings by lazy { SettingsStore(this) }
    val library by lazy { MediaStoreRepository(this, database.trackDao()) }
    val playback by lazy { PlaybackConnection(this, database.trackDao()) }
}
