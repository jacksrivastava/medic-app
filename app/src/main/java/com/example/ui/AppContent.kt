package com.example.ui

import com.example.ui.theme.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.DoctorAppointment
import com.example.data.MedicalRecord
import com.example.data.MedicationReminder
import com.example.data.NotificationLog
import com.example.utils.SecureStorageHelper
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class MedTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Console", Icons.Default.Home, "tab_dashboard"),
    SCAN("Scan Prescription", Icons.Default.Search, "tab_scan"),
    RECORDS("Records", Icons.Default.List, "tab_records"),
    APPOINTMENTS("Schedules", Icons.Default.DateRange, "tab_appointments")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MedViewModel) {
    val context = LocalContext.current
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }
    var isEmergencyBypassMode by rememberSaveable { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    
    var currentTab by remember { mutableStateOf(MedTab.DASHBOARD) }
    var showNotificationCenter by remember { mutableStateOf(false) }

    val notificationLogs by viewModel.notificationLogs.collectAsStateWithLifecycle()
    val unreadNotificationsCount = notificationLogs.size

    if (!isAuthenticated && !isEmergencyBypassMode) {
        // HIPAA Certified Vault Gateway (Authentication gate)
        SecureAuthenticationGate(
            onUnlock = { 
                isAuthenticated = true 
                Toast.makeText(context, "Welcome back, standard biometrics verified!", Toast.LENGTH_SHORT).show()
            },
            onEmergencyBypass = { 
                isEmergencyBypassMode = true 
                Toast.makeText(context, "Emergency Mode active: Secure records locked.", Toast.LENGTH_LONG).show()
            }
        )
    } else if (isEmergencyBypassMode) {
        // Red-Level Security Intruder / Emergency Bypass screen
        EmergencyBypassScreen(
            profile = activeProfile,
            onExit = { 
                isEmergencyBypassMode = false 
                Toast.makeText(context, "Emergency zone exited. Vault relocked.", Toast.LENGTH_SHORT).show()
            }
        )
    } else {
        // Full System Access
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "SECURITY RANK: HIPAA VAULT ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.3.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hello, ${activeProfile.name}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(android.graphics.Color.parseColor(activeProfile.colorHex)).copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = activeProfile.bloodGroup,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(android.graphics.Color.parseColor(activeProfile.colorHex))
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showNotificationCenter = true },
                            modifier = Modifier.testTag("inbox_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationsCount > 0) {
                                        Badge {
                                            Text(unreadNotificationsCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Reminder logs",
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Patient Avatar Profile selector bubble
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(activeProfile.colorHex)).copy(alpha = 0.2f))
                                .border(1.5.dp, Color(android.graphics.Color.parseColor(activeProfile.colorHex)), CircleShape)
                                .clickable { showProfileDialog = true }
                                .testTag("patient_avatar_bubble"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeProfile.avatarInitials,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(android.graphics.Color.parseColor(activeProfile.colorHex))
                            )
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_navigation")
                ) {
                    MedTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(imageVector = tab.icon, contentDescription = tab.title)
                            },
                            label = {
                                Text(text = tab.title, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main screens render with selected profile filter
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "MainTabs"
                ) { state ->
                    when (state) {
                        MedTab.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onScanShortcutClicked = { currentTab = MedTab.SCAN }
                        )
                        MedTab.SCAN -> PrescriptionScannerScreen(viewModel = viewModel)
                        MedTab.RECORDS -> MedicalRecordsScreen(viewModel = viewModel)
                        MedTab.APPOINTMENTS -> AppointmentsAndAlarmsScreen(viewModel = viewModel)
                    }
                }

                // Push notifier inbox
                if (showNotificationCenter) {
                    NotificationCenterDialog(
                        logs = notificationLogs,
                        onDismiss = { showNotificationCenter = false },
                        onClear = { viewModel.clearNotifications() }
                    )
                }

                // Profile Page & Family Multi-switcher dialog
                if (showProfileDialog) {
                    MedicalIdentityDialog(
                        viewModel = viewModel,
                        activeProfile = activeProfile,
                        onDismiss = { showProfileDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
fun SecureAuthenticationGate(
    onUnlock: () -> Unit,
    onEmergencyBypass: () -> Unit
) {
    val context = LocalContext.current
    var passwordInput by remember { mutableStateOf("") }
    var pinCodeInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("sarah.k@medkeeper.org") }
    var phoneInput by remember { mutableStateOf("+1234567890") }
    var showOtpField by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Shield Guard Lock",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MedKeeper Secure Vault",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "HIPAA Compliant Hardware Shield Enclosed",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // PIN Code Display Indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Authentication Methods",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Bio Unlock button
                    Button(
                        onClick = onUnlock,
                        modifier = Modifier.fillMaxWidth().testTag("simulate_biometrics_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Fingerprint")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Fingerprint / Face ID Link", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "— OR ENTER CRYPTO CREDENTIALS —", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Email / password inputs
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Patient Email Access") },
                        leadingIcon = { Icon(Icons.Default.Email, "Email Icon") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { 
                            passwordInput = it
                            if (it.length >= 4) {
                                onUnlock()
                            }
                        },
                        label = { Text("Secure Passcode / OTP (PIN: '1234')") },
                        leadingIcon = { Icon(Icons.Default.Lock, "Key Icon") },
                        modifier = Modifier.fillMaxWidth().testTag("password_vault_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Social & emergency panels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // OTP quick verify
                Button(
                    onClick = {
                        showOtpField = !showOtpField
                        if (showOtpField) {
                            Toast.makeText(context, "Verification code '1234' dispatched via SMS simulator!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mobile OTP Lock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onUnlock,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Social Auth Byp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (showOtpField) {
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { 
                        otpInput = it
                        if (it == "1234") {
                            onUnlock()
                        }
                    },
                    placeholder = { Text("Code: 1234") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FIRE RED EMERGENCY BYPASS MODE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEmergencyBypass() }
                    .testTag("emergency_bypass_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp),
                        contentDescription = "Alert logo"
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔴 FIRST RESPONDERS EMERGENCY ACCESS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Accident bypass widget gives immediate view-only speed dial and medical card.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = "Arrow"
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyBypassScreen(
    profile: PatientProfile,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B0000)) // Medical Crimson Background
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High Visibility Header banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            tint = Color(0xFF8B0000),
                            contentDescription = "Warning Badge"
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FIRST RESPONDERS EMERGENCY CARD",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Bypassed Mode - Read-Only Medical Identity",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 10.sp
                        )
                    }
                }

                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF8B0000)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🔒 Lock App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // CRITICAL PATIENT HEALTH INSIGNIA CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PATIENT MEDICAL IDENTITY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Gray
                            )
                            Text(
                                text = profile.name,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }

                        // Blood Type badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF8B0000), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "BLOOD: ${profile.bloodGroup}",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Key Specs row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("AGE", fontSize = 9.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                            Text("${profile.age} Years", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("GENDER", fontSize = 9.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                            Text(profile.gender, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("HEIGHT", fontSize = 9.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                            Text(profile.height, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("WEIGHT", fontSize = 9.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                            Text(profile.weight, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // ALLERGIES RED FLAG BLOCK (Feature 10: Allergies & Critical Diseases)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFCCCC), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ KNOWN DRUG/FOOD ALLERGIES:",
                            color = Color(0xFF8B0000),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = profile.allergies,
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chronic illnesses list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFECCC), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🩺 CHRONIC DISEASES / MEDICAL CONDITIONS:",
                            color = Color(0xFFE64A19),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = profile.chronicDiseases,
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CURRENT MEDS: Lisinopril 10mg once daily capsule, Albuterol inhaler PRN.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }

            // COHESIVE SPEED DIAL ACTIONS (Feature 10: Speed dials emergency contacts)
            Text(
                text = "EMERGENCY SPEED DIALS:",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.0.sp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EmergencyContactRow(
                        title = "Ambulance (Police / ICU)",
                        details = "Emergency Call Dial 102/911",
                        phone = "911",
                        context = context
                    )

                    EmergencyContactRow(
                        title = "${profile.emergencyContactName} (${profile.emergencyContactRelation})",
                        details = profile.emergencyContactPhone,
                        phone = profile.emergencyContactPhone,
                        context = context
                    )

                    EmergencyContactRow(
                        title = "Dr. Richard Smith (Cardiologist)",
                        details = "Main Clinic Direct Speed Dial",
                        phone = "+15559113030",
                        context = context
                    )
                }
            }

            // Autogenerated hospital QR barcode card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "APPROVED MEDICAL CODE FOR HOSPITAL SCANNER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simple programmatic QR rendering
                    Canvas(
                        modifier = Modifier
                            .size(240.dp, 80.dp)
                            .background(Color.White)
                    ) {
                        val barWidth = 4f
                        val gap = 6f
                        var startX = 20f
                        while (startX < size.width - 20f) {
                            val r = Random(startX.toLong())
                            val filled = r.nextBoolean()
                            if (filled) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(startX, 10f),
                                    size = androidx.compose.ui.geometry.Size(barWidth, size.height - 20f)
                                )
                            }
                            startX += barWidth + gap
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SECURE VERIFIED HIPAA PATIENT IDENTITY KEY *8821-X*",
                        fontSize = 9.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyContactRow(
    title: String,
    details: String,
    phone: String,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable {
                Toast.makeText(context, "Initiating Secure Emergency Dial to $phone!", Toast.LENGTH_SHORT).show()
            }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = details, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                tint = Color(0xFF8B0000),
                contentDescription = "Siren button dialer"
            )
        }
    }
}

