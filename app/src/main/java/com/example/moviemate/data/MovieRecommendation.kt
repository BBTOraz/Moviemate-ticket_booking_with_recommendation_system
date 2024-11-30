import com.google.gson.annotations.SerializedName

data class RecommendationsRequest(
    val user_history: List<String>,
    val city_id: String,
    val top_n: Int
)

data class RecommendedMovie(
    val id: String,
    val name: String,
    val trailer: String,
    val imageVertical: String,
    val imageHorizontal: String,
    val similarity: Double
)
