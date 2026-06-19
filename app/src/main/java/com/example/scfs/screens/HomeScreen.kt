package com.example.scfs.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
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

@Composable
fun HomeScreen() {
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
                        "Machine 1",
                        fontFamily = Hanuman,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Color(0xFF3F3F3F)
                    )
                    Text("● Online", fontSize = 18.sp, color = Color(0xFF5EE86B))
                }
                Spacer(Modifier.weight(1f))
                Text("♧", fontSize = 28.sp)
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
                            "50 %",
                            fontFamily = Hanuman,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                        LinearProgressIndicator(
                            progress = { 0.5f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = Color(0xFFD7AEB0),
                            trackColor = Color(0xFFD9D9D9)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Last Feeding", fontFamily = Harmattan, fontSize = 18.sp)
                    Text(
                        "16.10 P.M.",
                        fontFamily = Hanuman,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )

                    Divider(Modifier.padding(vertical = 10.dp))

                    Row {
                        Column(Modifier.weight(1f)) {
                            Text("Cats today", fontFamily = Harmattan, fontSize = 16.sp)
                            Text("2", fontFamily = Hanuman, fontSize = 24.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Feedings today", fontFamily = Harmattan, fontSize = 16.sp)
                            Text("5", fontFamily = Hanuman, fontSize = 24.sp)
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

            CatHomeCard("Luna", "52g / 65g", R.drawable.cat_placeholder_1)
            CatHomeCard("Sophie", "52g / 65g", R.drawable.cat_placeholder_2)

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
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(50.dp)),
                contentScale = ContentScale.Crop
            )

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
                    progress = { 0.8f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFD7AEB0),
                    trackColor = Color(0xFFD9D9D9)
                )
            }

            Text(
                "Last seen\n10 min ago",
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