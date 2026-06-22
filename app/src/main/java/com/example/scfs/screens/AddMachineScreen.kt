package com.example.scfs.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scfs.R
import com.example.scfs.data.MachineInsert
import com.example.scfs.data.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

val Hanuman = FontFamily(
    Font(R.font.hanuman_regular),
    Font(R.font.hanuman_bold, FontWeight.Bold),
    Font(R.font.hanuman_black, FontWeight.ExtraBold)
)

val Harmattan = FontFamily(
    Font(R.font.harmattan_regular)
)

@Composable
fun AddMachineScreen(onNext: () -> Unit) {
    var machineName by remember { mutableStateOf("") }
    var wifi by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var debugText by remember { mutableStateOf("") }

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(55.dp))

            Image(
                painter = painterResource(R.drawable.machine),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Add Machine",
                fontFamily = Hanuman,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp,
                color = Color(0xFF3F3F3F)
            )

            Spacer(Modifier.height(24.dp))

            SCFSTextField(
                label = "Machine Name",
                value = machineName,
                placeholder = "e.g. Machine 1",
                onValueChange = { machineName = it }
            )

            Spacer(Modifier.height(16.dp))

            SCFSTextField(
                label = "WiFi Network",
                value = wifi,
                placeholder = "Select your WiFi",
                onValueChange = { wifi = it }
            )

            Spacer(Modifier.height(16.dp))

            SCFSPasswordField(
                label = "Password",
                value = password,
                placeholder = "Enter Password",
                onValueChange = { password = it }
            )
            Spacer(Modifier.weight(1f))

            SCFSButton(
                text = "Add Machine",
                onClick = {
                    scope.launch {
                        try {
                            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
                            if (userId == null) {
                                debugText = "No user logged in"
                                return@launch
                            }

                            val machineId = "machine-${System.currentTimeMillis()}"
                            SupabaseManager.client
                                .from("machines")
                                .upsert(
                                    MachineInsert(
                                        id = machineId,
                                        name = machineName.ifBlank { "Machine 1" }
                                    )
                                )

                            SupabaseManager.client
                                .from("machine_users")
                                .upsert(
                                    mapOf(
                                        "machine_id" to machineId,
                                        "user_id" to userId,
                                        "role" to "owner"
                                    )
                                )

                            debugText = ""
                            onNext()
                        } catch (e: Exception) {
                            debugText = "Error: ${e.message}"
                        }
                    }
                }
            )

            Text(debugText)
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SCFSTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = Harmattan,
            fontSize = 20.sp,
            color = Color(0xFF444444)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    fontFamily = Harmattan,
                    fontSize = 18.sp
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6D6D6D),
                unfocusedBorderColor = Color(0xFF6D6D6D),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SCFSButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE5DEDE)
        ),
        border = BorderStroke(2.dp, Color(0xFF6D6D6D)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
    ) {
        Text(
            text = text,
            fontFamily = Hanuman,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = Color(0xFF4A4A4A)
        )
    }
}
@Composable
fun SCFSPasswordField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = Harmattan,
            fontSize = 20.sp,
            color = Color(0xFF444444)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    fontFamily = Harmattan,
                    fontSize = 18.sp
                )
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6D6D6D),
                unfocusedBorderColor = Color(0xFF6D6D6D),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}