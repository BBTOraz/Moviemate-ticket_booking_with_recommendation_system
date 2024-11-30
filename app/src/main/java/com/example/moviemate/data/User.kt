package com.example.moviemate.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


data class User(
    val name: String = "",
    val city: String = "",
    val email: String = "",
    val ticketPurchases: List<TicketPurchase> = emptyList()
)

@Parcelize
data class TicketPurchase(
    val movieTitle: String = "",
    val imageUrl: String = "",
    val genres: List<String> = emptyList(),
    val cinemaName: String = "",
    //val hall: String = "",
    val seat: List<String> = emptyList(),
    val format: String = "",
    val date: String = "",
    val time: String = "",
    val discount: List<Discount> = emptyList(),
    val theatreId: String = ""
) : Parcelable

data class UserTicket(
    val movieTitle: String = "",
    val imageUrl: String = "",
    val cinemaName: String = "",
    val date: String = "",
    val time: String = "",
    val seat: List<String> = emptyList(),
    val format: String = ""
)
