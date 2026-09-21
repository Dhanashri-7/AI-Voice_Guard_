package com.voiceguard.ui.screens.reconstruction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voiceguard.domain.model.AttackStage
import com.voiceguard.domain.model.AttackScenario
import com.voiceguard.ui.theme.*

@Composable
fun AttackReconstructionScreen(
    scenario: AttackScenario,
    riskScore: Int,
    onViewIncidentReport: () -> Unit,
    onBackToHome: () -> Unit
) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "POST-CALL FORENSICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusThreat,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Attack Reconstruction",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovTextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFFEE2E2))
                    .border(1.dp, StatusThreat.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$riskScore / 100",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusThreat
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Analysis of how this impersonation attack escalated across 7 psychological & acoustic stages.",
            fontSize = 12.sp,
            color = GovTextSecondary,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7 Stages List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(scenario.reconstructionStages) { stage ->
                ReconstructionStageCard(stage = stage)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Footer Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onViewIncidentReport,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovNavyPrimary)
            ) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Incident Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
            ) {
                Text(text = "Done / Home", color = GovTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ReconstructionStageCard(stage: AttackStage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            val badgeColor = if (stage.stageNumber == 7) StatusSafe else if (stage.stageNumber >= 5) StatusThreat else StatusWarning
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            ) {
                Text(
                    text = "${stage.stageNumber}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stage.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stage.description,
                    fontSize = 11.sp,
                    color = GovTextSecondary,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "EVIDENCE: ${stage.evidence}",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GovBlueAccent
                    )
                }
            }
        }
    }
}
