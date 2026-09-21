package com.voiceguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voiceguard.ui.theme.*

@Composable
fun ThreatTimelineView(
    events: List<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(events.reversed()) { event ->
            val isAlert = event.contains("ALERT") || event.contains("CRITICAL") || event.contains("SUSPICIOUS")
            val dotColor = if (isAlert) ThreatCrimson else NeuralCyan

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = event,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = if (isAlert) ThreatCrimson else TextSecondary,
                    fontWeight = if (isAlert) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
