package com.monoplayer.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.monoplayer.app.domain.*
import com.monoplayer.app.playback.PlaybackState
import com.monoplayer.app.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

private enum class Tab(val label: String) { HOME("Home"), SONGS("Songs"), ALBUMS("Albums"), ARTISTS("Artists"), PLAYLISTS("Playlists") }
@Composable fun MonoApp(vm: LibraryViewModel) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }; var nowPlaying by rememberSaveable { mutableStateOf(false) }; var search by rememberSaveable { mutableStateOf(false) }
    val state by vm.state.collectAsState(); val playback by vm.playback.state.collectAsState(); val scope = rememberCoroutineScope()
    Scaffold(containerColor = MaterialTheme.colorScheme.background, contentWindowInsets = WindowInsets(0), bottomBar = {
        Column { if (playback.current != null) MiniPlayer(playback, vm, { nowPlaying = true })
            NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) { Tab.entries.forEach { item -> NavigationBarItem(selected=tab==item, onClick={ tab=item; search=false }, icon={ Icon(tabIcon(item), item.label) }, label={ Text(item.label) }, colors=NavigationBarItemDefaults.colors(indicatorColor=MaterialTheme.colorScheme.onBackground, selectedIconColor=MaterialTheme.colorScheme.background, selectedTextColor=MaterialTheme.colorScheme.onBackground, unselectedIconColor=MaterialTheme.colorScheme.onSurfaceVariant, unselectedTextColor=MaterialTheme.colorScheme.onSurfaceVariant)) } }
        }
    }) { pad -> Box(Modifier.padding(pad).fillMaxSize()) {
        when { search -> SearchScreen(state.tracks, vm, { search=false }); tab == Tab.HOME -> HomeScreen(state.tracks, vm, { tab=Tab.SONGS }); tab == Tab.SONGS -> SongsScreen(state, vm, { search=true }); tab == Tab.ALBUMS -> AlbumsScreen(state.tracks, vm); tab == Tab.ARTISTS -> ArtistsScreen(state.tracks, vm); tab == Tab.PLAYLISTS -> PlaylistsScreen(vm) }
        AnimatedVisibility(nowPlaying, enter=slideInVertically { it }, exit=slideOutVertically { it }) { NowPlaying(playback, vm, { nowPlaying=false }) }
    } }
}
@Composable private fun tabIcon(tab: Tab) = when(tab) { Tab.HOME -> Icons.Default.Home; Tab.SONGS -> Icons.Default.QueueMusic; Tab.ALBUMS -> Icons.Default.Album; Tab.ARTISTS -> Icons.Default.Person; Tab.PLAYLISTS -> Icons.Default.QueueMusic }
@Composable private fun Header(title: String, trailing: (@Composable () -> Unit)? = null) = Row(Modifier.fillMaxWidth().padding(24.dp, 24.dp, 18.dp), verticalAlignment=Alignment.CenterVertically) { Text(title, fontSize=34.sp, fontWeight=FontWeight.Bold, letterSpacing=(-1).sp, modifier=Modifier.weight(1f)); trailing?.invoke() }
@Composable private fun HomeScreen(tracks: List<Track>, vm: LibraryViewModel, songs: () -> Unit) = LazyColumn(contentPadding=PaddingValues(bottom=24.dp)) { item { Header("MonoPlayer", { IconButton(onClick=songs) { Icon(Icons.Default.Search, "Search library") } }) }; if (tracks.isEmpty()) item { Empty("Your music will appear here", "Allow music access, then add audio files to your device.") } else { item { HomeSection("Recently added", tracks.sortedByDescending { it.dateAdded }.take(8), vm) }; val fav=tracks.filter { it.favorite }; if(fav.isNotEmpty()) item { HomeSection("Favorites", fav.take(8), vm) }; val played=tracks.filter { it.playCount>0 }.sortedByDescending { it.lastPlayed }; if(played.isNotEmpty()) item { HomeSection("Recently played", played.take(8), vm) } } }
@Composable private fun HomeSection(title: String, tracks: List<Track>, vm: LibraryViewModel) { Text(title, fontSize=21.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(24.dp,14.dp)); LazyRow(contentPadding=PaddingValues(horizontal=24.dp), horizontalArrangement=Arrangement.spacedBy(14.dp)) { items(tracks, key={it.id}) { t -> Column(Modifier.width(144.dp).clickable { vm.play(tracks, tracks.indexOf(t)) }) { Artwork(t, Modifier.size(144.dp)); Text(t.title, maxLines=1,overflow=TextOverflow.Ellipsis,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=8.dp)); Text(t.artist,maxLines=1,overflow=TextOverflow.Ellipsis,color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=13.sp) } } } }
@Composable private fun SongsScreen(state: com.monoplayer.app.viewmodel.LibraryUiState, vm: LibraryViewModel, search: () -> Unit) = LazyColumn(contentPadding=PaddingValues(bottom=24.dp)) { item { Header("Songs", { Row { IconButton(onClick=search){Icon(Icons.Default.Search,"Search")}; SortMenu(state.sort, vm::setSort) } }) }; if (state.loading) item { Empty("Scanning your library", "This happens locally on your device.") } else if(state.tracks.isEmpty()) item { Empty("No music found", "Add MP3, FLAC, M4A, WAV, OGG, AAC, or OPUS files and rescan.") } else itemsIndexed(state.tracks,key={_,it->it.id}) { i,t -> TrackRow(t, { vm.play(state.tracks,i) }, { vm.favorite(t) }) } }
@Composable private fun SortMenu(current: SongSort, choose: (SongSort)->Unit) { var open by remember { mutableStateOf(false) }; Box { IconButton(onClick={open=true}){Icon(Icons.Default.Sort,"Sort songs")}; DropdownMenu(expanded=open,onDismissRequest={open=false}) { SongSort.entries.forEach { s -> DropdownMenuItem(text={Text(s.name.replace('_',' ').lowercase().replaceFirstChar { it.uppercase() })},onClick={choose(s);open=false}) } } } }
@OptIn(ExperimentalFoundationApi::class) @Composable fun TrackRow(t: Track, click:()->Unit, favorite:()->Unit) = Row(Modifier.fillMaxWidth().combinedClickable(onClick=click,onLongClick=favorite).padding(horizontal=24.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) { Artwork(t,Modifier.size(52.dp)); Column(Modifier.padding(start=14.dp).weight(1f)) { Text(t.title,maxLines=1,overflow=TextOverflow.Ellipsis,fontWeight=FontWeight.Medium); Text(t.artist,maxLines=1,overflow=TextOverflow.Ellipsis,color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=14.sp) }; Text(t.duration.asTime(),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=12.sp); IconButton(onClick=favorite) { Icon(if(t.favorite) Icons.Default.Favorite else Icons.Default.MoreVert, if(t.favorite) "Remove favorite" else "Song options") } }
@Composable fun Artwork(t: Track, modifier: Modifier) = AsyncImage(model=android.content.ContentUris.withAppendedId(android.net.Uri.parse("content://media/external/audio/albumart"),t.albumId), contentDescription="Album artwork for ${t.album}",contentScale=ContentScale.Crop,modifier=modifier.clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant))
@Composable private fun AlbumsScreen(tracks:List<Track>,vm:LibraryViewModel) { val albums=tracks.groupBy{it.albumId}.values.sortedBy{it.first().album}; LazyVerticalGrid(columns=GridCells.Adaptive(150.dp),contentPadding=PaddingValues(24.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) { item(span={GridItemSpan(maxLineSpan)}) { Header("Albums") }; items(albums,key={it.first().albumId}) { a -> Column(Modifier.clickable { vm.play(a.sortedBy{it.trackNumber}) }) { Artwork(a.first(),Modifier.aspectRatio(1f));Text(a.first().album,maxLines=1,overflow=TextOverflow.Ellipsis,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=7.dp));Text(a.first().artist,maxLines=1,overflow=TextOverflow.Ellipsis,color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=13.sp) } } } }
@Composable private fun ArtistsScreen(tracks:List<Track>,vm:LibraryViewModel) { val artists=tracks.groupBy { it.artist }.toSortedMap(String.CASE_INSENSITIVE_ORDER); LazyColumn { item { Header("Artists") }; artists.forEach { (name,songs) -> item(key=name) { Row(Modifier.fillMaxWidth().clickable{vm.play(songs)}.padding(24.dp,12.dp),verticalAlignment=Alignment.CenterVertically) { Surface(Modifier.size(54.dp),shape=MaterialTheme.shapes.extraLarge,color=MaterialTheme.colorScheme.surfaceVariant){};Column(Modifier.padding(start=15.dp)){Text(name,fontWeight=FontWeight.SemiBold);Text("${songs.size} songs · ${songs.map{it.album}.distinct().size} albums",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=13.sp)} } } } } }
@Composable private fun PlaylistsScreen(vm: LibraryViewModel) { val ps by vm.playlistItems.collectAsState(emptyList()); var creating by remember{mutableStateOf(false)}; var name by remember{mutableStateOf("")}; LazyColumn { item { Header("Playlists",{IconButton(onClick={creating=true}){Icon(Icons.Default.Add,"Create playlist")}}) }; if(ps.isEmpty()) item { Empty("No playlists yet","Create playlists that stay on this device.") }; items(ps,key={it.id}){p->Row(Modifier.fillMaxWidth().padding(24.dp,12.dp),verticalAlignment=Alignment.CenterVertically){Surface(Modifier.size(52.dp),shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceVariant){};Column(Modifier.padding(start=14.dp).weight(1f)){Text(p.name,fontWeight=FontWeight.SemiBold);Text("${p.songCount} songs",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=13.sp)};IconButton(onClick={vm.deletePlaylist(Playlist(p.id,p.name,p.createdAt,p.songCount))}){Icon(Icons.Default.Delete,"Delete playlist")}}} }; if(creating) AlertDialog(onDismissRequest={creating=false},title={Text("New playlist")},text={OutlinedTextField(value=name,onValueChange={name=it},singleLine=true,label={Text("Name")})},confirmButton={TextButton(onClick={vm.createPlaylist(name);creating=false}){Text("Create")}},dismissButton={TextButton(onClick={creating=false}){Text("Cancel")}}) }