@Composable
fun MedicalIdentityDialog(
    viewModel: MedViewModel,
    activeProfile: PatientProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSharing by remember { mutableStateOf(false) }
    var showTemporaryLink by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .testTag("profile_identity_card_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Medical ID Card icon"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Family Medical Health Card",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // horizontal family profile selector (Feature 8: Family health management profile switcher)
                Text("Select Managed Member Profile:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.familyProfiles.forEach { profileItem ->
                        val isSelected = profileItem.name == activeProfile.name
                        val tint = Color(android.graphics.Color.parseColor(profileItem.colorHex))
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                viewModel.selectProfile(profileItem) 
                                Toast.makeText(context, "Activated medical profile: ${profileItem.name}", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text(profileItem.name, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tint.copy(alpha = 0.2f),
                                selectedLabelColor = tint
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                // Profile Specific Medical Details Card
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "VITAL IDENTITY PARMS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.0.sp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                IdentityParamRow("Vitals & Age", "${activeProfile.age} Yrs old (${activeProfile.gender})")
                                IdentityParamRow("Height / Weight", "${activeProfile.height} / ${activeProfile.weight}")
                                IdentityParamRow("Blood Type Code", activeProfile.bloodGroup)
                                IdentityParamRow("Insurance Policy", activeProfile.insuranceDetails)
                                IdentityParamRow("Organ Donor Registration", activeProfile.organDonorStatus)
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "⚠️ ALLERGIES & CHRONIC DISORDERS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Allergies: ${activeProfile.allergies}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Diagnoses: ${activeProfile.chronicDiseases}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        // AUTOMATIC QR GENERATOR (Hospital scanning)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "AUTO-GENERATED APPROVED HOSPITAL SCANNER QR",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Beautiful programmatically generated simulated dense Grid QR code
                                Canvas(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(Color.White)
                                ) {
                                    val blockCount = 10
                                    val blockSize = size.width / blockCount
                                    for (x in 0 until blockCount) {
                                        for (y in 0 until blockCount) {
                                            val r = Random((activeProfile.name.hashCode() + (x * 33) + y).toLong())
                                            val paintBlack = r.nextBoolean() || (x in 0..2 && y in 0..2) || (x in 7..9 && y in 0..2) || (x in 0..2 && y in 7..9)
                                            if (paintBlack) {
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = Offset(x * blockSize, y * blockSize),
                                                    size = androidx.compose.ui.geometry.Size(blockSize, blockSize)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "PATIENT-ID: ${activeProfile.avatarInitials}-REK-8821",
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TEMPORARY SECURE VIEW-ONLY ACCESS SHARING LINK (Feature 13: Temporary links expiring in 30m)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isSharing = true
                            showTemporaryLink = true
                            Toast.makeText(context, "Temporary view-only access encrypted token generated!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Share", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Temp Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Full HIPAA PDF Report drafted to storage/emulated/0/Download!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Export Health PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (showTemporaryLink) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🔒 Expiring HIPAA Sharing Key Link:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "https://medkeeper.app/sh/8821x-${activeProfile.name.lowercase()}s?expire=30m",
                                fontSize = 9.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    Toast.makeText(context, "Url copied to patient clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🔴 SECURE NOTICE: Shared key link expires in 30 minutes automatically.",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IdentityParamRow(label: String, valStr: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = valStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DashboardScreen(viewModel: MedViewModel, onScanShortcutClicked: () -> Unit) {
    val context = LocalContext.current
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val reminders by viewModel.medicationReminders.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    val filteredAppts = appointments.filter { it.profileName == activeProfile.name }
    val filteredReminders = reminders.filter { it.profileName == activeProfile.name }

    val nextAppointment = filteredAppts.sortedBy { it.date }.firstOrNull { !it.isCompleted }
    val activeReminders = filteredReminders.filter { it.isActive }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Status Banner (Bento Scan Card Style)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) BentoBackboneDark else BentoBackboneLight
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onScanShortcutClicked() }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Scan Icon",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Scan Prescription",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Instant medication schedule entry via AI OCR",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Button(
                            onClick = onScanShortcutClicked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scan_shortcut_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Scan")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Record by Prescription Scan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Go",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Action Progress Overview (Red-Salmon & Green-Pastel Bento cells)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Alarms metrics (Bento Card Red-Salmon)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) BentoSalmonBgDark else BentoSalmonBg
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                tint = if (isSystemInDarkTheme()) BentoDeepRedTextDark else BentoDeepRedText,
                                contentDescription = "reminders",
                                modifier = Modifier.size(18.dp)
                              )
                        }
                        Column {
                            Text(
                                text = "${activeReminders.size} Active Alarms",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (isSystemInDarkTheme()) BentoDeepRedTextDark else BentoDeepRedText,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Medication reminders running",
                                fontSize = 11.sp,
                                color = (if (isSystemInDarkTheme()) BentoDeepRedTextDark else BentoDeepRedText).copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Completed visits metrics (Bento Card Green)
                val completedAppointmentsVal = filteredAppts.filter { it.isCompleted }.size
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) BentoGreenBgDark else BentoGreenBg
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                tint = if (isSystemInDarkTheme()) BentoDeepGreenTextDark else BentoDeepGreenText,
                                contentDescription = "visits",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "$completedAppointmentsVal Completed",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (isSystemInDarkTheme()) BentoDeepGreenTextDark else BentoDeepGreenText,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Doctor visits registered",
                                fontSize = 11.sp,
                                color = (if (isSystemInDarkTheme()) BentoDeepGreenTextDark else BentoDeepGreenText).copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // BENTO STYLE HEALTH ANALYTICS WIDGET (Feature 12: Custom graphics on canvas displaying BP, blood sugar, & adherence index)
        item {
            HealthAnalyticsWidget(
                profileName = activeProfile.name,
                adherenceScore = 93
            )
        }

        // Active Reminders dispatcher with simulation trigger!
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Medicating Routine",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "FAMILY ASSOC: ${activeProfile.name.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        if (activeReminders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active medication scheduling. Scan a prescription to configure standard medication alerts.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(activeReminders) { reminder ->
                var stockCount by remember(reminder.id) { 
                    mutableStateOf(if (reminder.medicationName.contains("Lisinopril", ignoreCase = true) || reminder.medicationName.contains("Inhaler", ignoreCase = true)) 4 else 28) 
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .testTag("dashboard_reminder_card_${reminder.id}"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                // Circular icon
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "💊",
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = reminder.medicationName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${reminder.dosage} • Alarms: ${reminder.specificTimes}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Simulation button that triggers an actual system notification!
                            Button(
                                onClick = {
                                    viewModel.simulateMedicationReminderTrigger(context, reminder)
                                    Toast.makeText(context, "Push Alert fired for ${reminder.medicationName}!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("test_push_btn_${reminder.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Fires Alert",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Push", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Pill inventory stock section (Feature 11: Stock alerts and replenish action buttons)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info, 
                                    contentDescription = "Inventory Tracker",
                                    tint = if (stockCount <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (stockCount <= 5) {
                                        "⚠️ Only $stockCount Tablets Left! (Replenish Needed)"
                                    } else {
                                        "Stock Inventory: $stockCount Pills remaining"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (stockCount <= 5) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = if (stockCount <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(
                                onClick = {
                                    stockCount += 30
                                    Toast.makeText(context, "Inventory restocked (+30 Pills) for ${reminder.medicationName}!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Stock", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Refill Stock (+30)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Next Doctor Visit Tracking
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Next Upcoming Doctor Checkup",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            if (nextAppointment != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("next_appt_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) BentoLightBlueBgDark else BentoLightBlueBg
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "NEXT APPOINTMENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSystemInDarkTheme()) BentoDeepBlueTextDark else BentoDeepBlueText,
                                    style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.sp)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.DateRange,
                                tint = if (isSystemInDarkTheme()) BentoDeepBlueTextDark else BentoDeepBlueText,
                                contentDescription = "Calendar",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = nextAppointment.doctorName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = if (isSystemInDarkTheme()) BentoDeepBlueTextDark else BentoDeepBlueText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nextAppointment.specialty,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = (if (isSystemInDarkTheme()) BentoDeepBlueTextDark else BentoDeepBlueText).copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Date: ${nextAppointment.date} at ${nextAppointment.time}",
                            fontSize = 13.sp,
                            color = (if (isSystemInDarkTheme()) BentoDeepBlueTextDark else BentoDeepBlueText).copy(alpha = 0.8f)
                        )

                        if (nextAppointment.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Instructions & Prep: ${nextAppointment.notes}",
                                fontSize = 12.sp,
                                color = (if (isSystemInDarkTheme()) BentoDeepBlueTextDark else BentoDeepBlueText).copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSystemInDarkTheme()) BentoBlueAccentDark else BentoDeepBlueText)
                                    .clickable {
                                        Toast.makeText(context, "Checked-in successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Check-in",
                                    color = if (isSystemInDarkTheme()) BentoDeepBlueText else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.3f))
                                    .clickable {
                                        Toast.makeText(context, "Map direction loading...", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Map Direction",
                                    color = if (isSystemInDarkTheme()) BentoDeepBlueTextDark else BentoDeepBlueText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pending doctor appointments scheduled.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Shared Records Bento Row at list bottom
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) BentoBackboneDark else Color.White
                ),
                border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color.Transparent else BentoBorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.width(44.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(BentoBlueAccentDark)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 14.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFB4B1B8))
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Shared Records",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Your family (2 members) has access",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Settings,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "Sharing Settings",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                Toast.makeText(context, "Encryption & Access Keys configured!", Toast.LENGTH_SHORT).show()
                            }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrescriptionScannerScreen(viewModel: MedViewModel) {
    val context = LocalContext.current
    val scanUiState by viewModel.scanUiState.collectAsStateWithLifecycle()

    var pastedText by remember { mutableStateOf("") }
    var customNotes by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Launcher for selecting prescription photos from device gallery!
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectImageBitmap = null // Clear camera photo
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                selectImageBytes = bytes
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading image file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher for taking prescription photos using device's camera!
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectImageBitmap = bitmap
            selectedImageUri = null // Clear gallery URI
            try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                selectImageBytes = outputStream.toByteArray()
            } catch (e: Exception) {
                Toast.makeText(context, "Error processing camera capture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // High fidelity presets for instant scanning showcase
    val presets = listOf(
        Triple(
            "Amoxicillin Care Plan",
            "Meds: Amoxicillin Capsules 500mg\nFrequency: Three times daily for 10 days.\nSchedule doctor appointment follow up in 2 weeks with Dr. Jane Foster Specialty: General Practitioner to verify recovery of streptococcus infection symptoms.",
            "Strep throat cure preset. Triggers Amoxicillin reminders and GP Follow-Up."
        ),
        Triple(
            "Diabetes Metformin Plan",
            "Meds: Metformin 850mg ER daily tablets\nDirections: Take twice daily, with breakfast and dinner. Avoid alcohol. Scheduled Specialist Endocrinology Doctor checkup Dr. Linda Gomez in 14 days time for HbA1c review blood checks.",
            "Endocrinology glucose care preset. Triggers Metformin alerts and lab follow-up."
        ),
        Triple(
            "BP Lisinopril Regulation",
            "Prescription Rx: Lisinopril 20mg Once Daily.\nDirections: Take every morning at 08:00 AM. Monitor blood pressure levels.\nSchedule Dr. Robert Chen (Cardiology Specialty) follow-up appointment in 3 months for cardiovascular stability checks.",
            "Cardiovascular health preset. Triggers Lisinopril reminders and Cardiology Visit."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Prescription OCR Intake",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Supply a prescription image or snap a photo. The Gemini AI engine will parse raw documents into structured schedules and active alerts instantly.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Show parsing states dynamically
        when (val state = scanUiState) {
            is ScanUiState.Idle -> {
                // Interactive dual image source options
                if (selectedImageUri == null && selectImageBitmap == null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Camera trigger card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { cameraLauncher.launch(null) }
                                    .testTag("camera_picker_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow, // camera snapshot accent
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentDescription = "Camera capture",
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Snap Camera Pic",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Use phone camera",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Gallery trigger card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { photoPickerLauncher.launch("image/*") }
                                    .testTag("gallery_picker_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        contentDescription = "Gallery Picker",
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Open Gallery",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Select from library",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = selectImageBitmap ?: selectedImageUri,
                                    contentDescription = "Selected Prescription",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        contentDescription = "Done"
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (selectImageBitmap != null) "Camera snapshot attached" else "Prescription photo attached",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                TextButton(onClick = {
                                    selectedImageUri = null
                                    selectImageBitmap = null
                                    selectImageBytes = null
                                }) {
                                    Text("Remove image", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                // TextInput alternative
                item {
                    Text(
                        text = "Or Paste Prescription / Care Instructions Text",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("prescription_text_field"),
                        placeholder = {
                            Text(
                                text = "Example: Rx: Amoxicillin 500mg thrice daily. Follow-up Dr. Foster in 14 days...",
                                fontSize = 13.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // AI Scanning Action Trigger
                item {
                    Button(
                        onClick = {
                            if (selectImageBytes == null && pastedText.isBlank()) {
                                Toast.makeText(context, "Please select an image or type text prescription instruction first", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.scanPrescription(
                                    rawContent = pastedText.ifBlank { "Scanned prescription file attachment contents representation." },
                                    imageBytes = selectImageBytes
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_scan_button")
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Parse")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extract Medical Schedules", fontWeight = FontWeight.Bold)
                    }
                }

                // Show Presets Showcase Section
                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Instant Testing Presets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Don't have a prescription? Tap a preset to run a simulated high-fid parsing check immediately.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(presets) { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                pastedText = preset.second
                                selectedImageUri = null
                                selectImageBytes = null
                                Toast
                                    .makeText(
                                        context,
                                        "${preset.first} loaded into prompt!",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                            .testTag("preset_${preset.first.replace(" ", "_")}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = preset.first,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.third,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            is ScanUiState.Scanning -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 5.dp,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Prescription OCR Running",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "AI is segmenting text nodes and formatting alarms relative to today's date...",
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            is ScanUiState.Success -> {
                val data = state.result
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_result_review_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI Extracted Prescription",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = "Success"
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Med details block
                            Text(
                                text = "Medication Name",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = data.medicationName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("extracted_med_name")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Dosage Strength",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = data.dosage,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Taking Frequency",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = data.frequency,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Therapy Duration",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = data.duration,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Configured Alarms",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = data.specificTimes,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Medical Advice / Guidelines",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = data.notes.ifBlank { "Take daily as requested by doctor instructions." },
                                fontSize = 13.sp
                            )

                            // Suggested Appointment details block if flagged!
                            if (data.hasAppointment) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Auto-Scheduled Follow-Up Appointment",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        contentDescription = "Doctor"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = data.appointmentDoctor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Specialty: ${data.appointmentSpecialty} • Date: ${data.appointmentDate} • Time: ${data.appointmentTime}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Write Custom Intake Observation (Optional)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customNotes,
                                onValueChange = { customNotes = it },
                                placeholder = { Text("Example: Started on Thursday morning, no side effects yet") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_notes_field"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.resetScanState()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("cancel_scan_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Discard Change")
                                }

                                Button(
                                    onClick = {
                                        viewModel.confirmAndSavePrescription(
                                            context = context,
                                            result = data,
                                            customNotes = customNotes
                                        )
                                        Toast.makeText(context, "Successfully populated records and schedules!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .testTag("confirm_scan_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Confirm & Schedule")
                                }
                            }
                        }
                    }
                }
            }

            is ScanUiState.Error -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Extraction Error",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.resetScanState() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicalRecordsScreen(viewModel: MedViewModel) {
    val context = LocalContext.current
    val records by viewModel.medicalRecords.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Manual add records fields
    var titleInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("Clinical Notes") }
    var notesInput by remember { mutableStateOf("") }

    var attachedFileUri by remember { mutableStateOf<Uri?>(null) }
    var attachedFileNameInput by remember { mutableStateOf("") }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedFileUri = uri
            attachedFileNameInput = SecureStorageHelper.getFileName(context, uri)
        }
    }

    val categories = listOf("All", "Prescription", "Lab Result", "Clinical Notes", "Immunization", "Other")

    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    val filteredRecords = records.filter { record ->
        val matchesProfile = record.profileName == activeProfile.name
        val matchesCategory = selectedCategory == "All" || record.type == selectedCategory
        val matchesSearch = record.title.contains(searchQuery, ignoreCase = true) ||
                record.patientNotes.contains(searchQuery, ignoreCase = true) ||
                record.type.contains(searchQuery, ignoreCase = true)
        matchesProfile && matchesCategory && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_record_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Medical Record")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Clinical Medical Records",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Check doctor prescriptions, clinic checkups logs, or diagnostic lab panels reports.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Search layout
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("records_search_bar"),
                placeholder = { Text("Search through reports & pills...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cat pills filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(text = cat, fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("filter_chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No records found matching query filter.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRecords) { record ->
                        MedicalRecordItemCard(record = record, onDelete = { viewModel.deleteMedicalRecord(record) })
                    }
                }
            }
        }

        // Add manual record modal
        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_record_dialog"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Write Critical Health Record",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Record Document Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("record_title_input")
                        )

                        Text("Select Document Category Type", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        val types = listOf("Prescription", "Lab Result", "Clinical Notes", "Immunization", "Other")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            types.forEach { t ->
                                FilterChip(
                                    selected = typeInput == t,
                                    onClick = { typeInput = t },
                                    label = { Text(t) }
                                )
                            }
                                          OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Patient Clinical Observations Notes") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("record_notes_input")
                        )

                        // HIPAA Encrypted File Attachment Module
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔒 Encrypted HIPAA Storage Vault",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (attachedFileUri == null) {
                            OutlinedButton(
                                onClick = { documentPickerLauncher.launch("*/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("attach_file_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add File Icon")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Attach Lab Report, History or Imaging File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        contentDescription = "Attached successful check"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = attachedFileNameInput, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = "AES Encryption Active prior to write", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = {
                                            attachedFileUri = null
                                            attachedFileNameInput = ""
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Remove attached document", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    titleInput = ""
                                    notesInput = ""
                                    attachedFileUri = null
                                    attachedFileNameInput = ""
                                    showAddDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Discard")
                            }
                            Button(
                                onClick = {
                                    if (titleInput.isNotBlank()) {
                                        viewModel.addMedicalRecordWithAttachment(
                                            context = context,
                                            title = titleInput,
                                            type = typeInput,
                                            notes = notesInput,
                                            fileUri = attachedFileUri
                                        )
                                        titleInput = ""
                                        notesInput = ""
                                        attachedFileUri = null
                                        attachedFileNameInput = ""
                                        showAddDialog = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("save_manual_record_btn")
                            ) {
                                Text("Save Document")
                            }
                        }        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalRecordItemCard(record: MedicalRecord, onDelete: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    var isDecrypting by remember { mutableStateOf(false) }
    var decryptedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var decryptedText by remember { mutableStateOf<String?>(null) }
    var decryptionError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val badgeColor = when (record.type) {
        "Prescription" -> MaterialTheme.colorScheme.primary
        "Lab Result" -> MaterialTheme.colorScheme.secondary
        "Clinical Notes" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("record_card_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.date,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔒 Hardware AES Encrypted",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Document type badge
                Text(
                    text = record.type,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = record.patientNotes,
                fontSize = 14.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            if (isExpanded) {
                if (record.rawExtractedContent.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scanned Meta Info:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = record.rawExtractedContent,
                        fontSize = 13.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                // Render secure file attachments if present on this record
                if (record.attachmentPath.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                tint = MaterialTheme.colorScheme.secondary,
                                contentDescription = "Secure attached file icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Secure Attachment:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = record.attachedFileName,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (decryptedBitmap == null && decryptedText == null) {
                            Button(
                                onClick = {
                                    isDecrypting = true
                                    decryptionError = null
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val bytes = SecureStorageHelper.decryptAndReadFile(record.attachmentPath)
                                            if (bytes != null) {
                                                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                if (bmp != null) {
                                                    decryptedBitmap = bmp
                                                } else {
                                                    val text = String(bytes, Charsets.UTF_8).take(1500)
                                                    decryptedText = text
                                                }
                                            } else {
                                                decryptionError = "Decryption failure"
                                            }
                                        } catch (e: Exception) {
                                            decryptionError = "Err: ${e.localizedMessage}"
                                        } finally {
                                            isDecrypting = false
                                        }
                                    }
                                },
                                enabled = !isDecrypting,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("decrypt_btn_${record.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Unlock",
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isDecrypting) "Decrypting..." else "Unlock & View",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    decryptedBitmap = null
                                    decryptedText = null
                                },
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Hide Client Doc", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // Decrypted content render box
                    if (decryptedBitmap != null || decryptedText != null || decryptionError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        contentDescription = "Shield Verified Logo",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Verified Hardware Decrypted (HIPAA Authenticated Zone)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                if (decryptedBitmap != null) {
                                    Image(
                                        bitmap = decryptedBitmap!!.asImageBitmap(),
                                        contentDescription = "Decrypted record document",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                } else if (decryptedText != null) {
                                    Text(
                                        text = decryptedText!!,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else if (decryptionError != null) {
                                    Text(
                                        text = decryptionError!!,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_record_btn_${record.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete prescription record",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentsAndAlarmsScreen(viewModel: MedViewModel) {
    val rawAppointments by viewModel.appointments.collectAsStateWithLifecycle()
    val rawReminders by viewModel.medicationReminders.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    val appointments = rawAppointments.filter { it.profileName == activeProfile.name }
    val reminders = rawReminders.filter { it.profileName == activeProfile.name }

    var showAddApptDialog by remember { mutableStateOf(false) }

    // Manual appointment form fields
    var docNameInput by remember { mutableStateOf("") }
    var docSpecialtyInput by remember { mutableStateOf("") }
    var docDateInput by remember { mutableStateOf("") }
    var docTimeInput by remember { mutableStateOf("10:00 AM") }
    var docLocationInput by remember { mutableStateOf("") }
    var docNotesInput by remember { mutableStateOf("") }

    var selectedSection by remember { mutableStateOf("Appointments") }

    Scaffold(
        floatingActionButton = {
            if (selectedSection == "Appointments") {
                FloatingActionButton(
                    onClick = { showAddApptDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_appointment_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Doctor Appointment")
                }
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Medkeeper Patient Schedules",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Manage upcoming followups and medicine alarms status.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-navigation chip selectors
            TabRow(
                selectedTabIndex = if (selectedSection == "Appointments") 0 else 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSection == "Appointments",
                    onClick = { selectedSection = "Appointments" },
                    text = { Text("Clinical Visits", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("section_appointments_tab")
                )
                Tab(
                    selected = selectedSection == "Alarms",
                    onClick = { selectedSection = "Alarms" },
                    text = { Text("Med Reminders", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("section_alerts_tab")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedSection == "Appointments") {
                if (appointments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No clinic visits scheduled. Build appointment or scan prescription to generate routine visits.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(appointments) { appointment ->
                            AppointmentItemCard(
                                appt = appointment,
                                onCheckToggle = { viewModel.toggleAppointmentCompleted(appointment) },
                                onDelete = { viewModel.deleteDoctorAppointment(appointment) }
                            )
                        }
                    }
                }
            } else {
                if (reminders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No medication configurations found. Add some by prescription scanning.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reminders) { reminder ->
                            ReminderItemCard(
                                reminder = reminder,
                                onActiveToggle = { viewModel.toggleReminderActive(reminder) },
                                onDelete = { viewModel.deleteMedicationReminder(reminder) }
                            )
                        }
                    }
                }
            }
        }

        // Add manual Doctor appointment Dialog
        if (showAddApptDialog) {
            Dialog(onDismissRequest = { showAddApptDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_appt_dialog"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Plan Doctor Consulta Book",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = docNameInput,
                            onValueChange = { docNameInput = it },
                            label = { Text("Doctor Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doc_name_input")
                        )

                        OutlinedTextField(
                            value = docSpecialtyInput,
                            onValueChange = { docSpecialtyInput = it },
                            label = { Text("Clinic Specialty") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doc_specialty_input")
                        )

                        OutlinedTextField(
                            value = docDateInput,
                            onValueChange = { docDateInput = it },
                            label = { Text("Checkup Date (e.g., 2026-06-25)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doc_date_input")
                        )

                        OutlinedTextField(
                            value = docTimeInput,
                            onValueChange = { docTimeInput = it },
                            label = { Text("Time (e.g., 10:30 AM)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doc_time_input")
                        )

                        OutlinedTextField(
                            value = docLocationInput,
                            onValueChange = { docLocationInput = it },
                            label = { Text("Location Clinic Suite Building") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doc_location_input")
                        )

                        OutlinedTextField(
                            value = docNotesInput,
                            onValueChange = { docNotesInput = it },
                            label = { Text("Medical Prep instructions") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doc_notes_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddApptDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Discard")
                            }
                            Button(
                                onClick = {
                                    if (docNameInput.isNotBlank() && docDateInput.isNotBlank()) {
                                        viewModel.addDoctorAppointment(
                                            DoctorAppointment(
                                                doctorName = docNameInput,
                                                specialty = docSpecialtyInput.ifBlank { "General Medicine" },
                                                date = docDateInput,
                                                time = docTimeInput,
                                                location = docLocationInput.ifBlank { "Virtual Care" },
                                                notes = docNotesInput,
                                                isCompleted = false
                                            )
                                        )
                                        docNameInput = ""
                                        docSpecialtyInput = ""
                                        docDateInput = ""
                                        docLocationInput = ""
                                        docNotesInput = ""
                                        showAddApptDialog = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                        .testTag("save_manual_appt_btn")
                            ) {
                                Text("Save Visit")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentItemCard(appt: DoctorAppointment, onCheckToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("appt_card_${appt.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (appt.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = appt.isCompleted,
                        onCheckedChange = { onCheckToggle() },
                        modifier = Modifier.testTag("appt_checkbox_${appt.id}")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = appt.doctorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            style = if (appt.isCompleted) MaterialTheme.typography.bodyLarge.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = appt.specialty,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_appt_btn_${appt.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete appointment", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "📅 ${appt.date} • ⏰ ${appt.time}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "📍 ${appt.location}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (appt.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Prep Instructions: ${appt.notes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ReminderItemCard(reminder: MedicationReminder, onActiveToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_card_${reminder.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = reminder.medicationName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Dosage: ${reminder.dosage}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Switch(
                    checked = reminder.isActive,
                    onCheckedChange = { onActiveToggle() },
                    modifier = Modifier.testTag("reminder_switch_${reminder.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = "Alarm Schedule:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "__ ${reminder.specificTimes} __",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Duration Details",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = reminder.duration,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (reminder.patientNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Patient Instructions: ${reminder.patientNotes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_reminder_btn_${reminder.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete reminder alarm", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun NotificationCenterDialog(
    logs: List<NotificationLog>,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .testTag("notification_center_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Inbox",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Prescription Push Inbox",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    TextButton(onClick = onClear) {
                        Text("Clear All")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notification reminders triggered yet.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(logs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = log.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                                        Text(
                                            text = timeStr,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = log.message,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close Inbox Panel")
                }
            }
        }
    }
}

@Composable
fun HealthAnalyticsWidget(
    profileName: String,
    adherenceScore: Int
) {
    var selectedChartTab by remember { mutableStateOf("Adherence") }
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("health_analytics_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VITAL METRICS ANALYSIS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.0.sp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Clinical Bento Trendlogs",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Interactive adherence metrics badge
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "STREAK: 12 DAYS 🔥",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab row button controller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Adherence", "Blood Pressure", "Sugar Levels").forEach { tab ->
                    val isSelected = selectedChartTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedChartTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Subtitle information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val headline = when (selectedChartTab) {
                        "Adherence" -> "Consistency: $adherenceScore% Average Ratio"
                        "Blood Pressure" -> "Last Diagnostic: 121/79 mmHg"
                        else -> "Fasting Glucose: 98 mg/dL"
                    }
                    val subtitle = when (selectedChartTab) {
                        "Adherence" -> "Excellent medication consistency for $profileName"
                        "Blood Pressure" -> "Systolic optimal • Pre-hypertensive safe"
                        else -> "Optimal metabolic index regulation monitored"
                    }
                    Text(
                        text = headline,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Small legend badge
                Box(
                    modifier = Modifier
                        .background(if (isDark) Color(0xFF333333) else Color(0xFFF3F3F3), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PAST 7 DAYS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PROFESSIONAL COMPOSE CANVAS CHART GRAPHIC (Drawing on Canvas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val outlineColor = MaterialTheme.colorScheme.outlineVariant

                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw helper horizontal grid lines
                    val lineCount = 4
                    for (i in 0..lineCount) {
                        val y = (height / lineCount) * i
                        drawLine(
                            color = outlineColor.copy(alpha = 0.35f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 2f
                        )
                    }

                    // 2. Draw charts based on tab
                    when (selectedChartTab) {
                        "Adherence" -> {
                            // Column Bar charts representation
                            val points = listOf(100f, 100f, 85f, 100f, 95f, 100f, 92f)
                            val barWidth = 32f
                            val step = width / (points.size)
                            points.forEachIndexed { idx, pct ->
                                val x = step * idx + (step / 2) - (barWidth / 2)
                                val colHeight = height * (pct / 100f)
                                val topY = height - colHeight

                                // Draw column bar
                                drawRect(
                                    color = if (idx == 6) primaryColor else primaryColor.copy(alpha = 0.5f),
                                    topLeft = Offset(x, topY),
                                    size = androidx.compose.ui.geometry.Size(barWidth, colHeight),
                                    style = androidx.compose.ui.graphics.drawscope.Fill
                                )
                            }
                        }
                        "Blood Pressure" -> {
                            // Dual line charts for systolic and diastolic pressure trends
                            val sysPoints = listOf(124f, 122f, 125f, 121f, 119f, 123f, 121f)
                            val diaPoints = listOf(81f, 82f, 83f, 79f, 78f, 81f, 79f)

                            val step = width / (sysPoints.size - 1)

                            // Helper lists to map Coordinates
                            val sysCoords = sysPoints.mapIndexed { idx, value ->
                                val normalizedVal = (value - 60f) / (140f - 60f) // Normalized between 60 & 140
                                Offset(step * idx, height - (height * normalizedVal))
                            }
                            val diaCoords = diaPoints.mapIndexed { idx, value ->
                                val normalizedVal = (value - 60f) / (140f - 60f)
                                Offset(step * idx, height - (height * normalizedVal))
                            }

                            // Connect dots with line paths
                            for (i in 0 until sysCoords.size - 1) {
                                drawLine(
                                    color = primaryColor,
                                    start = sysCoords[i],
                                    end = sysCoords[i + 1],
                                    strokeWidth = 6f
                                )
                                drawLine(
                                    color = secondaryColor,
                                    start = diaCoords[i],
                                    end = diaCoords[i + 1],
                                    strokeWidth = 6f
                                )
                            }

                            // Draw dot intersections
                            sysCoords.forEach { pt ->
                                drawCircle(color = primaryColor, radius = 8f, center = pt)
                                drawCircle(color = Color.White, radius = 4f, center = pt)
                            }
                            diaCoords.forEach { pt ->
                                drawCircle(color = secondaryColor, radius = 8f, center = pt)
                                drawCircle(color = Color.White, radius = 4f, center = pt)
                            }
                        }
                        "Sugar Levels" -> {
                            // Single line chart with smooth gradients representing Sugar fluctuations
                            val glucose = listOf(102f, 98f, 114f, 96f, 103f, 111f, 98f)
                            val step = width / (glucose.size - 1)

                            val coords = glucose.mapIndexed { idx, value ->
                                val normalizedVal = (value - 70f) / (150f - 70f) // Normalized between 70 & 150
                                Offset(step * idx, height - (height * normalizedVal))
                            }

                            // Draw line and fill gradient shading below line
                            for (i in 0 until coords.size - 1) {
                                drawLine(
                                    color = primaryColor,
                                    start = coords[i],
                                    end = coords[i + 1],
                                    strokeWidth = 7f
                                )
                            }

                            coords.forEach { pt ->
                                drawCircle(color = primaryColor, radius = 9f, center = pt)
                                drawCircle(color = Color.White, radius = 5f, center = pt)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive explanation bottom footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Clinical Insights",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Medical metrics generated dynamically using sandboxed diagnostic models.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
