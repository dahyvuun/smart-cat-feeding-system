package com.example.scfs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scfs.screens.AddCatScreen
import com.example.scfs.screens.AddMachineScreen
import com.example.scfs.screens.AuthScreen
import com.example.scfs.screens.HomeScreen
import com.example.scfs.screens.MachineConnectedScreen
import com.example.scfs.screens.TakePhotosScreen
import com.example.scfs.screens.WelcomeScreen
import com.example.scfs.ui.theme.SCFSTheme
import com.example.scfs.data.SetupRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SCFSTheme {
                SCFSApp()
            }
        }
    }
}

@Composable
fun SCFSApp() {
    var currentCatId by remember { mutableStateOf<String?>(null) }
    var currentScreen by remember { mutableStateOf("auth") }
    val scope = rememberCoroutineScope()
    fun goAfterLogin() {
        scope.launch {
            try {
                currentScreen = SetupRepository.getNextScreen()
            } catch (e: Exception) {
                e.printStackTrace()
                currentScreen = "machine"
            }
        }
    }

    when (currentScreen) {
        "auth" ->
            AuthScreen(
                onLoggedIn = {
                    goAfterLogin()
                }
            )

        "welcome" ->
            WelcomeScreen(
                onStart = {
                    goAfterLogin()
                }
            )

        "machine" ->
            AddMachineScreen(
                onNext = {
                    currentScreen = "cat"
                }
            )

        "connected" ->
            MachineConnectedScreen(
                onContinue = {
                    currentScreen = "cat"
                }
            )

        "cat" ->
            AddCatScreen(
                onNext = { catId ->
                    currentCatId = catId
                    currentScreen = "photos"
                }
            )

        "photos" ->
            TakePhotosScreen(
                catId = currentCatId ?: "",
                onFinish = {
                    currentScreen = "home"
                }
            )

        "home" ->
            HomeScreen()
    }
}