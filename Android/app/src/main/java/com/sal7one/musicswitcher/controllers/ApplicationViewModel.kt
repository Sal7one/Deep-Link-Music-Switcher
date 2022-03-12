package com.sal7one.musicswitcher.controllers

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sal7one.musicswitcher.repository.DataStoreProvider
import com.sal7one.musicswitcher.utils.Constants
import com.sal7one.musicswitcher.utils.typeofLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TheScreenUiData(
    val provider: String = "",
    val playList: Boolean = false,
    val albums: Boolean = false,
    val appleMusic: Boolean = false,
    val spotify: Boolean = false,
    val anghami: Boolean = false,
    val ytMusic: Boolean = false,
    val deezer: Boolean = false
)

class ApplicationViewModel(

    private val dataStoreManager: DataStoreProvider
) : ViewModel() {

    //  val chosenProvider: MutableState<String> = mutableStateOf("")
    val musicPackage: MutableState<String> = mutableStateOf("")
    val searchLink: MutableState<String> = mutableStateOf("")
    val playlistChoice: MutableState<Boolean> = mutableStateOf(false)
    val albumChoice: MutableState<Boolean> = mutableStateOf(false)
    val appleMusicChoice: MutableState<Boolean> = mutableStateOf(false)
    val spotifyChoice: MutableState<Boolean> = mutableStateOf(false)
    val anghamiChoice: MutableState<Boolean> = mutableStateOf(false)
    val ytMusicChoice: MutableState<Boolean> = mutableStateOf(false)
    val deezerChoice: MutableState<Boolean> = mutableStateOf(false)

    val sameApp = MutableLiveData<Boolean>()
    val differentApp = MutableLiveData<Boolean>()

    private val _myUiState = MutableStateFlow(TheScreenUiData())
    val myUiState: StateFlow<TheScreenUiData> = _myUiState

    private var isAlbum = true
    private var isPlaylist = true
    private var overrulesPreference = false

    init {
        sameApp.value = false
        differentApp.value = false
        getData()
    }

    fun saveData(
        userChoice: String,
        userPlaylist: Boolean,
        userAlbum: Boolean,
    ) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreManager.saveToDataStore(
            userMusicProvider = userChoice,
            userPlaylist = userPlaylist,
            userAlbum = userAlbum,
        )
    }

    fun saveExceptions(
        appleMusic: Boolean,
        spotify: Boolean,
        anghami: Boolean,
        ytMusic: Boolean,
        deezer: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreManager.saveExceptions(
            appleMusic = appleMusic,
            spotify = spotify,
            anghami = anghami,
            ytMusic = ytMusic,
            deezer = deezer
        )
    }

    fun changeValue(
        provider: String? = null, playList: Boolean? = null, albums: Boolean? = null,
        appleMusic: Boolean? = null,
        spotify: Boolean? = null,
        anghami: Boolean? = null,
        ytMusic: Boolean? = null,
        deezer: Boolean? = null
    ) {
        _myUiState.update {
            when {
                provider != null -> it.copy(provider = provider)
                playList != null -> it.copy(playList = !playList)
                albums != null -> it.copy(albums = !albums)
                else -> it.copy()
            }
        }
    }

    private fun getData() = viewModelScope.launch(Dispatchers.IO) { // TODO Find Solution to this
        dataStoreManager.getFromDataStore().collect {
            val provider = it[DataStoreProvider.StoredKeys.musicProvider] ?: ""
            val playList = it[DataStoreProvider.StoredKeys.playlistChoice] ?: false
            val album = it[DataStoreProvider.StoredKeys.albumChoice] ?: false
            val appleMusic = it[DataStoreProvider.StoredKeys.appleMusicException] ?: false
            val spotify = it[DataStoreProvider.StoredKeys.spotifyException] ?: false
            val anghami = it[DataStoreProvider.StoredKeys.anghamiException] ?: false
            val ytMusic = it[DataStoreProvider.StoredKeys.ytMusicException] ?: false
            val deezer = it[DataStoreProvider.StoredKeys.deezerException] ?: false

            _myUiState.value = TheScreenUiData(
                provider = provider,
                playList = playList,
                albums = album,
            )
            updatePackage(provider)
        }
    }

    fun handleDeepLink(data: Uri) = viewModelScope.launch(Dispatchers.IO) {
        val link = data.toString()
        if (link.contains(_myUiState.value.provider)) {
            sameApp.postValue(true)
        } else {
            // To ignore deep linking by request of user
            updateMusicExceptions(link)

            when (typeofLink(link)) {
                "playlist" -> {
                    isPlaylist = true
                    isAlbum = false
                }
                "album" -> {
                    isAlbum = true
                    isPlaylist = false
                }
                else -> {
                    isAlbum = false
                    isPlaylist = false
                }
            }

            // Ignores the chosen music provider
            // When the data is got from the datastore this gets updated in relation to the music provider
            if (overrulesPreference) {
                // Open same/original app
                sameApp.postValue(true)
            } else {
                // Check if It should search in the chosen music provider and open it (sameAPP)
                if (isPlaylist && !playlistChoice.value) {
                    sameApp.postValue(true)
                } else if (isAlbum && !albumChoice.value) {
                    sameApp.postValue(true)
                } else {
                    differentApp.postValue(true)
                }
            }
        }
    }

    private fun updatePackage(savedMusicProvider: String) {
        when {
            savedMusicProvider.contains(Constants.APPLE_MUSIC.link) -> {
                musicPackage.value = Constants.APPLE_MUSIC_PACKAGE.link
                searchLink.value = Constants.APPLE_MUSIC_SEARCH.link
            }
            savedMusicProvider.contains(Constants.SPOTIFY.link) -> {
                musicPackage.value = Constants.SPOTIFY_PACKAGE.link
                searchLink.value = Constants.SPOTIFY_SEARCH.link
            }
            savedMusicProvider.contains(Constants.ANGHAMI.link) -> {
                musicPackage.value = Constants.ANGHAMI_PACKAGE.link
                searchLink.value = Constants.ANGHAMI_SEARCH.link
            }
            savedMusicProvider.contains(Constants.YT_MUSIC.link) -> {
                musicPackage.value = Constants.YT_MUSIC_PACKAGE.link
                searchLink.value = Constants.YT_MUSIC_SEARCH.link
            }
            savedMusicProvider.contains(Constants.DEEZER.link) -> {
                musicPackage.value = Constants.DEEZER_PACKAGE.link
                searchLink.value = Constants.DEEZER_SEARCH.link
            }
        }
    }

    private fun updateMusicExceptions(musicProvider: String) {
        when {
            musicProvider.contains(Constants.APPLE_MUSIC.link) -> {
                overrulesPreference = appleMusicChoice.value
            }
            musicProvider.contains(Constants.SPOTIFY.link) -> {
                overrulesPreference = spotifyChoice.value
            }
            musicProvider.contains(Constants.ANGHAMI.link) -> {
                overrulesPreference = anghamiChoice.value
            }
            musicProvider.contains(Constants.YT_MUSIC.link) -> {
                overrulesPreference = ytMusicChoice.value
            }
            musicProvider.contains(Constants.DEEZER.link) -> {
                overrulesPreference = deezerChoice.value
            }
        }
    }

}
