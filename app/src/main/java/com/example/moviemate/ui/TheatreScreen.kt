package com.example.moviemate.ui

import TheatreSeatsRepository
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources.Theme
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.moviemate.R
import com.example.moviemate.data.Theatre
import com.example.moviemate.data.cityMap
import com.example.moviemate.network.RetrofitClient
import com.example.moviemate.ui.theme.DarkColorPalette
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TheatreScreen() {
    val coroutineScope = rememberCoroutineScope()
    var selectedCity by remember { mutableStateOf("Астана") }
    var cities = listOf(
        "Астана", "Актобе", "Алматы", "Атырау", "Уральск", "Шымкент", "Актау", "Караганды", "Туркистан"
    )
    var theatres by remember { mutableStateOf<List<Theatre>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
     
    LaunchedEffect(Unit) {
        val fireStoreRepository = TheatreSeatsRepository()
        val userCity = fireStoreRepository.getUserCityFromFirestore() ?: "Астана"
        selectedCity = userCity
    }

    LaunchedEffect(selectedCity) {
        coroutineScope.launch {
            val cityId = cityMap[selectedCity] ?: "000000000000000000010000"
            try {
                val token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbXBsb3llZV9pZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDEwMSIsImxvZ2luIjoiZnJvbnQucHJvZCIsInJvbGUiOiJmcm9udCIsIm5hbWUiOiJmcm9udC5wcm9kIiwiY3VzdG9tX2NsYWltcyI6eyJjb250cmFjdF9pZCI6IjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAxMCJ9fQ.vt_WV5qYI_b_8_oBUQK-euPE4WnUdranIeS32QLD0KY"
                val response = RetrofitClient.cityApi.getCityObjects(cityId, token)
                theatres = response.data.objects
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCity,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.clickable { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    cities.forEach { city ->
                        DropdownMenuItem(onClick = {
                            selectedCity = city
                            expanded = false
                        }) {
                            Text(text = city)
                        }
                    }
                }
            }
        },
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(theatres) { theatre ->
                    TheatreCard(theatre)
                }
            }
        }
    )
}

@Composable
fun TheatreCard(theatre: Theatre) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* TODO: Open theatre details */ },
        elevation = 8.dp,
        backgroundColor = Color(0xFF2F2F2F),
        contentColor = Color.White
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(theatre.image.vertical),
                contentDescription = theatre.name,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop
            )
            Column {
                Text(
                    text = theatre.name,
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                    fontSize = 20.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable {
                            val uri = Uri.parse("geo:0,0?q=${Uri.encode(theatre.address)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")
                            context.startActivity(intent)
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Icon",
                        tint = Color(0xFF0778C8),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = theatre.address,
                        style = MaterialTheme.typography.body2,
                        color = Color.White
                    )
                }
                theatre.phones.firstOrNull()?.let { phone ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone Icon",
                            tint = Color(0xFF0778C8),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.body2,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
