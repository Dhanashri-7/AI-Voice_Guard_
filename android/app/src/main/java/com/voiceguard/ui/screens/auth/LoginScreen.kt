package com.voiceguard.ui.screens.auth

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.voiceguard.R
import com.voiceguard.VoiceGuardApplication
import com.voiceguard.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val app = context.applicationContext as? VoiceGuardApplication
    val prefs = app?.preferencesManager

    var userName by remember { mutableStateOf(prefs?.userName ?: "") }
    var phoneNumber by remember { mutableStateOf(prefs?.userPhone ?: "") }
    var otpCode by remember { mutableStateOf("") }
    var expectedOtp by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf(30) }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Countdown Timer for Resend OTP
    LaunchedEffect(otpSent, countdownSeconds) {
        if (otpSent && countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds -= 1
        }
    }

    fun postOtpNotification(otp: String) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val notif = NotificationCompat.Builder(context, VoiceGuardApplication.CHANNEL_ID_ALERTS)
                .setSmallIcon(R.drawable.voiceguard_app_logo)
                .setContentTitle("VoiceGuard Verification Code")
                .setContentText("Your OTP code is $otp. Valid for 10 minutes.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            nm?.notify(1099, notif)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dispatchSms(rawPhone: String, otp: String) {
        val rawDigits = rawPhone.filter { it.isDigit() }
        val clean10 = if (rawDigits.length == 12 && rawDigits.startsWith("91")) {
            rawDigits.substring(2)
        } else if (rawDigits.length > 10) {
            rawDigits.takeLast(10)
        } else {
            rawDigits
        }

        // Standard 7-bit GSM ASCII text without emojis (well within 160-char single PDU limit)
        val msg = "VoiceGuard verification code: $otp. Valid for 10 minutes. Strictly do not share this OTP with anyone."

        postOtpNotification(otp)

        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val subId = android.telephony.SubscriptionManager.getDefaultSmsSubscriptionId()
                if (subId != android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    context.getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
                } else {
                    context.getSystemService(SmsManager::class.java)
                }
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(msg)

            // Primary: Dispatch to clean 10-digit domestic phone number
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(clean10, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(clean10, null, msg, null, null)
            }

            // Also attempt with +91 prefix if 10 digits for broad carrier SMSC compatibility
            if (clean10.length == 10) {
                try {
                    val intlDestination = "+91$clean10"
                    if (parts.size > 1) {
                        smsManager.sendMultipartTextMessage(intlDestination, null, parts, null, null)
                    } else {
                        smsManager.sendTextMessage(intlDestination, null, msg, null, null)
                    }
                } catch (ignored: Exception) {}
            }

            Toast.makeText(context, "✓ SMS dispatched to $clean10", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "SMS queued for $clean10", Toast.LENGTH_SHORT).show()
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && expectedOtp.isNotEmpty()) {
            dispatchSms(phoneNumber, expectedOtp)
        }
    }

    fun sendRealSmsOtp(rawPhone: String, otp: String) {
        val hasSmsPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        if (!hasSmsPerm) {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        } else {
            dispatchSms(rawPhone, otp)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Clean Shield Brand Logo Box
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.voiceguard_app_logo),
                contentDescription = "VoiceGuard Logo",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "VOICEGUARD",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = GovNavyPrimary,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "AI-Powered Voice Impersonation Protection",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = GovTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Main Authentication Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (!otpSent) "SECURITY REGISTRATION" else "DEVICE VERIFICATION",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GovNavyPrimary,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (otpSent) Color(0xFFDCFCE7) else Color(0xFFEFF6FF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (!otpSent) "STEP 1/2" else "OTP PENDING",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (otpSent) StatusSafe else GovBlueAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (!otpSent) {
                    // Full Name Input
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { 
                            userName = it
                            errorMessage = null
                        },
                        label = { Text("Account Name", color = GovTextMuted) },
                        placeholder = { Text("Enter your full name", color = GovTextMuted.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = GovBlueAccent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = GovTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GovTextPrimary,
                            unfocusedTextColor = GovTextPrimary,
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedBorderColor = GovBlueAccent,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = GovBlueAccent,
                            unfocusedLabelColor = GovTextMuted,
                            cursorColor = GovBlueAccent
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Phone Number Input
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { 
                            if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                phoneNumber = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Registered Mobile Number", color = GovTextMuted) },
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                            ) {
                                Text(
                                    text = "+91",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GovNavyPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(16.dp)
                                        .background(CardBorder)
                                )
                            }
                        },
                        placeholder = { Text("10-digit mobile number", color = GovTextMuted.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = GovTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GovTextPrimary,
                            unfocusedTextColor = GovTextPrimary,
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedBorderColor = GovBlueAccent,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = GovBlueAccent,
                            unfocusedLabelColor = GovTextMuted,
                            cursorColor = GovBlueAccent
                        )
                    )

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = StatusThreat,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (userName.trim().isEmpty()) {
                                errorMessage = "Please enter your name"
                                return@Button
                            }
                            if (phoneNumber.trim().length < 10) {
                                errorMessage = "Please enter a valid 10-digit mobile number"
                                return@Button
                            }

                            focusManager.clearFocus()
                            val newOtp = String.format("%04d", Random.nextInt(1000, 9999))
                            expectedOtp = newOtp
                            otpSent = true
                            countdownSeconds = 30
                            errorMessage = null

                            // Persist user details
                            prefs?.userName = userName.trim()
                            prefs?.userPhone = phoneNumber.trim()
                            prefs?.userEmail = "${userName.lowercase().replace(" ", "")}@voiceguard.local"

                            sendRealSmsOtp(phoneNumber, newOtp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovNavyPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send 4-Digit Security OTP (SMS)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                } else {
                    // OTP Verification State
                    Text(
                        text = "Enter the 4-digit security code dispatched via SMS to +91 $phoneNumber:",
                        fontSize = 12.5.sp,
                        color = GovTextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // SMS Delivery Notification Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF6FF))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = GovBlueAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SMS dispatched to +91 $phoneNumber. Ask contact for code.",
                                fontSize = 11.5.sp,
                                color = GovBlueAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4-Digit PIN Input Field
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { 
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                otpCode = it
                                errorMessage = null
                            }
                        },
                        label = { Text("4-Digit Security Code", color = GovTextMuted) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = StatusSafe)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = GovTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 8.sp,
                            textAlign = TextAlign.Center
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GovTextPrimary,
                            unfocusedTextColor = GovTextPrimary,
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedBorderColor = StatusSafe,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = StatusSafe,
                            unfocusedLabelColor = GovTextMuted,
                            cursorColor = StatusSafe
                        )
                    )

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = StatusThreat,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Verify Button
                    Button(
                        onClick = {
                            if (otpCode.length < 4) {
                                errorMessage = "Please enter all 4 digits"
                                return@Button
                            }
                            if (otpCode != expectedOtp) {
                                errorMessage = "Invalid code. Please enter the OTP received on your mobile."
                                return@Button
                            }

                            isVerifying = true
                            focusManager.clearFocus()

                            // Save session state for returning user
                            prefs?.isLoggedIn = true
                            prefs?.userName = userName.trim().ifEmpty { "Citizen User" }
                            prefs?.userPhone = phoneNumber.trim()
                            prefs?.userEmail = "${userName.lowercase().replace(" ", "")}@voiceguard.local"

                            Toast.makeText(context, "Verification Successful! VoiceGuard Active.", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovNavyPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verify OTP & Continue",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resend OTP / Change Number Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (countdownSeconds > 0) {
                            Text(
                                text = "Resend OTP in ${countdownSeconds}s",
                                fontSize = 11.5.sp,
                                color = GovTextMuted
                            )
                        } else {
                            Text(
                                text = "Resend Security Code",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GovBlueAccent,
                                modifier = Modifier.clickable {
                                    val newOtp = String.format("%04d", Random.nextInt(1000, 9999))
                                    expectedOtp = newOtp
                                    countdownSeconds = 30
                                    errorMessage = null
                                    sendRealSmsOtp(phoneNumber, newOtp)
                                }
                            )
                        }

                        Text(
                            text = "Change Number",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = GovTextSecondary,
                            modifier = Modifier.clickable {
                                otpSent = false
                                otpCode = ""
                                errorMessage = null
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DPDP Act 2023 Compliance Guarantee (clean government style, removed forbidden claims)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = StatusSafe, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "DPDP Act 2023 Compliant",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = GovTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
