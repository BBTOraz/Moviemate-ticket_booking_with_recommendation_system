package com.example.moviemate.ui

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.moviemate.data.Movie
import com.example.moviemate.data.TicketPurchase
import com.example.moviemate.data.cityMap
import com.example.moviemate.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


@SuppressLint("NewApi")
@Composable
fun MovieDetailsScreen(movieId: String, navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    var movie by remember { mutableStateOf<Movie?>(null) }
    var expandedDescription by remember { mutableStateOf(false) }
    var selectedCinema by remember { mutableStateOf<String?>(null) }
    var selectedFormat by remember { mutableStateOf<String?>(null) }
    var availableDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var availableTimes by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    val currentDateTime = LocalDateTime.now()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var city by remember { mutableStateOf<String?>(null) }
    var isCityLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        city = document.getString("city")
                        isCityLoaded = true
                    } else {
                        city = "Астана"
                        isCityLoaded = true
                    }
                }
                .addOnFailureListener { e ->
                    city = "Астана"
                    isCityLoaded = true
                }
        } ?: run {
            city = "Астана"
            isCityLoaded = true
        }
    }

    LaunchedEffect(movieId, isCityLoaded) {
        if (isCityLoaded && city != null) {
            coroutineScope.launch {
                try {
                    val token =
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbXBsb3llZV9pZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDEwMSIsImxvZ2luIjoiZnJvbnQucHJvZCIsInJvbGUiOiJmcm9udCIsIm5hbWUiOiJmcm9udC5wcm9kIiwiY3VzdG9tX2NsYWltcyI6eyJjb250cmFjdF9pZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAxMCJ9fQ.vt_WV5qYI_b_8_oBUQK-euPE4WnUdranIeS32QLD0KY"
                    val cityCode = cityMap[city] ?: "000000000000000000010000"

                    val response = RetrofitClient.moviesApi.getMovieDetails(
                        cityId = cityCode,
                        movieId = movieId,
                        token = token
                    )
                    if (response.data.isNotEmpty()) {
                        movie = response.data[0]
                    }

                    movie?.let {
                        if (selectedCinema == null) {
                            selectedCinema = it.objects.firstOrNull()?.name
                        }
                        if (selectedFormat == null) {
                            val cinema = it.objects.firstOrNull { obj -> obj.name == selectedCinema }
                            selectedFormat = cinema?.halls?.keys?.firstOrNull()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(movie, selectedCinema, selectedFormat) {
        if (movie != null && selectedCinema != null && selectedFormat != null) {
            val cinema = movie!!.objects.firstOrNull { it.name == selectedCinema }
            val hall = cinema?.halls?.get(selectedFormat)
            availableDates = hall?.seances?.map { seance ->
                seance.timeframe.start.substring(0, 10)
            }?.distinct() ?: emptyList()

            selectedDate = null
            selectedTime = null
        }
    }

    LaunchedEffect(selectedDate) {
        if (movie != null && selectedCinema != null && selectedFormat != null && selectedDate != null) {
            val cinema = movie!!.objects.firstOrNull { it.name == selectedCinema }
            val hall = cinema?.halls?.get(selectedFormat)
            availableTimes = hall?.seances?.filter { seance ->
                seance.timeframe.start.substring(0, 10) == selectedDate
            }?.map { seance ->
                seance.timeframe.start.substring(11, 16)
            } ?: emptyList()

            selectedTime = null
        }
    }

    movie?.let { movie ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                    val cinemaNames = movie.objects.map { it.name }
                    CinemaDropdown(
                        cinemas = cinemaNames,
                        selectedCinema = selectedCinema,
                        onCinemaSelected = { cinema ->
                            selectedCinema = cinema
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = movie.name,
                    color = MaterialTheme.colors.onBackground,
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${movie.duration / 60} ч ${movie.duration % 60} мин",
                        color = MaterialTheme.colors.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(movie.image.vertical),
                        contentDescription = null,
                        modifier = Modifier
                            .size(width = 150.dp, height = 220.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    val dimmedTextColor = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Жанры: ${movie.genre.joinToString(", ")}",
                            color = dimmedTextColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Режиссер: ${movie.directors.joinToString(", ")}",
                            color = dimmedTextColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "В ролях: ${movie.actors.joinToString(", ")}",
                            color = dimmedTextColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (expandedDescription) "Скрыть описание" else "Показать описание",
                            modifier = Modifier.clickable {
                                expandedDescription = !expandedDescription
                            },
                            color = MaterialTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (expandedDescription) {
                            Text(
                                text = movie.description,
                                color = MaterialTheme.colors.onBackground,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val formats = movie.objects
                    .firstOrNull { it.name == selectedCinema }
                    ?.halls
                    ?.keys
                    ?.toList() ?: emptyList()

                if (formats.isNotEmpty()) {
                    Text(
                        text = "Выберите формат",
                        color = MaterialTheme.colors.onBackground,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        formats.forEach { format ->
                            Button(
                                onClick = { selectedFormat = format },
                                colors = if (selectedFormat == format) ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                ) else ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
                                modifier = Modifier
                                    .weight(1f),
                                    //.height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(
                                    text = format,
                                    color = if (selectedFormat == format) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                                )
                            }
                        }
                    }
                }

                if (availableDates.isNotEmpty()) {
                    Text(
                        text = "Выберите дату",
                        color = MaterialTheme.colors.onBackground,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableDates.size) { index ->
                            val date = availableDates[index]
                            val isSelected = selectedDate == date
                            val localDate = LocalDate.parse(date)
                            val dayOfWeek = localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
                            Column(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { selectedDate = date }
                                    .background(
                                        color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayOfWeek,
                                    color = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = date.substring(8, 10),
                                    color = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                                )
                            }
                        }
                    }
                }

                if (availableTimes.isNotEmpty()) {
                    Text(
                        text = "Выберите время",
                        color = MaterialTheme.colors.onBackground,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableTimes.size) { index ->
                            val time = availableTimes[index]
                            val isSelected = selectedTime == time

                            if (selectedDate != null) {
                                val sessionDateTime = LocalDateTime.parse(
                                    "$selectedDate $time",
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                )
                                val isPast = sessionDateTime.isBefore(currentDateTime)
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clickable(enabled = !isPast) { selectedTime = time }
                                        .background(
                                            color = when {
                                                isPast -> MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                                isSelected -> MaterialTheme.colors.primary
                                                else -> MaterialTheme.colors.surface
                                            },
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = time,
                                        color = when {
                                            isPast -> MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                            isSelected -> MaterialTheme.colors.onPrimary
                                            else -> MaterialTheme.colors.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val cinema = movie.objects.firstOrNull { it.name == selectedCinema }
                        val hall = cinema?.halls?.get(selectedFormat)
                        val selectedDiscounts = hall?.seances
                            ?.filter { seance ->
                                seance.timeframe.start.substring(11, 16) == selectedTime
                            }?.flatMap { seance ->
                                seance.discounts ?: emptyList() } ?: emptyList()
                        val ticketPurchase = TicketPurchase(
                            movieTitle = movie.name,
                            theatreId = cinema!!.id,
                            imageUrl = movie.image.vertical,
                            genres = movie.genre,
                            date = selectedDate ?: "",
                            time = selectedTime ?: "",
                            cinemaName = selectedCinema ?: "",
                            format = selectedFormat ?: "",
                            discount = selectedDiscounts
                        )
                        Log.i("MovieDetailsScreen - ticket", ticketPurchase.toString())
                        navController.navigate("selectSeatsScreen") {
                            navController.currentBackStackEntry?.savedStateHandle?.set("ticketPurchase", ticketPurchase)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    ),
                    shape = RoundedCornerShape(30.dp),
                    enabled = selectedDate != null && selectedTime != null
                ) {
                    Text("Выбрать места", color = MaterialTheme.colors.onPrimary)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CinemaDropdown(
    cinemas: List<String>,
    selectedCinema: String?,
    onCinemaSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()
    val textWidth = selectedCinema?.let {
        textMeasurer.measure(AnnotatedString(it)).size.width.dp
    } ?: 0.dp
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCinema ?: "",
            onValueChange = {},
            label = { Text("Кинотеатр") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.widthIn(textWidth),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colors.onPrimary
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            cinemas.forEach { cinema ->
                DropdownMenuItem(onClick = {
                    onCinemaSelected(cinema)
                    expanded = false
                }) {
                    Text(text = cinema)
                }
            }
        }
    }
}

