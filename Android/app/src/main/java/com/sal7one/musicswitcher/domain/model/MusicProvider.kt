package com.sal7one.musicswitcher.domain.model

data class MusicProvider(
    val name: String,
    val nameReference: String,
    val searchLink: String,
    val packageReference: String,
    var overrulesPreference: Boolean,
    val icon: Int,
)