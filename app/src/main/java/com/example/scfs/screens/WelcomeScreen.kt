package com.example.scfs.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scfs.R
import androidx.compose.ui.text.style.TextAlign
import com.example.scfs.ui.theme.Hanuman
import com.example.scfs.ui.theme.Harmattan
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun WelcomeScreen(
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.bg_paw),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Image(
                painter = painterResource(R.drawable.logo_circle),
                contentDescription = null,
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "SCFS",
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Hanuman
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Smart Cat\nFeeding System",
                fontSize = 30.sp,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                fontFamily = Harmattan,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStart,

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE5DEDE)
                ),

                border = BorderStroke(
                    2.dp,
                    Color(0xFF6D6D6D)
                ),

                shape = RoundedCornerShape(18.dp),

                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)

            ) {

                Text(
                    text = "LET'S GET STARTED",

                    fontFamily = Hanuman,

                    fontWeight = FontWeight.ExtraBold,

                    fontSize = 22.sp,

                    color = Color(0xFF4A4A4A)
                )

            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}