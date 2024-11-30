package com.example.moviemate.data

import com.google.gson.annotations.SerializedName

data class Theatre(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("sort_order") val sortOrder: Int,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("address") val address: String,
    @SerializedName("phones") val phones: List<String>,
    @SerializedName("features") val features: List<String>,
    @SerializedName("image") val image: TheatreImage
)

data class TheatreImage(
    @SerializedName("vertical") val vertical: String,
    @SerializedName("horizontal") val horizontal: String
)

data class CityResponse(
    @SerializedName("data") val data: CityData
)

data class CityData(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("objects") val objects: List<Theatre>
)