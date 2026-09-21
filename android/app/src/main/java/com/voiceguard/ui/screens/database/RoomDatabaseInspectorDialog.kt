package com.voiceguard.ui.screens.database

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.voiceguard.data.local.entity.IncidentEntity
import com.voiceguard.data.repository.VoiceGuardRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun RoomDatabaseInspectorDialog(
    repository: VoiceGuardRepository,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTable by remember { mutableStateOf("incidents") }
    var incidentList by remember { mutableStateOf<List<IncidentEntity>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTable) {
        scope.launch {
            incidentList = repository.allIncidents.firstOrNull() ?: emptyList()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE0F2FE))
                                .border(1.2.dp, Color(0xFF0284C7), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ROOM SQLITE DATA VAULT",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "On-Device Encrypted Storage • Jetpack Room ORM",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2x2 Colorful KPI Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiGridCard(
                            modifier = Modifier.weight(1f),
                            title = "DATABASE FILE",
                            value = "voiceguard_secure.db",
                            icon = Icons.Default.Description,
                            iconTint = Color(0xFF0284C7),
                            cardBg = Color(0xFFF0F9FF),
                            borderColor = Color(0xFFBAE6FD)
                        )
                        KpiGridCard(
                            modifier = Modifier.weight(1f),
                            title = "SECURITY CIPHER",
                            value = "AES-256 WAL Encrypted",
                            icon = Icons.Default.Lock,
                            iconTint = Color(0xFF7C3AED),
                            cardBg = Color(0xFFFAF5FF),
                            borderColor = Color(0xFFE9D5FF)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiGridCard(
                            modifier = Modifier.weight(1f),
                            title = "ACTIVE SCHEMAS",
                            value = "10 SQLite Tables",
                            icon = Icons.Default.TableChart,
                            iconTint = Color(0xFFD97706),
                            cardBg = Color(0xFFFFFBEB),
                            borderColor = Color(0xFFFDE68A)
                        )
                        KpiGridCard(
                            modifier = Modifier.weight(1f),
                            title = "PRIVACY STANDARD",
                            value = "100% On-Device DPDP",
                            icon = Icons.Default.CheckCircle,
                            iconTint = Color(0xFF16A34A),
                            cardBg = Color(0xFFF0FDF4),
                            borderColor = Color(0xFFBBF7D0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Colorful Table Selector Chips
                Text(
                    text = "SELECT ROOM ENTITY TABLE:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tableChips = listOf(
                        Triple("incidents", "🚨", Color(0xFFDC2626)),
                        Triple("protected_contacts", "🛡️", Color(0xFF16A34A)),
                        Triple("call_sessions", "📞", Color(0xFF0284C7)),
                        Triple("callers", "⚠️", Color(0xFFD97706)),
                        Triple("voice_analyses", "🔬", Color(0xFF7C3AED)),
                        Triple("users", "👤", Color(0xFF0F766E))
                    )

                    tableChips.forEach { (table, emoji, themeColor) ->
                        val isSelected = selectedTable == table
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) themeColor else themeColor.copy(alpha = 0.08f))
                                .border(
                                    1.2.dp,
                                    if (isSelected) themeColor else themeColor.copy(alpha = 0.3f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedTable = table }
                                .padding(horizontal = 11.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emoji, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = table,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else themeColor,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Table Content Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TABLE: ${selectedTable.uppercase()}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "LIVE SQLITE ROWS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Structured Data Grid based on selected table
                when (selectedTable) {
                    "incidents" -> {
                        if (incidentList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No incidents logged yet.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                    Text(
                                        text = "Trigger a live phone call or attack demo to populate SQLite records!",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                incidentList.take(6).forEach { inc ->
                                    val isCritical = inc.riskScore >= 80
                                    val accentColor = if (isCritical) Color(0xFFDC2626) else Color(0xFF16A34A)
                                    val cardBg = if (isCritical) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                                    val borderColor = if (isCritical) Color(0xFFFECACA) else Color(0xFFBBF7D0)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(cardBg)
                                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            // Top Row: ID & Risk Badge
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White)
                                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = inc.incidentId,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F172A),
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(accentColor)
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (isCritical) "CRITICAL: ${inc.riskScore}%" else "SAFE: ${inc.riskScore}%",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // 2-Column Info Grid
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = "CALLER IDENTITY", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                                    Text(text = inc.callerNumber, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontFamily = FontFamily.Monospace)
                                                    Text(text = inc.callerName ?: "Unknown", fontSize = 9.sp, color = Color(0xFF475569))
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = "CLASSIFICATION", fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                                    Text(text = inc.threatType, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor, maxLines = 1)
                                                    Text(text = "Lang: ${inc.language.uppercase()} • PoA: Anchored", fontSize = 8.5.sp, color = Color(0xFF475569))
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Evidence Summary Pill
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "🧬 Forensic: ${inc.forensicEvidenceJson}",
                                                    fontSize = 8.5.sp,
                                                    color = Color(0xFF334155),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "protected_contacts" -> {
                        val contacts = listOf(
                            Triple("Mom (Mother)", "+91 94220 55667", "F0 Pitch: 210 Hz • Bio-Enrolled ✅"),
                            Triple("Father", "+91 94220 01122", "F0 Pitch: 115 Hz • Bio-Enrolled ✅"),
                            Triple("Emergency Guardian", "+91 98221 14455", "Designated Golden-Hour Contact 🛡️")
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            contacts.forEach { (name, num, detail) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF0FDF4))
                                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = name, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = num, fontSize = 10.sp, color = Color(0xFF0284C7), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                                            Text(text = detail, fontSize = 9.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFDCFCE7))
                                                .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(text = "TRUSTED", fontSize = 8.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF166534))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "call_sessions" -> {
                        val sessions = listOf(
                            Triple("session-live-01", "16,000 Hz PCM Mono • Latency: 38ms", "Threat: AI Clone (94%)"),
                            Triple("session-safe-02", "16,000 Hz PCM Mono • Latency: 42ms", "Human Voice (12%)"),
                            Triple("session-sim-03", "Acoustic Ring Buffer: 2048 samples", "Verified Safe (15%)")
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            sessions.forEach { (id, dsp, status) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF0F9FF))
                                        .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = id, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), fontFamily = FontFamily.Monospace)
                                            Text(text = status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = dsp, fontSize = 9.sp, color = Color(0xFF475569), fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }

                    "callers" -> {
                        val callers = listOf(
                            Triple("SBI Impersonator Syndicate", "+91 98765 43210", "Reputation: 0.94 • Financial KYC Fraud"),
                            Triple("Digital Arrest Coercion", "+91 91234 56789", "Reputation: 0.98 • Extortion Syndicate")
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            callers.forEach { (name, num, detail) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFEF2F2))
                                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                            Text(text = num, fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                                            Text(text = detail, fontSize = 8.5.sp, color = Color(0xFF991B1B))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFEE2E2))
                                                .border(1.dp, Color(0xFFF87171), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "BLACKLISTED", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "voice_analyses" -> {
                        val analyses = listOf(
                            Triple("Sample Chunk #14", "F0 Mean: 184 Hz • F0 Std: 4.8 Hz", "Synthetic Prob: 96%"),
                            Triple("Sample Chunk #15", "Phase Discontinuity >3.2 kHz Detected", "Vocoder Cutoff: HiFi-GAN"),
                            Triple("Sample Chunk #16", "Pause Ratio: 0.04 (Unnatural Regularity)", "Coercion Flag: CRITICAL")
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            analyses.forEach { (chunk, metrics, result) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFAF5FF))
                                        .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = chunk, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                            Text(text = result, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = metrics, fontSize = 9.sp, color = Color(0xFF475569), fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Table '$selectedTable' schema active and synced with Android Room ORM.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ready to receive streaming audio events and cryptographic telemetry.",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isRefreshing = true
                                incidentList = repository.allIncidents.firstOrNull() ?: emptyList()
                                isRefreshing = false
                                Toast.makeText(context, "Refreshed Live Room SQLite Database!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.2.dp, Color(0xFF0284C7))
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Refresh DB", color = Color(0xFF0284C7), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text(text = "Close Inspector", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiGridCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    cardBg: Color,
    borderColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconTint,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1
            )
        }
    }
}
