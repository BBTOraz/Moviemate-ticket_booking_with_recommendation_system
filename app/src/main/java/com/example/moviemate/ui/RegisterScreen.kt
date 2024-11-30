package com.example.moviemate.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.moviemate.R
import com.example.moviemate.ui.theme.Gray
import com.example.moviemate.ui.theme.White
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showEmailVerificationDialog by remember { mutableStateOf(false) }
    val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(name)
        .build()

    val cities = listOf(
        "Астана",
        "Актобе",
        "Алматы",
        "Атырау",
        "Уральск",
        "Шымкент",
        "Актау",
        "Караганды",
        "Туркистан"
    )
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Логотип
                Image(
                    painter = painterResource(id = R.drawable.moviemate),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(300.dp),
                    colorFilter = ColorFilter.colorMatrix(
                        ColorMatrix().apply { setToSaturation(0f) }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя") },
                    placeholder = { Text("Введите ваше имя") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Name Icon"
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = White,
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = Gray,
                        leadingIconColor = MaterialTheme.colors.primary,
                        placeholderColor = Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле City (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { },
                        label = { Text("Город") },
                        placeholder = { Text("Выберите ваш город") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = "City Icon"
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        readOnly = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = White,
                            focusedBorderColor = MaterialTheme.colors.primary,
                            unfocusedBorderColor = Gray,
                            leadingIconColor = MaterialTheme.colors.primary,
                            trailingIconColor = MaterialTheme.colors.primary,
                            placeholderColor = Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        cities.forEach { selectedCity ->
                            DropdownMenuItem(
                                onClick = {
                                    city = selectedCity
                                    expanded = false
                                }
                            ) {
                                Text(text = selectedCity)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Поле Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("Введите ваш email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = "Email Icon"
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = White,
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = Gray,
                        leadingIconColor = MaterialTheme.colors.primary,
                        placeholderColor = Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    placeholder = { Text("Введите ваш пароль") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon"
                        )
                    },
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Default.Visibility
                        else Icons.Default.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle Password Visibility")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = White,
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = Gray,
                        leadingIconColor = MaterialTheme.colors.primary,
                        trailingIconColor = MaterialTheme.colors.primary,
                        placeholderColor = Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Подтверждение пароля") },
                    placeholder = { Text("Введите пароль еще раз") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirm Password Icon"
                        )
                    },
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Default.Visibility
                        else Icons.Default.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle Password Visibility")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = White,
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = Gray,
                        leadingIconColor = MaterialTheme.colors.primary,
                        trailingIconColor = MaterialTheme.colors.primary,
                        placeholderColor = Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Валидация полей
                        if (email.isBlank() || password.isBlank() || name.isBlank() || city.isBlank() || confirmPassword.isBlank()) {
                            error = "Все поля должны быть заполнены"
                            return@Button
                        }

                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            error = "Введите корректный email"
                            return@Button
                        }

                        if (password != confirmPassword) {
                            error = "Пароли не совпадают"
                            return@Button
                        }

                        if (password.length < 6) {
                            error = "Пароль должен содержать не менее 6 символов"
                            return@Button
                        }

                        isLoading = true
                        error = ""
                        println("Начало регистрации пользователя")

                        auth.createUserWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { task ->
                                println("createUserWithEmailAndPassword завершен")
                                if (task.isSuccessful) {
                                    println("Пользователь успешно создан" + auth.currentUser)
                                    val user = auth.currentUser
                                    val userId = user?.uid ?: return@addOnCompleteListener

                                    user.sendEmailVerification()
                                        .addOnCompleteListener { verificationTask ->
                                            if (verificationTask.isSuccessful) {
                                                print("Пользователю отправили сообщение")
                                                user.updateProfile(profileUpdates)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            println("Профиль успешно обновлен!")
                                                        } else {
                                                            println("Ошибка при обновлении профиля: ${task.exception}")
                                                        }
                                                    }


                                                val dbUser = hashMapOf(
                                                    "name" to name,
                                                    "city" to city,
                                                    "email" to email,
                                                    "ticket_purchases" to emptyList<Map<String, Any>>()
                                                )
                                                print("Сохранение пользователя в бд")
                                                db.collection("users").document(userId).set(dbUser)
                                                    .addOnSuccessListener {
                                                        isLoading = false
                                                        println("Пользователь успешно создан")
                                                        showEmailVerificationDialog = true
                                                    }
                                                    .addOnFailureListener { e ->
                                                        isLoading = false
                                                        error = "Не удалось создать пользователя в Firestore: ${e.message}"
                                                    }
                                            } else {
                                                isLoading = false
                                                error = verificationTask.exception?.message ?: "Ошибка при отправке подтверждения email"
                                            }
                                        }
                                } else {
                                    isLoading = false
                                    error = task.exception?.message ?: "Ошибка регистрации"
                                }
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(text = "Зарегистрироваться", color = White, style = MaterialTheme.typography.button)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    navController.navigate("login")
                }) {
                    Text("Уже есть аккаунт? Войти", color = MaterialTheme.colors.onBackground)
                }

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, color = MaterialTheme.colors.error)
                }
            }

            if (showEmailVerificationDialog) {
                print("Диалоговое окно - уведомление")
                EmailVerificationDialog(
                    email = email,
                    auth = auth,
                    onVerified = {
                        println("Перенравление пользователя")
                        showEmailVerificationDialog = false
                        navController.navigate("login") {
                            println("Перенравели пользователя")
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onResendEmail = {
                        println("Переотправить сообщение")
                        auth.currentUser?.sendEmailVerification()
                    },
                    onCancel = {
                        println("onCancel")
                        showEmailVerificationDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun EmailVerificationDialog(
    email: String,
    auth: FirebaseAuth,
    onVerified: () -> Unit,
    onResendEmail: () -> Unit,
    onCancel: () -> Unit
) {
    println("Начало функции")

    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(

        onDismissRequest = { /* Блокируем закрытие диалога по клику вне */ },
        title = { Text(text = "Подтвердите ваш email") },
        text = {
            Column {
                Text(text = "Письмо с подтверждением было отправлено на $email. Пожалуйста, проверьте вашу почту и подтвердите email для входа в приложение.")
                if (isChecking) {
                    println("Окно")
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator()
                }
                if (errorMessage.isNotEmpty()) {
                    println("нет окна")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = MaterialTheme.colors.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    println("нажал кнопку")
                    isChecking = true
                    errorMessage = ""
                    auth.currentUser?.reload()?.addOnCompleteListener { task ->
                        isChecking = false
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null && user.isEmailVerified) {
                                println("Подтвердил")
                                onVerified()
                            } else {
                                errorMessage = "Email еще не подтвержден. Пожалуйста, проверьте почту."
                            }
                        } else {
                            errorMessage = task.exception?.message ?: "Ошибка при проверке статуса верификации"
                        }
                    }
                }
            ) {
                Text("Я подтвердил email")
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = onResendEmail) {
                    Text("Отправить письмо снова")
                }
                TextButton(onClick = onCancel) {
                    Text("Отмена")
                }
            }
        }
    )
}
