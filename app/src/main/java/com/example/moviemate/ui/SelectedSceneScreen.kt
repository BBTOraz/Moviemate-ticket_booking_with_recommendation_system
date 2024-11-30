import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.moviemate.data.Discount
import com.example.moviemate.data.TicketPurchase
import com.example.moviemate.ui.theme.DarkColorPalette
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState", "UnusedMaterial3ScaffoldPaddingParameter", "NewApi")
@Composable
fun SelectedSceneScreen(navController: NavHostController) {
    val ticketPurchase =
        navController.previousBackStackEntry?.savedStateHandle?.get<TicketPurchase>("ticketPurchase")

    if (ticketPurchase == null) {
        Text("Ошибка загрузки данных", color = Color.White)
    }

    val movieName = ticketPurchase?.movieTitle ?: ""
    val image = ticketPurchase?.imageUrl ?: ""
    val genres = ticketPurchase?.genres ?: emptyList()
    val selectedCinema = ticketPurchase?.cinemaName ?: ""
    val selectedDate = ticketPurchase?.date ?: ""
    val selectedTime = ticketPurchase?.time ?: ""
    val discounts = ticketPurchase?.discount ?: emptyList()

    val selectedSeatsWithDiscounts = remember { mutableStateListOf<Pair<String, Discount>>() }
    var seatData by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val selectedSeats = remember { mutableStateListOf<String>() }
    var selectedSeat by remember { mutableStateOf<String?>(null) }
    var isBottomSheetVisible by remember { mutableStateOf(false) }


    LaunchedEffect(ticketPurchase?.theatreId, ticketPurchase?.movieTitle) {
        if (ticketPurchase?.theatreId != null && ticketPurchase.movieTitle.isNotEmpty()) {
            val theatreSeatsRepository = TheatreSeatsRepository()
            seatData = theatreSeatsRepository.loadOrCreateTheatreSeats(
                theatreId = ticketPurchase.theatreId,
                movieId = ticketPurchase.movieTitle
            )["rows"] as? List<Map<String, Any>> ?: emptyList()
        }
    }


    val availableSeatsCount = seatData.sumOf { row ->
        (row["seats"] as? List<Map<String, Any>>)?.count { it["status"] == "available" } ?: 0
    }

    Scaffold(containerColor = DarkColorPalette.background) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Кнопка назад
            item {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = DarkColorPalette.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Информация о фильме и количество свободных мест
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movieName,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkColorPalette.onPrimary
                    )
                    Text(
                        text = "$availableSeatsCount мест свободно",
                        color = DarkColorPalette.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    seatData.forEach { row ->
                        val seats = row["seats"] as? List<Map<String, Any>> ?: emptyList()
                        seats.forEach { seat ->
                            val seatNumber = seat["seatNumber"] as String
                            val status = seat["status"] as String
                            val isSelected = selectedSeats.contains(seatNumber)

                            item {
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isSelected -> DarkColorPalette.primaryVariant
                                                status == "available" -> DarkColorPalette.secondaryVariant
                                                else -> DarkColorPalette.onSecondary
                                            }
                                        )
                                        .clickable {
                                            if (status == "available" && selectedSeat == null) {
                                                if (isSelected) {
                                                    selectedSeats.remove(seatNumber)
                                                } else {
                                                    selectedSeat = seatNumber
                                                    isBottomSheetVisible = true
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = seatNumber,
                                        color = DarkColorPalette.onPrimary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                CurvedLineWithSoftLight()
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem(color = DarkColorPalette.secondaryVariant, label = "Available")
                    LegendItem(color = DarkColorPalette.primaryVariant, label = "Selected")
                    LegendItem(color = DarkColorPalette.onSecondary, label = "Not Available")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Date", color = DarkColorPalette.onPrimary)
                        Text(selectedDate, color = DarkColorPalette.onPrimary)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Seats Selected", color = DarkColorPalette.onPrimary)
                        Text(selectedSeats.joinToString(), color = DarkColorPalette.onPrimary)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Location", color = DarkColorPalette.onPrimary)
                        Text(selectedCinema, color = DarkColorPalette.onPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                val coroutineScope = rememberCoroutineScope()
                val theatreSeatsRepository = TheatreSeatsRepository()
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (userId.isNotEmpty() && ticketPurchase != null) {
                                val newTicketPurchase = TicketPurchase(
                                    movieTitle = ticketPurchase.movieTitle,
                                    imageUrl = ticketPurchase.imageUrl,
                                    genres = ticketPurchase.genres,
                                    cinemaName = ticketPurchase.cinemaName,
                                    seat = selectedSeatsWithDiscounts.map { it.first },
                                    format = ticketPurchase.format,
                                    date = ticketPurchase.date,
                                    time = ticketPurchase.time,
                                    discount = selectedSeatsWithDiscounts.map { it.second },
                                    theatreId = ticketPurchase.theatreId
                                )
                                theatreSeatsRepository.saveTicketPurchase(userId, newTicketPurchase)
                                theatreSeatsRepository.updateSeatsStatus(
                                    ticketPurchase.theatreId,
                                    ticketPurchase.movieTitle,
                                    selectedSeatsWithDiscounts.map { it.first }
                                )
                            }
                        }
                              },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(DarkColorPalette.primary)
                ) {
                    Text("Continue", color = DarkColorPalette.onPrimary)
                }
            }
        }
    }
    if (isBottomSheetVisible && selectedSeat != null) {
        ModalBottomSheet(
            onDismissRequest = {
                isBottomSheetVisible = false
                selectedSeat = null
            },
            containerColor = DarkColorPalette.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Выберите тип билета для $selectedSeat",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkColorPalette.onPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                discounts.distinctBy { it.value }.forEach { discount ->
                    Button(
                        onClick = {
                            selectedSeatsWithDiscounts.add(Pair(selectedSeat!!, discount))
                            selectedSeats.add(selectedSeat!!)
                            println("Выбран билет: ${discount.name}")
                            selectedSeat = null
                            isBottomSheetVisible = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(DarkColorPalette.primary)
                    ) {
                        Text(
                            text = "${discount.name} - ${discount.value}тг",
                            color = DarkColorPalette.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = DarkColorPalette.onPrimary)
    }
}

@Composable
fun CurvedLineWithSoftLight() {
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        val width = size.width
        val height = size.height

        val screenPath = Path().apply {
            moveTo(0f, height * 0.7f)
            quadraticTo(
                width / 2, height * 1.1f,
                width, height * 0.7f
            )
        }

        drawPath(
            path = screenPath,
            color = Color.Blue,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Blue.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                center = androidx.compose.ui.geometry.Offset(width / 2, height * 0.5f),
                radius = width
            ),
            radius = width,
            center = androidx.compose.ui.geometry.Offset(width / 2, height * 0.7f)
        )
    }
}







