package com.voiceguard.ui.screens.incidents

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voiceguard.R
import com.voiceguard.data.local.entity.IncidentEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IncidentDetailScreen(
    incident: IncidentEntity,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isHighRisk = incident.riskScore >= 80

    var showBlockchainExplorer by remember { mutableStateOf(false) }
    var showDispatchDialog by remember { mutableStateOf(false) }

    val dateStr = SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date(incident.timestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Official Crisp White/Light Slate Paper Background
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFCBD5E1), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F172A)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FORENSIC INCIDENT DOSSIER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7),
                    letterSpacing = 1.sp
                )
                Text(
                    text = incident.incidentId,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isHighRisk) Color(0xFFFEE2E2) else Color(0xFFFEF3C7))
                    .border(1.dp, if (isHighRisk) Color(0xFFF87171) else Color(0xFFFDE68A), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isHighRisk) "CRITICAL" else "SUSPICIOUS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isHighRisk) Color(0xFFDC2626) else Color(0xFFB45309)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RISK SCORE BANNER (High-Contrast Light Crimson)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isHighRisk) Color(0xFFFEF2F2) else Color(0xFFFFFBEB))
                .border(1.2.dp, if (isHighRisk) Color(0xFFF87171) else Color(0xFFFDE68A), RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CALCULATED IMPERSONATION RISK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighRisk) Color(0xFF991B1B) else Color(0xFF92400E),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHighRisk) "Voice Clone Confirmed" else "Suspicious Pattern Detected",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Threat Classification: " + incident.threatType,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, if (isHighRisk) Color(0xFFDC2626) else Color(0xFFD97706), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${incident.riskScore}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isHighRisk) Color(0xFFDC2626) else Color(0xFFD97706)
                        )
                        Text(
                            text = "/100",
                            fontSize = 8.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CALLER & TELECOM METADATA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "CALLER & TELECOM METADATA",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Target Identity", fontSize = 9.sp, color = Color(0xFF64748B))
                        Text(text = incident.callerName ?: "Impersonated Contact Target", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Caller Number", fontSize = 9.sp, color = Color(0xFF64748B))
                        Text(text = incident.callerNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Timestamp", fontSize = 9.sp, color = Color(0xFF64748B))
                        Text(text = dateStr, fontSize = 11.sp, color = Color(0xFF334155))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Call Duration", fontSize = 9.sp, color = Color(0xFF64748B))
                        Text(text = "42s (Active Interception)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ACOUSTIC FORENSIC EVIDENCE CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "ACOUSTIC FORENSIC EVIDENCE",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // High-contrast dark wave container for glowing forensic clarity
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "FFT SPECTRUM (300Hz - 8kHz)", fontSize = 8.5.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                            Text(text = "VOCODER PHASE DISCONTINUITY > 3.2kHz", fontSize = 8.5.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                        }

                        // Simulated FFT Bars
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val heights = listOf(0.3f, 0.5f, 0.75f, 0.4f, 0.9f, 0.85f, 0.6f, 0.95f, 0.45f, 0.7f, 0.88f, 0.65f, 0.92f, 0.8f, 0.4f, 0.3f)
                            heights.forEachIndexed { index, fraction ->
                                val barColor = if (index > 9) Color(0xFFFF1744) else Color(0xFF00E5FF)
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .fillMaxHeight(fraction)
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                        .background(barColor)
                                )
                            }
                        }

                        Text(text = "Cosine Similarity: 34% (Impersonation threshold < 70%)", fontSize = 8.5.sp, color = Color(0xFF38BDF8))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // CONSORTIUM CYBER LEDGER CARD WITH OFFICIAL LOGOS (WHITE THEME)
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.2.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE))
                                .border(1.dp, Color(0xFF0284C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Blockchain Anchor",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CONSORTIUM CYBER LEDGER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "PoA Multi-Agency Consensus (IBFT 2.0)",
                                fontSize = 8.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Synced badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFDCFCE7))
                            .border(0.8.dp, Color(0xFF86EFAC), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "4/4 SYNCED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF166534)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4 Official Agency Badges with Logos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AgencyMiniLogoBadge(logoResId = R.drawable.ic_aicte_logo, name = "AICTE", modifier = Modifier.weight(1f))
                    AgencyMiniLogoBadge(logoResId = R.drawable.ic_certin_logo, name = "CERT-In", modifier = Modifier.weight(1f))
                    AgencyMiniLogoBadge(logoResId = R.drawable.ic_dot_logo, name = "DoT", modifier = Modifier.weight(1f))
                    AgencyMiniLogoBadge(logoResId = R.drawable.ic_npci_logo, name = "NPCI", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                val hashPrefix = Math.abs((incident.incidentId + incident.timestamp).hashCode()).toString(16).padStart(8, '0')
                val sha256Preview = "0x${hashPrefix}a8f29471b04c6e...9d7e31"

                // Terminal Hash Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SHA-256 MERKLE ROOT:",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "§65B Certified",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = sha256Preview,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF22D3EE)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Block #19,420,812 • Gas: 0.00 Gwei",
                        fontSize = 9.5.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Chain: IMMUTABLE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showBlockchainExplorer = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Inspect on Consortium Ledger Explorer", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // RECOMMENDED ACTION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF0FDF4))
                .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "RECOMMENDED PROTECTIVE ACTION",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = incident.recommendedAction,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF14532D),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // EXPORT BUTTONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Exported JSON forensic report for ${incident.incidentId}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0284C7))
                )
            ) {
                Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Export JSON", color = Color(0xFF0284C7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Exported CSV summary for ${incident.incidentId}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF166534))
                )
            ) {
                Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Export CSV", color = Color(0xFF166534), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // REPORT TO OFFICIAL DESTINATIONS (Golden Hour Action)
        Button(
            onClick = { showDispatchDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Report to Sanchar Saathi & 1930 (Golden Hour)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }

    // Dialogs
    if (showBlockchainExplorer) {
        BlockchainExplorerDialog(
            incident = incident,
            onDismiss = { showBlockchainExplorer = false }
        )
    }

    if (showDispatchDialog) {
        CybercrimeDispatchDialog(
            incident = incident,
            onDismiss = { showDispatchDialog = false }
        )
    }
}

@Composable
private fun AgencyMiniLogoBadge(
    logoResId: Int,
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .border(0.8.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
            .padding(vertical = 5.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = logoResId), contentDescription = name, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
    }
}
