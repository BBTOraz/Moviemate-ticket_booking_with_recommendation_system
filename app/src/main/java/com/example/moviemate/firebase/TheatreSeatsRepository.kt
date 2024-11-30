import android.util.Log
import com.example.moviemate.data.Discount
import com.example.moviemate.data.TicketPurchase
import com.example.moviemate.data.User
import com.example.moviemate.data.UserTicket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TheatreSeatsRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun loadOrCreateTheatreSeats(theatreId: String, movieId: String): Map<String, Any> {
        val theatreSeatsDoc = db.collection("theatreSeats")
            .document(theatreId)
            .collection("movies")
            .document(movieId)

        val document = theatreSeatsDoc.get().await()
        Log.i("loadOrCreate firestore method", "Метод создания или загрузки мест вызван для $theatreId и $movieId")

        if (document.exists()) {
            return document.data ?: emptyMap()
        } else {
            val newSeats = createNewTheatreSeats()
            theatreSeatsDoc.set(newSeats).await()
            return newSeats
        }
    }

    suspend fun loadUserTickets(): List<UserTicket> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val db = FirebaseFirestore.getInstance()
        val userTicketsDoc = db.collection("users").document(userId)
            .get()
            .await()

        val tickets = userTicketsDoc.data?.get("ticket_purchases") as? List<Map<String, Any>>
        return tickets?.map { ticket ->
            UserTicket(
                movieTitle = ticket["movieTitle"] as? String ?: "",
                imageUrl = ticket["imageUrl"] as? String ?: "",
                cinemaName = ticket["cinemaName"] as? String ?: "",
                date = ticket["date"] as? String ?: "",
                time = ticket["time"] as? String ?: "",
                seat = ticket["seat"] as? List<String> ?: emptyList(),
                format = ticket["format"] as? String ?: ""
            )
        } ?: emptyList()
    }

    suspend fun getUserCityFromFirestore(): String? {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("users").document(userId).get().await()
        return userDoc.getString("city")
    }

    private fun createNewTheatreSeats(): Map<String, Any> {
        val rows = mutableListOf<Map<String, Any>>()

        ('A'..'I').forEach { row ->
            val seats = (1..12).map { seatNumber ->
                mapOf("seatNumber" to "$row$seatNumber", "status" to "available")
            }
            rows.add(mapOf("row" to row.toString(), "seats" to seats))
        }

        return mapOf("rows" to rows)
    }

    suspend fun loadUserTicketPurchases(): List<TicketPurchase> {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return emptyList()
        val db = FirebaseFirestore.getInstance()
        val document = db.collection("users").document(currentUser.uid).get().await()

        // Проверка на наличие документа
        if (document.exists()) {
            // Преобразуем `ticket_purchases` из документа в список `TicketPurchase`
            val ticketPurchases = document["ticket_purchases"] as? List<Map<String, Any>> ?: emptyList()

            // Преобразуем список Map в список объектов `TicketPurchase`
            return ticketPurchases.map { map ->
                TicketPurchase(
                    movieTitle = map["movieTitle"] as? String ?: "",
                    imageUrl = map["imageUrl"] as? String ?: "",
                    genres = map["genres"] as? List<String> ?: emptyList(),
                    cinemaName = map["cinemaName"] as? String ?: "",
                    seat = map["seat"] as? List<String> ?: emptyList(),
                    format = map["format"] as? String ?: "",
                    date = map["date"] as? String ?: "",
                    time = map["time"] as? String ?: "",
                    discount = (map["discount"] as? List<Map<String, Any>>)?.map { discountMap ->
                        Discount(
                            id = discountMap["id"] as? String ?: "",
                            name = discountMap["name"] as? String ?: "",
                            value = (discountMap["value"] as? Number)?.toInt() ?: 0
                        )
                    } ?: emptyList(),
                    theatreId = map["theatreId"] as? String ?: ""
                )
            }
        }
        return emptyList()
    }


    suspend fun updateSeatsStatus(theatreId: String, movieId: String, selectedSeats: List<String>) {
        val theatreSeatsDoc = db.collection("theatreSeats")
            .document(theatreId)
            .collection("movies")
            .document(movieId)

        val document = theatreSeatsDoc.get().await()

        if (document.exists()) {
            val rows = document.data?.get("rows") as? MutableList<Map<String, Any>>
            rows?.forEachIndexed { rowIndex, row ->
                val seats = row["seats"] as? MutableList<Map<String, Any>>
                seats?.forEachIndexed { seatIndex, seat ->
                    val seatNumber = seat["seatNumber"] as String
                    if (seatNumber in selectedSeats) {
                        // Преобразуем `seat` в изменяемую копию
                        val updatedSeat = seat.toMutableMap()
                        updatedSeat["status"] = "not available"

                        // Обновляем элемент в списке `seats`
                        seats[seatIndex] = updatedSeat
                    }
                }
            }
            // Обновляем документ в Firestore с новыми данными
            theatreSeatsDoc.update("rows", rows).await()
        } else {
            Log.e("Firestore", "Document not found for movie: $movieId in theatre: $theatreId")
        }
    }



    suspend fun saveTicketPurchase(userId: String, ticketPurchase: TicketPurchase) {
        val userDoc = db.collection("users").document(userId)
        userDoc.update("ticket_purchases", FieldValue.arrayUnion(ticketPurchase)).await()
    }
}

