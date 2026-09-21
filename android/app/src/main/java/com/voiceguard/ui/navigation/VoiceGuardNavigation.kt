package com.voiceguard.ui.navigation
import com.voiceguard.telecom.RealCallManager
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.voiceguard.data.local.PreferencesManager
import com.voiceguard.data.local.entity.IncidentEntity
import com.voiceguard.data.repository.VoiceGuardRepository
import com.voiceguard.domain.model.AttackScenario
import com.voiceguard.simulator.CallSimulatorEngine
import com.voiceguard.simulator.DemoAttackScenarios
import com.voiceguard.ui.screens.api.EnterpriseApiScreen
import com.voiceguard.ui.screens.attacklab.AttackLabScreen
import com.voiceguard.ui.screens.auth.LoginScreen
import com.voiceguard.ui.screens.call.IncomingCallScreen
import com.voiceguard.ui.screens.contacts.ProtectedContactsScreen
import com.voiceguard.ui.screens.dashboard.DashboardScreen
import com.voiceguard.ui.screens.explain.ExplainabilityDialog
import com.voiceguard.ui.screens.history.CallHistoryScreen
import com.voiceguard.ui.screens.incidents.IncidentDetailScreen
import com.voiceguard.ui.screens.onboarding.OnboardingScreen
import com.voiceguard.ui.screens.privacy.PrivacyCenterScreen
import com.voiceguard.ui.screens.reconstruction.AttackReconstructionScreen
import com.voiceguard.ui.screens.splash.SplashScreen
import com.voiceguard.ui.theme.*
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String? = null, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Shield)
    object IncomingCall : Screen("incoming_call")
    object AttackLab : Screen("attack_lab", "Threats", Icons.Default.Security)
    object AttackReconstruction : Screen("attack_reconstruction")
    object ProtectedContacts : Screen("protected_contacts", "Contacts", Icons.Default.RecordVoiceOver)
    object CallHistory : Screen("call_history", "History", Icons.Default.History)
    object IncidentDetail : Screen("incident_detail")
    object PrivacyCenter : Screen("privacy_center", "Privacy", Icons.Default.Lock)
    object EnterpriseApi : Screen("enterprise_api")
}

