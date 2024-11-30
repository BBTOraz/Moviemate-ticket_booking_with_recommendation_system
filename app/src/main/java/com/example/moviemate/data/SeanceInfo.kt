package com.example.moviemate.data
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SeanceInfo(
    val movieId: String,
    val movieName: String,
    val cinemaName: String,
    val format: String,
    val date: String,
    val time: String
) : Parcelable
