package com.voiceguard.ui.screens.api

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voiceguard.ui.theme.*

@Composable
fun EnterpriseApiScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var activeTab by remember { mutableStateOf("SCREENING") }

    val screeningResponse = """
{
  "incoming_number": "+919876543210",
  "screening_decision": "SILENCE_AND_WARN",
  "risk_level": "HIGH_THREAT",
  "reputation_score": 0.92,
  "recommended_overlay": "VOICEGUARD_CRITICAL_WARNING_OVERLAY",
  "explanation": "Flagged in National Cyber Crime Registry (142 reports)"
}
    """.trimIndent()

    val bankingResponse = """
{
  "status": "BLOCKED_PENDING_STEP_UP",
  "action_code": "TRIGGER_BIOMETRIC_VIDEO_KYC",
  "voice_authenticity": 0.12,
  "transaction_amount_inr": 50000,
  "coercion_risk": "CRITICAL",
  "recommendation": "Do not process transfer. Initiate out-of-band video verification."
}
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
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
                    text = "B2B & REGULATORY INTEGRATION",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovNavyPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Enterprise & Banking API",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Expose real-time voice cloning and fraud risk signals to Telecom IMS cores, Banking fraud engines, and Enterprise Call Centers via REST & WebSocket.",
            fontSize = 12.sp,
            color = GovTextSecondary,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { activeTab = "SCREENING" },
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "SCREENING") GovNavyPrimary else CardBackground
                ),
                border = if (activeTab != "SCREENING") ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)) else null
            ) {
                Text(
                    text = "Telco Call Screening",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeTab == "SCREENING") Color.White else GovTextSecondary
                )
            }

            Button(
                onClick = { activeTab = "BANKING" },
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "BANKING") GovNavyPrimary else CardBackground
                ),
                border = if (activeTab != "BANKING") ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)) else null
            ) {
                Text(
                    text = "Banking Transfer Guard",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeTab == "BANKING") Color.White else GovTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // API Endpoint Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "POST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusSafe)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (activeTab == "SCREENING") "/api/v1/telecom/screen" else "/api/v1/banking/verify-transaction-call",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GovTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "RESPONSE PAYLOAD (200 OK)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovNavyPrimary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (activeTab == "SCREENING") screeningResponse else bankingResponse,
                        fontSize = 11.sp,
                        color = Color(0xFF4ADE80),
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Streaming WebSocket Documentation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "WS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GovBlueAccent)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "/ws/live-analysis",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GovTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bi-directional streaming WebSocket for feeding 1-2s audio chunks with sliding window feature extraction and instant real-time risk pushing.",
                    fontSize = 11.sp,
                    color = GovTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
