package com.example.scfs.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scfs.R
import com.example.scfs.data.CatDto
import com.example.scfs.data.DashboardCatDto
import com.example.scfs.data.DashboardRepository
import com.example.scfs.data.MachineDto
import com.example.scfs.data.MachineStatusDto
import androidx.compose.material3.IconButton

@Composable
fun HomeScreen(
    onAddMachine: () -> Unit,
    onAddCat: () -> Unit
) {
    var machine by remember { mutableStateOf<MachineDto?>(null) }
    var cats by remember { mutableStateOf<List<CatDto>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var dashboardCats by remember { mutableStateOf<List<DashboardCatDto>>(emptyList()) }
    var status by remember { mutableStateOf<MachineStatusDto?>(null) }

    LaunchedEffect(Unit) {
        try {
            val loadedCats = DashboardRepository.getDashboardCats()
            dashboardCats = loadedCats

            val firstMachineId = loadedCats.firstOrNull()?.machine_id
            if (firstMachineId != null) {
                status = DashboardRepository.getMachineStatus(firstMachineId)
            }
        } catch (e: Exception) {
            error = e.message ?: "Dashboard loading failed"
        }
    }

    val fillLevel = status?.food_level_percent ?: 50
    val machineName = dashboardCats.firstOrNull()?.machine_name ?: "Machine"
    val isOnline = status?.last_seen != null

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
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(45.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("☰", fontSize = 30.sp)

                Spacer(Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        machineName,
                        fontFamily = Hanuman,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Color(0xFF3F3F3F)
                    )

                    Text(
                        if (isOnline) "● Online" else "● Offline",
                        fontSize = 18.sp,
                        color = if (isOnline) Color(0xFF5EE86B) else Color.Gray
                    )
                }

                Spacer(Modifier.weight(1f))

                Image(
                    painter = painterResource(R.drawable.notification_icon),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )            }

            if (error.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = Color(0xFF8A3A3A), fontFamily = Harmattan)
            }

            Spacer(Modifier.height(22.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.machine),
                        contentDescription = null,
                        modifier = Modifier.size(70.dp)
                    )

                    Column(Modifier.weight(1f)) {
                        Text("Fill Level", fontFamily = Harmattan, fontSize = 20.sp)

                        Text(
                            "$fillLevel %",
                            fontFamily = Hanuman,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )

                        LinearProgressIndicator(
                            progress = { fillLevel / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = Color(0xFFD7AEB0),
                            trackColor = Color(0xFFD9D9D9)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            PlusIconButton(
                onClick = onAddMachine,
                contentDescription = "Add machine"
            )

            Spacer(Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Last Feeding", fontFamily = Harmattan, fontSize = 18.sp)

                    Text(
                        "--:--",
                        fontFamily = Hanuman,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )

                    Divider(Modifier.padding(vertical = 10.dp))

                    Row {
                        Column(Modifier.weight(1f)) {
                            Text("Cats today", fontFamily = Harmattan, fontSize = 16.sp)
                            Text("${dashboardCats.size}", fontFamily = Hanuman, fontSize = 24.sp)                        }

                        Column(Modifier.weight(1f)) {
                            Text("Feedings today", fontFamily = Harmattan, fontSize = 16.sp)
                            Text("0", fontFamily = Hanuman, fontSize = 24.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Cats",
                fontFamily = Hanuman,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            if (dashboardCats.isEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "No cats found for this machine.",
                    fontFamily = Harmattan,
                    fontSize = 18.sp
                )
            } else {
                dashboardCats.forEachIndexed { index, cat ->
                    CatHomeCard(
                        name = cat.cat_name,
                        food = "0g / ${cat.daily_food_goal_g ?: 0}g",
                        imageRes = if (index % 2 == 0) R.drawable.cat_placeholder_1 else R.drawable.cat_placeholder_2
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            PlusIconButton(
                onClick = onAddCat,
                contentDescription = "Add cat"
            )

            Spacer(Modifier.weight(1f))

            BottomNav()
        }
    }
}

@Composable
fun CatHomeCard(name: String, food: String, imageRes: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(37.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(name, fontFamily = Harmattan, fontSize = 18.sp)

                Text(
                    food,
                    fontFamily = Hanuman,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                LinearProgressIndicator(
                    progress = { 0.2f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFD7AEB0),
                    trackColor = Color(0xFFD9D9D9)
                )
            }

            Text(
                "Last seen\n--",
                fontFamily = Harmattan,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun BottomNav() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(R.drawable.home_icon, "Home")
        NavItem(R.drawable.cats_icon, "Cats")
        NavItem(R.drawable.statistics_icon, "Statistics")
        NavItem(R.drawable.settings_icon, "Settings")
    }
}

@Composable
fun NavItem(icon: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(label, fontFamily = Harmattan, fontSize = 12.sp)
    }
}
@Composable
fun PlusIconButton(
    onClick: () -> Unit,
    contentDescription: String
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(34.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.plus_icon),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}