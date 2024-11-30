package com.example.moviemate.data
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class MovieResponse(
    val data: List<Movie>
)

data class MovieDetailsResponse(
    @SerializedName("data") val data: List<Movie>
)
data class Movie(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("genre") val genre: List<String> = emptyList(),
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("directors") val directors: List<String> = emptyList(),
    @SerializedName("actors") val actors: List<String> = emptyList(),
    @SerializedName("image") val image: MovieImage = MovieImage("", ""),
    @SerializedName("trailer") val trailer: Trailer = Trailer(""),
    @SerializedName("certification") val certification: String = "",
    @SerializedName("objects") val objects: List<TheatreInfo> = emptyList()
)

data class MovieImage(
    @SerializedName("vertical") val vertical: String = "",
    @SerializedName("horizontal") val horizontal: String = ""
)

data class Trailer(
    @SerializedName("url") val url: String = ""
)

data class TheatreInfo(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("halls") val halls: Map<String, Hall>? = emptyMap()
)

data class Hall(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("format") val format: List<String>? = emptyList(),
    @SerializedName("seances") val seances: List<Seance> = emptyList()
)

data class Seance(
    @SerializedName("id") val id: String,
    @SerializedName("timeframe") val timeframe: TimeFrame = TimeFrame("", ""),
    @SerializedName("discounts") val discounts: List<Discount>? = emptyList()
)

data class TimeFrame(
    @SerializedName("start") val start: String,
    @SerializedName("end") val end: String
)

@Parcelize
data class Discount(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: Int
):Parcelable
