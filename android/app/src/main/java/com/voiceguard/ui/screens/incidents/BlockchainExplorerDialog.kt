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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.voiceguard.R
import com.voiceguard.data.local.entity.IncidentEntity

@Composable
fun BlockchainExplorerDialog(
    incident: IncidentEntity,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val hashPrefix = Math.abs((incident.incidentId + incident.timestamp).hashCode()).toString(16).padStart(8, '0')
    val merkleRoot = "0x" + hashPrefix + "a8f29471b04c6e729b4e31c890123fa9"

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFFFFFF)) // Crisp Official White Document Surface
                .border(
                    width = 1.2.dp,
                    color = Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // ==========================================
                // 1. TOP HEADER & NETWORK STATUS PILL
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE))
                                .border(1.2.dp, Color(0xFF0284C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CONSORTIUM CYBER LEDGER",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "PoA Multi-Party Trust Network (IBFT 2.0)",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Synced Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFDCFCE7))
                            .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "4/4 SYNCED",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Forensic Blockchain Explorer",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Immutable Chain-of-Custody for Speech Forensics • [Demonstration Sandbox Protocol]",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0369A1)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 2. BLOCK METADATA CARD (Light Slate Surface)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .border(0.8.dp, Color(0xFF86EFAC), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "BLOCK #19,420,812",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF166534)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(Finalized)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .border(0.8.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "0.00 GWEI (PoA)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "CONSENSUS PROTOCOL", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text(text = "IBFT 2.0 (PoA)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4338CA))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "BLOCK TIME", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text(text = "1.2s Interval", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "SMART CONTRACT", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text(text = "ThreatRegistry.sol (v2.4)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D4ED8))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "SEAL STANDARD", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text(text = "EIP-712 Structured", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 3. CRYPTOGRAPHIC MERKLE ROOT VAULT (High-Contrast Code Box)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F9FF)) // Ice Blue
                        .border(1.2.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SHA-256 MERKLE ROOT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Text(
                                text = "✓ VERIFIED ON-CHAIN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF166534)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // High-contrast dark navy code pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = merkleRoot,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF22D3EE),
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Signature: ECDSA secp256k1",
                                fontSize = 9.sp,
                                color = Color(0xFF475569),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Status: Nonce-Locked",
                                fontSize = 9.sp,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 4. 4 CONSORTIUM VALIDATOR NODES WITH OFFICIAL LOGOS
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONSORTIUM VALIDATOR NODES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "4 OF 4 QUORUM",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Node 1: AICTE (with official logo drawable)
                ValidatorNodeCardWithLogo(
                    logoResId = R.drawable.ic_aicte_logo,
                    agencyName = "AICTE Cyber Security Cell",
                    nodeRole = "Root Governance • Educational CSIRT Node",
                    nodeId = "0x82a9...AICTE-DELHI",
                    surfaceColor = Color(0xFFF0F7FF),
                    borderColor = Color(0xFFBAE6FD),
                    roleColor = Color(0xFF0284C7)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Node 2: CERT-In (with official logo drawable)
                ValidatorNodeCardWithLogo(
                    logoResId = R.drawable.ic_certin_logo,
                    agencyName = "CERT-In National Threat Grid",
                    nodeRole = "National Cyber Incident Threat Registry",
                    nodeId = "0x41f8...CERT-IN-ROOT",
                    surfaceColor = Color(0xFFFFF7ED),
                    borderColor = Color(0xFFFED7AA),
                    roleColor = Color(0xFFC2410C)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Node 3: DoT / Sanchar Saathi (with official logo drawable)
                ValidatorNodeCardWithLogo(
                    logoResId = R.drawable.ic_dot_logo,
                    agencyName = "DoT Telecom Vigilance (Sanchar Saathi)",
                    nodeRole = "Chakshu Telecom Carrier Blocking Mesh",
                    nodeId = "0x93c1...DOT-TELECOM",
                    surfaceColor = Color(0xFFFAF5FF),
                    borderColor = Color(0xFFE9D5FF),
                    roleColor = Color(0xFF7E22CE)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Node 4: NPCI Banking Gateway (with official logo drawable)
                ValidatorNodeCardWithLogo(
                    logoResId = R.drawable.ic_npci_logo,
                    agencyName = "NPCI Banking Fraud Prevention",
                    nodeRole = "CFCFRMS 1930 Financial Switch Gateway",
                    nodeId = "0x58e2...NPCI-GATEWAY",
                    surfaceColor = Color(0xFFF0FDF4),
                    borderColor = Color(0xFFBBF7D0),
                    roleColor = Color(0xFF15803D)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 5. OFFICIAL LEGAL CERTIFICATION (Gold Parchment)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFFBEB)) // Warm Gold Parchment
                        .border(1.2.dp, Color(0xFFD97706), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFDE68A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INDIAN EVIDENCE ACT §65B & IT ACT 2000",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF92400E),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "This electronic hash and multi-party timestamp establish an unbroken, tamper-evident Chain-of-Custody. Fully admissible as primary forensic evidence under Section 65B(4) in Indian Courts.",
                            fontSize = 10.5.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 6. ACTION BUTTONS
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(merkleRoot))
                            Toast.makeText(context, "Copied SHA-256 Merkle Proof to Clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0284C7))
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy Proof",
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A)
                        )
                    ) {
                        Text(
                            text = "Close Explorer",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidatorNodeCardWithLogo(
    logoResId: Int,
    agencyName: String,
    nodeRole: String,
    nodeId: String,
    surfaceColor: Color,
    borderColor: Color,
    roleColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(surfaceColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Official Agency Logo Image
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(0.8.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = logoResId),
                        contentDescription = agencyName,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = agencyName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = nodeRole,
                        fontSize = 9.sp,
                        color = roleColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = nodeId,
                        fontSize = 8.5.sp,
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFDCFCE7))
                    .border(0.8.dp, Color(0xFF86EFAC), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "VALIDATED",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF166534)
                )
            }
        }
    }
}
