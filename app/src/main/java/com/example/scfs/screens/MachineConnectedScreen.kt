package com.example.scfs.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.scfs.R
import androidx.compose.ui.unit.sp

@Composable
fun MachineConnectedScreen(
    onContinue: () -> Unit
) {
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
            Image(
                painter = painterResource(R.drawable.success),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Spacer(Modifier.height(35.dp))

            Image(
                painter = painterResource(R.drawable.machine),
                contentDescription = null,
                modifier = Modifier.size(170.dp)
            )

            Spacer(Modifier.height(35.dp))

            androidx.compose.material3.Text(
                text = "Machine 1\nconnected!",
                fontFamily = Hanuman,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                fontSize = 24.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = androidx.compose.ui.graphics.Color(0xFF3F3F3F)
            )

            Spacer(Modifier.height(60.dp))

            SCFSButton(
                text = "Continue",
                onClick = onContinue
            )
        }
    }
}