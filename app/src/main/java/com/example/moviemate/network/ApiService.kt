package com.example.moviemate.network

import RecommendationsRequest
import RecommendedMovie
import com.example.moviemate.data.CityResponse
import com.example.moviemate.data.MovieDetailsResponse
import com.example.moviemate.data.MovieResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/recommendations/")
    suspend fun getRecommendations(@Body requestBody: RecommendationsRequest): List<RecommendedMovie>

    @GET("api/movie/soon")
    suspend fun getUpcomingMovies(
        @Header("Authorization") token: String,
        @Query("release_date") releaseDate: String
    ): MovieResponse

    @GET("api/movie/today")
    suspend fun getTodayMovies(
        @Header("Authorization") token: String,
        @Query("city") city: String,
        @Query("start") startDate: String
    ): MovieResponse

    @GET("api/city/{cityId}/objects")
    suspend fun getCityObjects(
        @Path("cityId") cityId: String,
        @Header("Authorization") token: String
    ): CityResponse

    @GET("api/schedule/hall_format")
    suspend fun getMovieDetails(
        @Query("city") cityId: String,
        @Query("movie") movieId: String,
        @Header("Authorization") token: String
    ): MovieDetailsResponse


}