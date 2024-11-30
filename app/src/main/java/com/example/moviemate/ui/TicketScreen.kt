package com.example.moviemate.ui


import TheatreSeatsRepository
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.moviemate.data.UserTicket
import com.example.moviemate.ui.theme.DarkColorPalette
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TicketScreen() {
    val coroutineScope = rememberCoroutineScope()
    var userTickets by remember { mutableStateOf<List<UserTicket>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val repo = TheatreSeatsRepository()
            userTickets = repo.loadUserTickets()
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.Blue)
        }
    } else {
        Column(modifier = Modifier.padding(10.dp))   {
            Text("Мои билеты", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(bottom = 15.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(userTickets.size) { index ->
                    val ticket = userTickets[index]
                    TicketCard(ticket)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TicketCard(ticket: UserTicket) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { },
        backgroundColor = Color(0xFF2F2F2F),
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(ticket.imageUrl),
                contentDescription = ticket.movieTitle,
                modifier = Modifier
                    .size(width = 130.dp, height = 175.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = ticket.movieTitle, color = Color.White, fontSize = 22.sp, modifier = Modifier.padding(bottom = 10.dp))
                Text(text = "Кинотеатр", color = DarkColorPalette.onSurface, fontSize = 12.sp)
                Text(text = ticket.cinemaName, color = Color.White, modifier = Modifier.padding(bottom = 10.dp))
                Text(text = "Дата", color = DarkColorPalette.onSurface, fontSize = 12.sp)
                Text(text = "${formatDate(ticket.date)}, ${ticket.time}", color = Color.White, modifier = Modifier.padding(bottom = 10.dp))
                Row {
                    Column {
                        Text(text = "Места", color = DarkColorPalette.onSurface, fontSize = 12.sp)
                        Text(text = ticket.seat.joinToString(), color = Color.White)
                    }
                    Spacer(modifier = Modifier.padding(20.dp))
                    Column {
                        Text(text = "Формат", color = DarkColorPalette.onSurface, fontSize = 12.sp)
                        Text(text = ticket.format, color = Color.White)
                    }

                }
            }
        }
    }
}

@SuppressLint("NewApi")
fun formatDate(inputDate: String): String {
    val date = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    return date.format(formatter)
}