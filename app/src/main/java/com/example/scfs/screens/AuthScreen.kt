package com.example.scfs.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scfs.R
import com.example.scfs.data.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onLoggedIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_paw),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Login", fontFamily = Hanuman, fontSize = 34.sp)

            Spacer(Modifier.height(24.dp))

            SCFSTextField(
                label = "Email",
                value = email,
                placeholder = "email@example.com",
                onValueChange = { email = it }
            )

            Spacer(Modifier.height(14.dp))

            SCFSPasswordField(
                label = "Password",
                value = password,
                placeholder = "Password",
                onValueChange = { password = it }
            )

            Spacer(Modifier.height(24.dp))

            SCFSButton(
                text = "Login",
                onClick = {
                    scope.launch {
                        val cleanEmail = email.trim().lowercase()
                        val cleanPassword = password.trim()

                        if (!isValidEmail(cleanEmail)) {
                            message = "Please enter a valid email address."
                            return@launch
                        }

                        if (cleanPassword.isBlank()) {
                            message = "Please enter your password."
                            return@launch
                        }

                        try {
                            SupabaseManager.client.auth.signOut()

                            SupabaseManager.client.auth.signInWith(Email) {
                                this.email = cleanEmail
                                this.password = cleanPassword
                            }

                            val user = SupabaseManager.client.auth.currentUserOrNull()

                            if (user != null && user.email == cleanEmail) {
                                message = ""
                                onLoggedIn()
                            } else {
                                SupabaseManager.client.auth.signOut()
                                message = "Wrong email or password."
                            }

                        } catch (e: Exception) {
                            SupabaseManager.client.auth.signOut()
                            message = friendlyAuthError(e.message)
                        }
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            SCFSButton(
                text = "Register",
                onClick = {
                    scope.launch {
                        val cleanEmail = email.trim().lowercase()
                        val cleanPassword = password.trim()

                        if (!isValidEmail(cleanEmail)) {
                            message = "Please enter a valid email address."
                            return@launch
                        }

                        if (cleanPassword.length < 6) {
                            message = "Password must be at least 6 characters."
                            return@launch
                        }

                        try {
                            SupabaseManager.client.auth.signOut()

                            SupabaseManager.client.auth.signUpWith(Email) {
                                this.email = cleanEmail
                                this.password = cleanPassword
                            }

                            val user = SupabaseManager.client.auth.currentUserOrNull()

                            if (user == null) {
                                message = "Account created. Please log in."
                                return@launch
                            }

                            SupabaseManager.client
                                .from("profiles")
                                .upsert(
                                    mapOf(
                                        "id" to user.id,
                                        "email" to cleanEmail,
                                        "display_name" to cleanEmail.substringBefore("@")
                                    )
                                )

                            message = ""
                            onLoggedIn()

                        } catch (e: Exception) {
                            message = "Register error: ${e.message}"
                            e.printStackTrace()
                        }
                    }
                }
            )

            if (message.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = message,
                    color = Color(0xFF8A3A3A),
                    fontFamily = Harmattan,
                    fontSize = 18.sp
                )
            }
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return email.contains("@") &&
            email.contains(".") &&
            !email.contains(" ") &&
            email.length >= 6
}

fun friendlyAuthError(message: String?): String {
    val msg = message?.lowercase() ?: ""

    return when {
        "already registered" in msg || "already exists" in msg || "user already" in msg ->
            "This email is already registered. Please log in."

        "invalid login" in msg || "invalid credentials" in msg ->
            "Wrong email or password."

        "password" in msg ->
            "Password must be at least 6 characters."

        "email" in msg ->
            "Please enter a valid email address."

        else ->
            "Something went wrong. Please try again."
    }
}