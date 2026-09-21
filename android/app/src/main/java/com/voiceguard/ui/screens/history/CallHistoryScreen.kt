package com.voiceguard.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voiceguard.data.local.entity.IncidentEntity
import com.voiceguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallHistoryScreen(
    incidents: List<IncidentEntity>,
    onSelectIncident: (IncidentEntity) -> Unit,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "HIGH RISK", "SUSPICIOUS", "SAFE")

    val filteredList = remember(incidents, selectedFilter) {
        when (selectedFilter) {
            "HIGH RISK" -> incidents.filter { it.riskScore >= 75 }
            "SUSPICIOUS" -> incidents.filter { it.riskScore in 40..74 }
            "SAFE" -> incidents.filter { it.riskScore < 40 }
            else -> incidents
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GovTextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "CALL SCREENING ARCHIVE",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovNavyPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Call History & Incidents",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) GovNavyPrimary else CardBackground)
                        .border(1.dp, if (isSelected) GovNavyPrimary else CardBorder, RoundedCornerShape(20.dp))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else GovTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Incidents List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = GovTextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No recorded incidents in this category.",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GovTextPrimary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { incident ->
                    IncidentHistoryCard(
                        incident = incident,
                        onClick = { onSelectIncident(incident) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun IncidentHistoryCard(
    incident: IncidentEntity,
    onClick: () -> Unit
) {
    val isHighRisk = incident.riskScore >= 75
    val isSuspicious = incident.riskScore in 40..74
    val badgeColor = if (isHighRisk) StatusThreat else if (isSuspicious) StatusWarning else StatusSafe
    val badgeBg = if (isHighRisk) Color(0xFFFEE2E2) else if (isSuspicious) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(incident.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(badgeBg)
                    .border(1.dp, badgeColor.copy(alpha = 0.5f), CircleShape)
            ) {
                Text(
                    text = "${incident.riskScore}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = incident.callerName?.ifBlank { incident.callerNumber } ?: incident.callerNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovTextPrimary
                )
                Text(
                    text = "${incident.threatType} • ${incident.language.uppercase()}",
                    fontSize = 11.5.sp,
                    color = badgeColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    fontSize = 10.5.sp,
                    color = GovTextMuted
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GovTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
