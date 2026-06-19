package com.example.scfs.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scfs.R

@Composable
fun TakePhotosScreen(onFinish: () -> Unit) {
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
                text = "Take 4 Photos",
                fontFamily = Hanuman,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                color = Color(0xFF3F3F3F)
            )

            Spacer(Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoBox(R.drawable.cat_placeholder_1)
                PhotoBox(R.drawable.cat_placeholder_2)
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoBox(R.drawable.cat_placeholder_3)
                PhotoBox(R.drawable.cat_placeholder_4)
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "The better the photos, the\neasier it is to identify the cats!",
                fontFamily = Harmattan,
                fontSize = 22.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF3F3F3F)
            )

            Spacer(Modifier.weight(1f))

            SCFSButton(
                text = "Finish Setup",
                onClick = onFinish
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun PhotoBox(imageRes: Int) {
    Box(
        modifier = Modifier
            .size(width = 125.dp, height = 135.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier.size(105.dp)
        )
    }
}