package com.monoplayer.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monoplayer.app.ui.MonoApp
import com.monoplayer.app.ui.theme.MonoTheme
import com.monoplayer.app.viewmodel.LibraryViewModel
import com.monoplayer.app.viewmodel.LibraryViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); enableEdgeToEdge()
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        val request = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) request.launch(permission)
        setContent { val app = application as MonoApplication; val vm: LibraryViewModel = viewModel(factory = LibraryViewModelFactory(app.library, app.database.playlistDao(), app.playback, app.settings)); val theme by app.settings.theme.collectAsState(); MonoTheme(theme) { MonoApp(vm) } }
    }
}
