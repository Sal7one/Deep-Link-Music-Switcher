package com.sal7one.musicswitcher.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sal7one.musicswitcher.utils.StringConstants
import javax.inject.Inject

class DataStoreProvider @Inject constructor(
    private val dataStore: DataStore<Preferences>
)  {

    object StoredKeys {
        val musicProvider = stringPreferencesKey(StringConstants.MUSIC_PREFERENCES_KEY.link)
        val playlistChoice = booleanPreferencesKey(StringConstants.PLAYLIST_PREFERENCES_KEY.link)
        val albumChoice = booleanPreferencesKey(StringConstants.ALBUM_PREFERENCES_KEY.link)
        val loadingChoice = booleanPreferencesKey(StringConstants.LOADING_PREFERENCES_KEY.link)

        // If Exception to ignore deep linking is needed
        val appleMusicException =
            booleanPreferencesKey(StringConstants.APPLE_M_PREFERENCES_KEY.link)
        val spotifyException = booleanPreferencesKey(StringConstants.SPOTIFY_PREFERENCES_KEY.link)
        val anghamiException = booleanPreferencesKey(StringConstants.ANGHAMI_PREFERENCES_KEY.link)
        val ytMusicException = booleanPreferencesKey(StringConstants.YT_MUSIC_PREFERENCES_KEY.link)
        val deezerException = booleanPreferencesKey(StringConstants.DEEZER_PREFERENCES_KEY.link)
    }

    suspend fun saveExceptions(
        appleMusic: Boolean,
        spotify: Boolean,
        anghami: Boolean,
        ytMusic: Boolean,
        deezer: Boolean,
        userLoadingChoice: Boolean
    ) {
        dataStore.edit { preference ->
            preference[StoredKeys.appleMusicException] = appleMusic
            preference[StoredKeys.spotifyException] = spotify
            preference[StoredKeys.anghamiException] = anghami
            preference[StoredKeys.ytMusicException] = ytMusic
            preference[StoredKeys.deezerException] = deezer
            preference[StoredKeys.loadingChoice] = userLoadingChoice
        }
    }

    suspend fun saveToDataStore(
        userMusicProvider: String,
        userPlaylist: Boolean,
        userAlbum: Boolean,
    ) {
        dataStore.edit { preference ->
            preference[StoredKeys.musicProvider] = userMusicProvider
            preference[StoredKeys.playlistChoice] = userPlaylist
            preference[StoredKeys.albumChoice] = userAlbum
        }
    }

    fun getFromDataStore() = dataStore.data
}