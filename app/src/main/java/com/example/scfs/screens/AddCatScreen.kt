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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scfs.R
import kotlinx.coroutines.launch
import com.example.scfs.data.CatInsert
import com.example.scfs.data.SupabaseManager
import io.github.jan.supabase.postgrest.from

@Composable
fun AddCatScreen(onNext: () -> Unit) {
    var name by remember { mutableStateOf("Luna") }
    var weight by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var debugText by remember { mutableStateOf("") }

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(55.dp))

            Text(
                text = "Add Cat",
                fontFamily = Hanuman,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                color = Color(0xFF3F3F3F)
            )

            Spacer(Modifier.height(18.dp))

            Box {
                Image(
                    painter = painterResource(R.drawable.cat_silhouette),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )

                Image(
                    painter = painterResource(R.drawable.camera_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(34.dp)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(Modifier.height(24.dp))

            SCFSTextField(
                label = "Name",
                value = name,
                placeholder = "e.g. Luna",
                onValueChange = { name = it }
            )

            Spacer(Modifier.height(14.dp))

            SCFSTextField(
                label = "Weight",
                value = weight,
                placeholder = "e.g. 4.2 KG",
                onValueChange = { weight = it }
            )

            Spacer(Modifier.height(14.dp))

            SCFSTextField(
                label = "Daily Food Goal",
                value = goal,
                placeholder = "e.g. 70 g",
                onValueChange = { goal = it }
            )

            Spacer(Modifier.weight(1f))
            SCFSButton(
                text = "Next",
                onClick = {
                    debugText = "Saving..."

                    scope.launch {
                        try {
                            SupabaseManager.client
                                .from("cats")
                                .insert(
                                    CatInsert(
                                        name = name,
                                        weight_kg = weight.toDoubleOrNull(),
                                        daily_food_goal_g = goal.toIntOrNull()
                                    )
                                )

                            debugText = "Saved"
                            onNext()

                        } catch (e: Exception) {
                            debugText = "Supabase Error: ${e.message}"
                            e.printStackTrace()
                        }
                    }
                }
            )

            Text(debugText)
            Spacer(Modifier.height(40.dp))
        }
    }
}