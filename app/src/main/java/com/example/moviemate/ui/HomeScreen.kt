package com.example.moviemate.ui

import RecommendationsRequest
import TheatreSeatsRepository
import android.annotation.SuppressLint
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.moviemate.R
import com.example.moviemate.data.Movie
import com.example.moviemate.data.RecommendationsCache
import com.example.moviemate.data.cityMap
import com.example.moviemate.network.RetrofitClient
import com.example.moviemate.ui.theme.DarkColorPalette
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var movies by rememberSaveable { mutableStateOf<List<Movie>>(emptyList()) }
    var isDataLoaded by rememberSaveable { mutableStateOf(false) }

    val backgroundColor = DarkColorPalette.background
    val selectedButtonColor = DarkColorPalette.primary
    val selectedTextColor = DarkColorPalette.onPrimary
    val defaultButtonColor = DarkColorPalette.primaryVariant
    val defaultTextColor = DarkColorPalette.onSurface

    val currentDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var city by rememberSaveable { mutableStateOf<String?>(null) }
    var isCityLoaded by rememberSaveable { mutableStateOf(false) }


    var selectedTab by remember { mutableIntStateOf(0) }
    var userHistory by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var upcomingMovies by rememberSaveable { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isHistoryExists by remember { mutableStateOf(false) }

    val drawable = remember {
        if (SDK_INT >= Build.VERSION_CODES.P) {
            val source =
                ImageDecoder.createSource(context.resources, R.drawable.icons8_sad_unscreen_colored)
            ImageDecoder.decodeDrawable(source)
        } else {
            null
        }
    }


    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        city = document.getString("city")
                        if (city != null) {
                            Log.d("FirebaseCheck", "City field found: $city")
                        } else {
                            Log.d("FirebaseCheck", "City field not found.")
                            city = "Астана"
                        }
                    } else {
                        Log.d("FirebaseCheck", "User document does not exist.")
                        city = "Астана"
                    }
                    isCityLoaded = true
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseCheck", "Error checking document: ", e)
                    city = "Астана"
                    isCityLoaded = true
                }
        } ?: run {
            city = "Астана"
            isCityLoaded = true
        }
    }

    LaunchedEffect(isCityLoaded, city) {
        if (isCityLoaded && city != null && !isDataLoaded) {
            coroutineScope.launch {
                try {
                    isLoading = true
                    val token =
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbXBsb3llZV9pZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDEwMSIsImxvZ2luIjoiZnJvbnQucHJvZCIsInJvbGUiOiJmcm9udCIsIm5hbWUiOiJmcm9udC5wcm9kIiwiY3VzdG9tX2NsYWltcyI6eyJjb250cmFjdF9pZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAxMCJ9fQ.vt_WV5qYI_b_8_oBUQK-euPE4WnUdranIeS32QLD0KY"
                    val cityCode = cityMap[city] ?: ""
                    if(movies.isEmpty()) {
                        movies = RetrofitClient.moviesApi.getTodayMovies(
                            token,
                            city = cityCode,
                            startDate = currentDate
                        ).data
                    }
                    isDataLoaded = true
                    isLoading = false
                    //Log.i("Текущая дата: ", currentDate)
                    if (RecommendationsCache.recommendations == null) {
                        Log.i("Start", "check recommendation system")
                        val firestoreRepo = TheatreSeatsRepository()
                        val histories = firestoreRepo.loadUserTicketPurchases()
                        val currentYear = LocalDate.now().year

                        val userHistories = histories.mapNotNull { purchase ->
                            purchase.movieTitle.let { "$it ($currentYear)" }
                        }
                        Log.i("Histories", userHistories.toString())

                        if (userHistories.isNotEmpty()) {
                            isHistoryExists = true
                            val requestBody = RecommendationsRequest(
                                user_history = userHistories,
                                city_id = cityCode,
                                top_n = 5
                            )
                            val recommendations = RetrofitClient.api.getRecommendations(requestBody)
                            RecommendationsCache.recommendations = recommendations
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 2 && upcomingMovies.isEmpty()) {
            coroutineScope.launch {
                try {
                    val token =
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbXBsb3llZV9pZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDEwMSIsImxvZ2luIjoiZnJvbnQucHJvZCIsInJvbGUiOiJmcm9udCIsIm5hbWUiOiJmcm9udC5wcm9kIiwiY3VzdG9tX2NsYWltcyI6eyJjb250cmFjdF9pZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAxMCJ9fQ.vt_WV5qYI_b_8_oBUQK-euPE4WnUdranIeS32QLD0KY"
                    val response = RetrofitClient.moviesApi.getUpcomingMovies(
                        token,
                        releaseDate = currentDate
                    )
                    upcomingMovies = response.data
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    var showTrailer by remember { mutableStateOf(false) }
    var selectedMovieTrailerUrl by remember { mutableStateOf<String?>(null) }

    if (isLoading) {
        LoadingAnimation()
    } else {
        if (showTrailer && selectedMovieTrailerUrl != null) {
            TrailerPlayer(
                videoUrl = selectedMovieTrailerUrl!!,
                onClose = { showTrailer = false }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
            ) {
                if (movies.isNotEmpty()) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val currentIndex by infiniteTransition.animateFloat(
                        label = "FloatAnimation",
                        initialValue = 0f,
                        targetValue = movies.size.toFloat(),
                        animationSpec = infiniteRepeatable(
                            animation = tween(78000, easing = LinearEasing)
                        )
                    )
                    val movieIndex = currentIndex.toInt() % movies.size

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        val movie = movies[movieIndex]
                        Image(
                            painter = rememberAsyncImagePainter(movie.image.vertical),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .clickable { navController.navigate("movie_details/${movie.id}") },
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .background(Color.Black.copy(alpha = 0.4f))
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = movie.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        selectedMovieTrailerUrl = movie.trailer.url
                                        showTrailer = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red.copy(alpha = 0.8f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icons8_youtube_100),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Смотреть трейлер",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val buttons = listOf("Сегодня в кино", "Рекомендуемые", "Скоро в кино")
                    buttons.forEachIndexed { index, label ->
                        Button(
                            onClick = {
                                selectedTab = index
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (index == 0) selectedButtonColor else defaultButtonColor,
                                contentColor = if (index == 0) selectedTextColor else defaultTextColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(text = label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(movies.size) { index ->
                                val movie = movies[index]
                                Image(
                                    painter = rememberAsyncImagePainter(movie.image.vertical),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(width = 200.dp, height = 300.dp)
                                        .clickable { navController.navigate("movie_details/${movie.id}") }
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black, shape = RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    1 -> {
                        val recommendations = RecommendationsCache.recommendations
                        if (recommendations != null) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recommendations.size) { index ->
                                    val movie = recommendations[index]
                                    Image(
                                        painter = rememberAsyncImagePainter(movie.imageVertical),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(width = 200.dp, height = 300.dp)
                                            .clickable { navController.navigate("movie_details/${movie.id}") }
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Color.Black,
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        } else {
                            if (drawable is AnimatedImageDrawable) {
                                drawable.start()
                            }
                            if (isHistoryExists)
                                Text("Загрузка рекомендаций...", color = Color.White)
                            else {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row {
                                        Text(
                                            text = "Нет рекомендации для вас",
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box {
                                            drawable?.let {
                                                Image(
                                                    bitmap = it.toBitmap().asImageBitmap(),
                                                    contentDescription = "GIF Image",
                                                    modifier = Modifier.size(45.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(upcomingMovies.size) { index ->
                                val movie = upcomingMovies[index]
                                Image(
                                    painter = rememberAsyncImagePainter(movie.image.vertical),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(width = 200.dp, height = 300.dp)
                                        .clickable { navController.navigate("movie_details/${movie.id}") }
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black, shape = RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrailerPlayer(videoUrl: String, onClose: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val youtubeVideoId = extractYoutubeVideoId(videoUrl)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        if (youtubeVideoId != null) {
            AndroidView(
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        lifecycleOwner.lifecycle.addObserver(this)
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.loadVideo(youtubeVideoId, 0f)
                            }
                        })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.Center)
            )
        } else {
            Text(
                text = "Не удалось загрузить трейлер",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Закрыть",
                tint = Color.White
            )
        }
    }
}


fun extractYoutubeVideoId(ytUrl: String): String? {
    val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu\\.be\\/|\\/v\\/)[^#\\&\\?\\n]*"
    val compiledPattern = Pattern.compile(pattern)
    val matcher = compiledPattern.matcher(ytUrl)
    return if (matcher.find()) {
        matcher.group()
    } else {
        null
    }
}
