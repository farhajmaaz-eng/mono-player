package com.monoplayer.app.data.prefs

import android.content.Context
import com.monoplayer.app.domain.MonoTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _theme = MutableStateFlow(MonoTheme.valueOf(prefs.getString("theme", MonoTheme.BLACK.name)!!))
    val theme = _theme.asStateFlow()
    var pauseOnDisconnect: Boolean
        get() = prefs.getBoolean("pause_disconnect", true)
        set(value) { prefs.edit().putBoolean("pause_disconnect", value).apply() }
    fun setTheme(value: MonoTheme) { prefs.edit().putString("theme", value.name).apply(); _theme.value = value }
}