@Composable
fun VoiceGuardNavHost(
    repository: VoiceGuardRepository,
    prefs: PreferencesManager,
    simulatorEngine: CallSimulatorEngine,
    realCallManager: RealCallManager,
    openForensicDetails: Boolean = false
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val simState by simulatorEngine.simState.collectAsState()
    val realCallState by realCallManager.realCallState.collectAsState()

    val allIncidents by repository.allIncidents.collectAsState(initial = emptyList())
    val protectedContacts by repository.allProtectedContacts.collectAsState(initial = emptyList())

    var selectedIncident by remember { mutableStateOf<IncidentEntity?>(null) }
    var showExplainDialog by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Open forensic analysis screen ONLY when user explicitly taps high-risk alert notification!
    LaunchedEffect(openForensicDetails) {
        if (openForensicDetails && currentRoute != Screen.IncomingCall.route) {
            navController.navigate(Screen.IncomingCall.route)
        }
    }

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.AttackLab,
        Screen.ProtectedContacts,
        Screen.CallHistory,
        Screen.PrivacyCenter
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = Color.White,
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomNavItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            Column(
                                modifier = Modifier
                                    .widthIn(min = 72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                                        .padding(horizontal = 14.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    screen.icon?.let {
                                        Icon(
                                            imageVector = it,
                                            contentDescription = screen.title,
                                            tint = if (isSelected) Color(0xFF0F2546) else Color(0xFF64748B),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = screen.title ?: "",
                                    color = if (isSelected) Color(0xFF0F2546) else Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onNavigateNext = {
                    val nextRoute = if (prefs.isLoggedIn) Screen.Dashboard.route else Screen.Login.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Login.route) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(onFinishOnboarding = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    incidents = allIncidents,
                    contacts = protectedContacts,
                    userName = prefs.userName,
                    currentUserPhone = prefs.userPhone,
                    currentUserEmail = prefs.userEmail,
                    currentUserAdditionalInfo = prefs.userAdditionalInfo,
                    onUpdateProfile = { name, phone, email, info ->
                        prefs.userName = name
                        prefs.userPhone = phone
                        prefs.userEmail = email
                        prefs.userAdditionalInfo = info
                    },
                    realCallManager = realCallManager,
                    onLogout = {
                        prefs.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateAttackLab = { navController.navigate(Screen.AttackLab.route) },
                    onNavigateContacts = { navController.navigate(Screen.ProtectedContacts.route) },
                    onNavigateHistory = { navController.navigate(Screen.CallHistory.route) },
                    onNavigateEnterpriseApi = { navController.navigate(Screen.EnterpriseApi.route) }
                )
            }

            composable(Screen.IncomingCall.route) {
                IncomingCallScreen(
                    simState = simState,
                    realCallManager = realCallManager,
                    onWhyClicked = { showExplainDialog = true },
                    onVerifyCallerClicked = {
                        Toast.makeText(context, "Safe Verification: Initiating independent callback to Mom's enrolled trusted number.", Toast.LENGTH_LONG).show()
                    },
                    onHangUpClicked = {
                        val isReal = realCallManager?.realCallState?.value?.isCallActive == true
                        val realStateVal = realCallManager?.realCallState?.value
                        simulatorEngine.endCall()
                        realCallManager?.onCallEnded()

                        val scenario = simState.currentScenario ?: DemoAttackScenarios.scenarios[0]
                        val score = if (isReal && realStateVal != null) realStateVal.currentRiskScore else (simState.currentRiskScore?.overallScore ?: 91)
                        val callerNumber = if (isReal && realStateVal != null && realStateVal.callerNumber.isNotBlank()) realStateVal.callerNumber else scenario.incomingNumber
                        val callerName = if (isReal && realStateVal != null && realStateVal.callerName.isNotBlank()) realStateVal.callerName else scenario.callerName

                        // Persist incident to Room database
                        scope.launch {
                            repository.saveIncident(
                                IncidentEntity(
                                    incidentId = "VG-INC-${System.currentTimeMillis() % 100000}",
                                    callSessionId = "session-${System.currentTimeMillis()}",
                                    timestamp = System.currentTimeMillis(),
                                    callerNumber = callerNumber,
                                    callerName = callerName,
                                    riskScore = score,
                                    threatType = if (isReal) "Cellular Telephony Forensics" else scenario.attackType,
                                    language = if (isReal) "hi" else scenario.language,
                                    transcriptSummary = if (isReal && realStateVal != null) realStateVal.activeTranscript else simState.activeTranscript,
                                    forensicEvidenceJson = "[\"Real-time spectral analysis\", \"Harmonics variance F0 verification\"]",
                                    recommendedAction = "Forensic inspection complete. Real-time screening logged to Room DB."
                                )
                            )
                        }

                        navController.navigate(Screen.AttackReconstruction.route)
                    }
                )
            }

            composable(Screen.AttackReconstruction.route) {
                val scenario = simState.currentScenario ?: DemoAttackScenarios.scenarios[0]
                val score = simState.currentRiskScore?.overallScore ?: 91
                AttackReconstructionScreen(
                    scenario = scenario,
                    riskScore = score,
                    onViewIncidentReport = {
                        // Navigate to latest incident
                        allIncidents.firstOrNull()?.let {
                            selectedIncident = it
                            navController.navigate(Screen.IncidentDetail.route)
                        } ?: navController.navigate(Screen.CallHistory.route)
                    },
                    onBackToHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AttackLab.route) {
                AttackLabScreen(
                    realCallManager = realCallManager,
                    onBack = { navController.popBackStack() },
                    onNavigateIncomingCall = { navController.navigate(Screen.IncomingCall.route) }
                )
            }

            composable(Screen.ProtectedContacts.route) {
                ProtectedContactsScreen(
                    contacts = protectedContacts,
                    onSaveContact = { contact ->
                        scope.launch { repository.saveProtectedContact(contact) }
                    },
                    onDeleteContact = { contact ->
                        scope.launch { repository.deleteProtectedContact(contact) }
                    },
                    onSimulateIncomingCall = { scenario ->
                        simulatorEngine.startScenario(scenario, scope)
                        navController.navigate(Screen.IncomingCall.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CallHistory.route) {
                CallHistoryScreen(
                    incidents = allIncidents,
                    onSelectIncident = { incident ->
                        selectedIncident = incident
                        navController.navigate(Screen.IncidentDetail.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.IncidentDetail.route) {
                selectedIncident?.let { incident ->
                    IncidentDetailScreen(
                        incident = incident,
                        onBack = { navController.popBackStack() }
                    )
                } ?: run {
                    navController.popBackStack()
                }
            }

            composable(Screen.PrivacyCenter.route) {
                PrivacyCenterScreen(
                    prefs = prefs,
                    onWipeAllData = {
                        scope.launch { repository.purgeAllUserData() }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.EnterpriseApi.route) {
                EnterpriseApiScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showExplainDialog) {
        val score = simState.currentRiskScore
        ExplainabilityDialog(
            voiceRisk = score?.voiceRisk ?: 0.85f,
            speakerMismatch = score?.speakerMismatchRisk ?: 0.66f,
            onDismiss = { showExplainDialog = false }
        )
    }
}
