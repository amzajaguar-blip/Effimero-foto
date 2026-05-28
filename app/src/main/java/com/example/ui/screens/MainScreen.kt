package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BurnEntity
import com.example.data.model.MonumentBlockEntity
import com.example.data.model.UserEntity
import com.example.ui.AshesViewModel
import com.example.ui.BurnStage
import com.example.ui.FriendSinner
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.ui.AshAuction
import com.example.ui.AuctionNotification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AshesViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.userFlow.collectAsState(initial = null)
    val burns by viewModel.burnsFlow.collectAsState(initial = emptyList())
    val blocks by viewModel.monumentBlocksFlow.collectAsState(initial = emptyList())
    val burnState by viewModel.burnState.collectAsState(initial = BurnStage.Idle)
    val friends by viewModel.cemeteryFriends.collectAsState(initial = emptyList())
    val pvpAlert by viewModel.pvpAlert.collectAsState(initial = null)
    val auctions by viewModel.auctionsFlow.collectAsState(initial = emptyList())
    val auctionNotifications by viewModel.auctionNotificationsFlow.collectAsState(initial = emptyList())

    var activeTab by remember { mutableStateOf("pyre") }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBlack),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp, start = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "ACOLYTE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.8.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = AshGrey
                            )
                            Text(
                                user?.username ?: "Nox_Arcana",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Light,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                color = BoneWhite
                            )
                        }

                        // Balance block
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "BALANCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.8.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = AshGrey
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "${user?.ashBalance ?: 0}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 18.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = PhoenixOrange
                                )
                                // Pulsing/glowing dot
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PhoenixOrange)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CarbonBlack),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CarbonBlack,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = activeTab == "pyre",
                    onClick = { activeTab = "pyre" },
                    icon = { Icon(Icons.Default.Delete, contentDescription = "Pyre") },
                    label = { Text("PYRE", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PhoenixOrange,
                        selectedTextColor = PhoenixOrange,
                        unselectedIconColor = AshGrey,
                        unselectedTextColor = AshGrey,
                        indicatorColor = Charcoal
                    ),
                    modifier = Modifier.testTag("nav_pyre")
                )
                NavigationBarItem(
                    selected = activeTab == "monument",
                    onClick = { activeTab = "monument" },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Monument") },
                    label = { Text("MONUMENT", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PhoenixOrange,
                        selectedTextColor = PhoenixOrange,
                        unselectedIconColor = AshGrey,
                        unselectedTextColor = AshGrey,
                        indicatorColor = Charcoal
                    ),
                    modifier = Modifier.testTag("nav_monument")
                )
                NavigationBarItem(
                    selected = activeTab == "auction",
                    onClick = { activeTab = "auction" },
                    icon = { Icon(Icons.Default.Gavel, contentDescription = "Auctions") },
                    label = { Text("AUCTION", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PhoenixOrange,
                        selectedTextColor = PhoenixOrange,
                        unselectedIconColor = AshGrey,
                        unselectedTextColor = AshGrey,
                        indicatorColor = Charcoal
                    ),
                    modifier = Modifier.testTag("nav_auction")
                )
                NavigationBarItem(
                    selected = activeTab == "cemetery",
                    onClick = { activeTab = "cemetery" },
                    icon = { Icon(Icons.Default.Face, contentDescription = "Cemetery") },
                    label = { Text("CEMETERY", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PhoenixOrange,
                        selectedTextColor = PhoenixOrange,
                        unselectedIconColor = AshGrey,
                        unselectedTextColor = AshGrey,
                        indicatorColor = Charcoal
                    ),
                    modifier = Modifier.testTag("nav_cemetery")
                )
                NavigationBarItem(
                    selected = activeTab == "shop",
                    onClick = { activeTab = "shop" },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Offerings") },
                    label = { Text("OFFERING", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PhoenixOrange,
                        selectedTextColor = PhoenixOrange,
                        unselectedIconColor = AshGrey,
                        unselectedTextColor = AshGrey,
                        indicatorColor = Charcoal
                    ),
                    modifier = Modifier.testTag("nav_shop")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->

        // PVP Popups / Alerts
        if (pvpAlert != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissPvpAlert() },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissPvpAlert() }) {
                        Text("SO BE IT", color = PhoenixOrange, style = MaterialTheme.typography.labelLarge)
                    }
                },
                title = {
                    Text(
                        "RITUAL TRANSMUTATION",
                        style = MaterialTheme.typography.titleLarge,
                        color = PhoenixOrange
                    )
                },
                text = {
                    Text(
                        pvpAlert ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BoneWhite
                    )
                },
                containerColor = Charcoal,
                textContentColor = BoneWhite,
                titleContentColor = PhoenixOrange,
                shape = RoundedCornerShape(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CarbonBlack)
        ) {
            when (activeTab) {
                "pyre" -> PyreScreen(viewModel = viewModel, user = user, burnState = burnState, burns = burns)
                "monument" -> MonumentScreen(viewModel = viewModel, user = user, blocks = blocks)
                "auction" -> AuctionScreen(viewModel = viewModel, user = user, auctions = auctions, notifications = auctionNotifications)
                "cemetery" -> CemeteryScreen(viewModel = viewModel, user = user, friends = friends)
                "shop" -> SettingsShopScreen(viewModel = viewModel, user = user)
            }
        }
    }
}

@Composable
fun PyreScreen(
    viewModel: AshesViewModel,
    user: UserEntity?,
    burnState: BurnStage,
    burns: List<BurnEntity>
) {
    AnimatedContent(
        targetState = burnState,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
        },
        label = "RitualTransition"
    ) { state ->
        when (state) {
            is BurnStage.Idle -> PyreIdleView(viewModel, user, burns)
            is BurnStage.Scoring -> PyreScoringLoadingView()
            is BurnStage.ScoringDone -> PyreScoringDoneView(viewModel, state, user)
            is BurnStage.Burning -> PyreBurningAnimationView(state.progress)
            is BurnStage.Burnt -> PyreBurntSummaryView(viewModel, state)
        }
    }
}

@Composable
fun PyreIdleView(
    viewModel: AshesViewModel,
    user: UserEntity?,
    burns: List<BurnEntity>
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf("photo") }
    var ageDays by remember { mutableStateOf("30") }
    var fileSizeKb by remember { mutableStateOf("1500") }
    var isTrueBurn by remember { mutableStateOf(true) }

    var expandedMedia by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Section: Concentric alignment lines & Temple Archway centerpiece
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Charcoal.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Orbit background lines pattern
                OrbitBackground()

                // Center Archway
                val monumentCategory = when (user?.monumentTier ?: 0) {
                    0 -> "Rubble Pyre"
                    1 -> "Level I Sanctuary"
                    2 -> "Level II Scriptorium"
                    3 -> "Level III Pillar"
                    4 -> "Level IV Altar"
                    else -> "Tier ${user?.monumentTier ?: 0} Cathedral"
                }
                TempleArchway(
                    title = monumentCategory,
                    subtitle = "Transmutation imminent"
                )

                // Bottom row: Streak indicators on left, Decay status on right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    SegmentedStreakBar(
                        currentStreak = user?.currentStreak ?: 0,
                        modifier = Modifier.width(140.dp)
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "DECAY RISK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = AshGrey
                        )
                        val multiplier = when {
                            (user?.currentStreak ?: 0) == 0 -> "CRITICAL"
                            (user?.currentStreak ?: 0) < 3 -> "HIGH"
                            else -> "LOW"
                        }
                        Text(
                            text = multiplier,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (multiplier == "CRITICAL") Color.Red else PhoenixOrange
                            )
                        )
                    }
                }
            }
        }

        // Selection panel - styled precisely based on Design HTML's action card:
        // bg-[#1A1A1A] rounded-3xl p-6 border border-white/5 shadow-2xl
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Charcoal),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "The fire is lit.\nWhat will you feed it?",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Light,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = BoneWhite,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        "MEMORABILIA DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = PhoenixOrange
                    )

                    // Media select dropdown
                    Box {
                        OutlinedButton(
                            onClick = { expandedMedia = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("select_media_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BoneWhite),
                            border = BorderStroke(1.dp, AshGrey.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Type: ${mediaType.uppercase(Locale.ROOT)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = AshGrey)
                            }
                        }

                        DropdownMenu(
                            expanded = expandedMedia,
                            onDismissRequest = { expandedMedia = false },
                            modifier = Modifier.background(Charcoal)
                        ) {
                            listOf("photo", "voice", "text", "video").forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.uppercase(Locale.ROOT), color = BoneWhite) },
                                    onClick = {
                                        mediaType = m
                                        expandedMedia = false
                                    }
                                )
                            }
                        }
                    }

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title of memory (e.g., 'Ex boyfriend selfie')", color = AshGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhoenixOrange,
                            unfocusedBorderColor = AshGrey.copy(alpha = 0.3f),
                            focusedTextColor = BoneWhite,
                            unfocusedTextColor = BoneWhite,
                            focusedLabelColor = PhoenixOrange,
                            unfocusedLabelColor = AshGrey
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("title_input")
                    )

                    // Details
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Details or transcript (describe what they said/did...)", color = AshGrey) },
                        placeholder = { Text("Mention emotional terms to increase ML yield", color = AshGrey.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhoenixOrange,
                            unfocusedBorderColor = AshGrey.copy(alpha = 0.3f),
                            focusedTextColor = BoneWhite,
                            unfocusedTextColor = BoneWhite,
                            focusedLabelColor = PhoenixOrange,
                            unfocusedLabelColor = AshGrey
                        ),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("details_input")
                    )

                    // File age days / size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = ageDays,
                            onValueChange = { ageDays = it },
                            label = { Text("File Age (days)", color = AshGrey) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhoenixOrange,
                                unfocusedBorderColor = AshGrey.copy(alpha = 0.3f),
                                focusedTextColor = BoneWhite,
                                unfocusedTextColor = BoneWhite,
                                focusedLabelColor = PhoenixOrange,
                                unfocusedLabelColor = AshGrey
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("age_input")
                        )

                        OutlinedTextField(
                            value = fileSizeKb,
                            onValueChange = { fileSizeKb = it },
                            label = { Text("File Size (KB)", color = AshGrey) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhoenixOrange,
                                unfocusedBorderColor = AshGrey.copy(alpha = 0.3f),
                                focusedTextColor = BoneWhite,
                                unfocusedTextColor = BoneWhite,
                                focusedLabelColor = PhoenixOrange,
                                unfocusedLabelColor = AshGrey
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("size_input")
                        )
                    }

                    // True Burn confirmation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isTrueBurn,
                            onCheckedChange = { isTrueBurn = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PhoenixOrange,
                                uncheckedColor = AshGrey
                            )
                        )
                        Text(
                            "True Burn: Delete permanently from cloud",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BoneWhite
                        )
                    }

                    // SACRIFICE FILE button directly styled like design HTML:
                    // bg-[#FF4D00] text-[#0A0A0A] rounded-2xl font-bold uppercase tracking-widest text-sm h-14
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.initiateBurn(
                                    mediaType = mediaType,
                                    title = title,
                                    details = details,
                                    fileSizeKb = fileSizeKb.toIntOrNull() ?: 1500,
                                    ageDays = ageDays.toIntOrNull() ?: 30,
                                    isTrueBurn = isTrueBurn
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PhoenixOrange,
                            contentColor = CarbonBlack,
                            disabledContainerColor = AshGrey.copy(alpha = 0.3f),
                            disabledContentColor = AshGrey
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("initiate_burn_button")
                    ) {
                        Text(
                            "SACRIFICE FILE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }

        // Recent Sacrifices logs
        if (burns.isNotEmpty()) {
            item {
                Text(
                    "RECENT SACRIFICES",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                    color = AshGrey,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            items(burns.take(5)) { burn ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Charcoal.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (burn.mediaType) {
                                "photo" -> Icons.Default.Star
                                "voice" -> Icons.Default.Info
                                "text" -> Icons.Default.Edit
                                else -> Icons.Default.PlayArrow
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CarbonBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = "Media type", tint = PhoenixOrange)
                            }

                            Column {
                                Text(
                                    burn.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = BoneWhite
                                )
                                Text(
                                    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(burn.burnedAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AshGrey
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "+${burn.ashEarned} ASH",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                color = PhoenixOrange
                            )
                            Text(
                                "Streak ${burn.streakMultiplier}x",
                                style = MaterialTheme.typography.labelSmall,
                                color = AshGrey
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PyreScoringLoadingView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing ember ring
        val infiniteTransition = rememberInfiniteTransition(label = "ringRef")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "angleRot"
        )

        Canvas(modifier = Modifier.size(100.dp)) {
            // Draw ritual orbit line
            drawCircle(
                color = AshGrey.copy(alpha = 0.2f),
                radius = size.width / 2f,
                style = Stroke(width = 4.dp.toPx())
            )
            // Draw burning ember progress
            drawArc(
                color = PhoenixOrange,
                startAngle = angle,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx())
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            "THE PRIEST IS WEIGHING YOUR MEMORY...",
            style = MaterialTheme.typography.titleLarge,
            color = BoneWhite,
            textAlign = TextAlign.Center
        )
        Text(
            "Analyzing file age, cloud weights, and emotional depth indices locally & via digital pyre layers.",
            style = MaterialTheme.typography.bodyLarge,
            color = AshGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun PyreScoringDoneView(
    viewModel: AshesViewModel,
    stage: BurnStage.ScoringDone,
    user: UserEntity?
) {
    // Exact Formula matching:
    val multiplier = when {
        (user?.currentStreak ?: 0) == 0 -> 1.0
        (user?.currentStreak ?: 0) < 3 -> 1.5
        (user?.currentStreak ?: 0) < 7 -> 2.0
        (user?.currentStreak ?: 0) < 14 -> 2.5
        else -> 3.0
    }

    val baseAsh = 10
    val ageBonus = if (stage.ageDays >= 1095) 50 else if (stage.ageDays >= 365) 25 else 0
    val sizeBonus = if (stage.fileSizeKb >= 10240) 15 else (stage.fileSizeKb / 1024).coerceIn(0, 20)
    
    val hasEmotionalKeyword = stage.details.uppercase().contains("LOVE") || 
            stage.details.uppercase().contains("MOM") || 
            stage.details.uppercase().contains("DAD") || 
            stage.details.uppercase().contains("BABY") || 
            stage.details.uppercase().contains("SORRY")
    val facesDetected = stage.details.uppercase().contains("FACE") || stage.details.uppercase().contains("PORTRAIT")
    val expectedEarnedRaw = (baseAsh + ageBonus + sizeBonus + (if (facesDetected) 30 else 0) + (if (hasEmotionalKeyword) 20 else 0)) * multiplier
    val estimAsh = expectedEarnedRaw.toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "THE PRICING OF LOSS",
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
            color = BoneWhite,
            textAlign = TextAlign.Center
        )

        Text(
            "We have quantified the emotional and physical weight of your memory.",
            style = MaterialTheme.typography.bodyLarge,
            color = AshGrey,
            textAlign = TextAlign.Center
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Charcoal),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "WEIGH DATA SUMMARY",
                    style = MaterialTheme.typography.labelMedium,
                    color = PhoenixOrange
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Base Yield", color = AshGrey)
                    Text("$baseAsh Ash", color = BoneWhite, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Clutter Age (${stage.ageDays} days)", color = AshGrey)
                    Text("+$ageBonus Ash", color = BoneWhite, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Clutter Size (${stage.fileSizeKb} KB)", color = AshGrey)
                    Text("+$sizeBonus Ash", color = BoneWhite, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Emotional Weight (Faces/Clues)", color = AshGrey)
                    val emotivePlus = (if (facesDetected) 30 else 0) + (if (hasEmotionalKeyword) 20 else 0)
                    Text("+$emotivePlus Ash", color = BoneWhite, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Streak Multiplier", color = AshGrey)
                    Text("${multiplier}x", color = PhoenixOrange, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                Divider(color = AshGrey.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ESTIMATED RECOVERY",
                        style = MaterialTheme.typography.titleLarge,
                        color = BoneWhite
                    )
                    Text(
                        "$estimAsh ASH",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp, fontFamily = FontFamily.Monospace),
                        color = PhoenixOrange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.resetBurnState() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BoneWhite),
                border = BorderStroke(1.dp, AshGrey)
            ) {
                Text("RETREAT")
            }

            Button(
                onClick = { viewModel.confirmBurn(stage) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("confirm_burn_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange, contentColor = BoneWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SACRIFICE")
            }
        }
    }
}

@Composable
fun PyreBurningAnimationView(progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val transition = rememberInfiniteTransition(label = "emberTrans")
        val scaleOffset by transition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(200, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flickerScale"
        )

        // Simulated burning particle canvas visual effects
        Box(
            modifier = Modifier
                .size(240.dp)
                .drawBehind {
                    // Draw a dark base shadow
                    drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = size.width / 2.2f)
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp * scaleOffset)
            ) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val brush = Brush.radialGradient(
                    colors = listOf(
                        PhoenixOrange.copy(alpha = 0.9f),
                        PhoenixOrange.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = size.width / 1.5f * progress
                )

                drawCircle(
                    brush = brush,
                    radius = (size.width / 2.2f) * progress
                )

                // Particle systems representing flying charcoal sparks
                val random = Random(42)
                for (i in 0..25) {
                    val particleAge = (progress * 3f + (i * 0.1f)) % 1f
                    val angle = random.nextFloat() * 2 * Math.PI
                    val distance = (random.nextFloat() * 60.dp.toPx() + 20.dp.toPx()) * particleAge
                    val x = centerOffset.x + (distance * Math.cos(angle)).toFloat()
                    val y = centerOffset.y - (distance * Math.sin(angle)).toFloat() - (particleAge * 80.dp.toPx()) // Rise up

                    val particleSize = (random.nextFloat() * 4.dp.toPx() + 2.dp.toPx()) * (1f - particleAge)
                    drawCircle(
                        color = if (random.nextBoolean()) PhoenixOrange else BoneWhite,
                        radius = particleSize,
                        center = Offset(x, y)
                    )
                }

                // Internal text outline going soot-black
                drawCircle(
                    color = Color.Black.copy(alpha = progress.coerceIn(0f, 1f)),
                    radius = (size.width / 3.2f)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "CONVERTING",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = AshGrey
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp, fontFamily = FontFamily.Monospace),
                    color = BoneWhite
                )
                Text(
                    "TO ASHES",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = AshGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "THE RITUAL PREPARATION IS UNDERWAY",
            style = MaterialTheme.typography.titleLarge,
            color = PhoenixOrange,
            textAlign = TextAlign.Center
        )
        Text(
            "CRACKLE... ASMR compression. Squeezing physical files into pure, eternal embers.",
            style = MaterialTheme.typography.bodyLarge,
            color = AshGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
fun PyreBurntSummaryView(
    viewModel: AshesViewModel,
    state: BurnStage.Burnt
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Giant success fire symbol
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Charcoal),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Success",
                tint = PhoenixOrange,
                modifier = Modifier.size(50.dp)
            )
        }

        Text(
            "SACRIFICE COMPLETED",
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
            color = BoneWhite,
            textAlign = TextAlign.Center
        )

        Text(
            "Epitaph:",
            style = MaterialTheme.typography.labelLarge,
            color = PhoenixOrange,
            modifier = Modifier.align(Alignment.Start)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Charcoal),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "“${state.epitaph}”",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Light,
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    ),
                    color = BoneWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text(
            "Ghost Memory Shadow Preview:",
            style = MaterialTheme.typography.labelLarge,
            color = AshGrey,
            modifier = Modifier.align(Alignment.Start)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonBlack),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AshGrey.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Blur preview",
                    tint = AshGrey,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    state.ghostDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshGrey
                )
            }
        }

        // Earned report
        Card(
            colors = CardDefaults.cardColors(containerColor = Charcoal),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PhoenixOrange),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("ASH TRANSMUTED", style = MaterialTheme.typography.labelSmall, color = AshGrey)
                Text(
                    "+${state.totalEarned} ASH",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp, fontFamily = FontFamily.Monospace),
                    color = PhoenixOrange
                )
                if (state.streakBonus) {
                    Text(
                        "GOLDEN STREAK BONUS (+100 ASH) UNLOCKED!",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.resetBurnState() },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .testTag("dismiss_burnt_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange, contentColor = BoneWhite),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "RETURN TO PYRE",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun MonumentScreen(
    viewModel: AshesViewModel,
    user: UserEntity?,
    blocks: List<MonumentBlockEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "YOUR RITUAL MONUMENT",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                color = BoneWhite
            )
            val decayStatus = if (user?.monumentTier == 0) "RUBBLE" else if (user?.currentStreak == 0) "DECAYING" else "INTACT"
            Text(
                "Status: $decayStatus • ${blocks.size} Active Blocks",
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = if (decayStatus == "INTACT") PhoenixOrange else if (decayStatus == "DECAYING") AshGrey else Color.Red
            )
        }

        // Custom drawn Canvas representing the user's stacked 3D Monument!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Charcoal)
                .border(1.dp, AshGrey.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (user?.monumentTier == 0 || blocks.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Broken monument", tint = AshGrey, modifier = Modifier.size(48.dp))
                    Text("Your Monument lies in rubble.", style = MaterialTheme.typography.titleLarge, color = BoneWhite)
                    Text("You missed sacrifices. Cleanse and rebuild.", style = MaterialTheme.typography.bodyLarge, color = AshGrey, modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.rebuildMonument() },
                        colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                        modifier = Modifier.testTag("rebuild_button")
                    ) {
                        Text("EXCAVATE & RESTORE (200 ASH)")
                    }
                }
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height - 50.dp.toPx()

                    // Draw ground horizon
                    drawLine(
                        color = AshGrey.copy(alpha = 0.3f),
                        start = Offset(20.dp.toPx(), cy + 10.dp.toPx()),
                        end = Offset(size.width - 20.dp.toPx(), cy + 10.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Draw block-by-block isometric stacks
                    blocks.forEachIndexed { i, block ->
                        val blockHeight = 24.dp.toPx()
                        val level = i / 3
                        val slot = i % 3

                        // Calculate slot offset
                        val dx = (slot - 1) * 45.dp.toPx()
                        val dy = - (level * blockHeight) - 10.dp.toPx()

                        val px = cx + dx
                        val py = cy + dy

                        val isBlockGold = block.material == "gold"
                        val strokeColor = if (isBlockGold) GoldAccent else PhoenixOrange

                        // Draw standard isometric box outlines
                        val path = Path().apply {
                            moveTo(px, py - blockHeight / 2)
                            lineTo(px + 20.dp.toPx(), py)
                            lineTo(px, py + blockHeight / 2)
                            lineTo(px - 20.dp.toPx(), py)
                            close()
                        }

                        drawPath(
                            path = path,
                            color = strokeColor.copy(alpha = 0.9f),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Material lines representing building blocks
                        drawLine(
                            color = strokeColor.copy(alpha = 0.4f),
                            start = Offset(px - 20.dp.toPx(), py),
                            end = Offset(px, py + blockHeight / 2),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = strokeColor.copy(alpha = 0.4f),
                            start = Offset(px + 20.dp.toPx(), py),
                            end = Offset(px, py + blockHeight / 2),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Labels for major structures
                        if (block.blockType == "phoenix_statue") {
                            // Wing lines!
                            drawLine(
                                color = GoldAccent,
                                start = Offset(px - 25.dp.toPx(), py - 35.dp.toPx()),
                                end = Offset(px + 25.dp.toPx(), py - 35.dp.toPx()),
                                strokeWidth = 5.dp.toPx()
                            )
                        }
                    }
                }
            }
        }

        // TRANSMUTE & SHARE TO SOCIALS
        var isSharing by remember { mutableStateOf(false) }
        var selectedPlatform by remember { mutableStateOf("") }
        val context = LocalContext.current

        if (isSharing) {
            AlertDialog(
                onDismissRequest = { isSharing = false },
                confirmButton = {},
                title = {
                    Text(
                        "SEARING DIGITAL SLATE",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        color = PhoenixOrange,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        CircularProgressIndicator(color = PhoenixOrange)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Transmuting $selectedPlatform card...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BoneWhite,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Polishing isometric lines & details for the void.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AshGrey,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                },
                containerColor = CarbonBlack,
                shape = RoundedCornerShape(24.dp)
            )

            LaunchedEffect(selectedPlatform) {
                kotlinx.coroutines.delay(1500)
                isSharing = false

                val tierName = when (user?.monumentTier ?: 0) {
                    0 -> "Rubble"
                    1 -> "Stone"
                    2 -> "Bronze"
                    3 -> "Silver"
                    4 -> "Gold"
                    5 -> "Phoenix"
                    else -> "Ascended"
                }
                val appLink = "https://ai.studio/build"
                val preformattedMessage = "My Monument in ASHES has reached the $tierName tier! Burn to Build with me: $appLink"

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "My ASHES Monument")
                    putExtra(Intent.EXTRA_TEXT, preformattedMessage)
                }
                context.startActivity(Intent.createChooser(intent, "Share via ASHES"))
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Charcoal),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Broadcast to the void",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Light,
                        fontSize = 20.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = BoneWhite
                )
                Text(
                    text = "Expose your immutable monument's current state to external platforms.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AshGrey
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sharePlatforms = listOf(
                        "Instagram" to Color(0xFFE1306C),
                        "TikTok" to Color(0xFF000000),
                        "Twitter" to Color(0xFF1DA1F2),
                        "Standard" to PhoenixOrange
                    )

                    sharePlatforms.forEach { (platform, color) ->
                        Button(
                            onClick = {
                                selectedPlatform = platform
                                isSharing = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (platform == "TikTok") Charcoal.copy(alpha = 0.5f) else color.copy(alpha = 0.15f),
                                contentColor = if (platform == "Standard") PhoenixOrange else color
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("share_btn_${platform.lowercase(Locale.ROOT)}")
                        ) {
                            Text(
                                text = if (platform == "Standard") "ANY" else platform.uppercase(Locale.ROOT).take(4),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Builders section: Spend Ash here
        Text(
            "BUILD THE MONUMENT",
            style = MaterialTheme.typography.labelLarge,
            color = AshGrey,
            modifier = Modifier.align(Alignment.Start)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val buildOfferings = listOf(
                BuildPlan("Foundation", 10, "foundation", "Wide low blocks at ground layer."),
                BuildPlan("Pillar", 50, "pillar", "Vertical structural pillars."),
                BuildPlan("Arch", 100, "arch", "Arc connection outlines on pillars."),
                BuildPlan("Altar", 500, "altar", "Sacred ceremonial platform."),
                BuildPlan("Phoenix Statue", 2000, "phoenix_statue", "The ultimate transmutational statue.")
            )

            buildOfferings.forEach { plan ->
                val canAfford = (user?.ashBalance ?: 0) >= plan.price
                val hasExcaved = user?.monumentTier != 0

                Card(
                    colors = CardDefaults.cardColors(containerColor = Charcoal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                plan.title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = BoneWhite
                            )
                            Text(
                                plan.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = AshGrey
                            )
                        }

                        Button(
                            onClick = { viewModel.buildBlock(plan.blockType, plan.price) },
                            enabled = canAfford && hasExcaved,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PhoenixOrange,
                                contentColor = BoneWhite,
                                disabledContainerColor = AshGrey.copy(alpha = 0.2f),
                                disabledContentColor = AshGrey
                            ),
                            modifier = Modifier.testTag("build_item_${plan.blockType}")
                        ) {
                            Text("${plan.price} ASH")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.clearMonument() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_monument_button")
            ) {
                Text("CLEAR & CRUSH MONUMENT")
            }
        }
    }
}

@Composable
fun CemeteryScreen(
    viewModel: AshesViewModel,
    user: UserEntity?,
    friends: List<FriendSinner>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                "SINNERS' CEMETERY",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                color = BoneWhite
            )
            Text(
                "Observe monuments of other burners, inspect their Ghost Memories, and guess their contents to steal their balance.",
                style = MaterialTheme.typography.bodyLarge,
                color = AshGrey
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(friends) { friend ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Charcoal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Star, contentDescription = "Active", tint = if (friend.currentStreak > 0) PhoenixOrange else AshGrey)
                                Column {
                                    Text(
                                        friend.username,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = BoneWhite
                                    )
                                    Text(
                                        "Streak: ${friend.currentStreak} Days • Burns: ${friend.totalBurns}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AshGrey
                                    )
                                }
                            }

                            // Left an Ember like
                            IconButton(
                                onClick = { viewModel.leaveEmber(friend.id) },
                                enabled = !friend.isEmbered && (user?.ashBalance ?: 0) >= 1
                            ) {
                                Icon(
                                    imageVector = if (friend.isEmbered) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Leave Ember",
                                    tint = if (friend.isEmbered) PhoenixOrange else AshGrey
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ghost memory slot
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CarbonBlack)
                                .border(1.dp, AshGrey.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = "Blurred profile picture", tint = PhoenixOrange, modifier = Modifier.size(32.dp))
                                Column {
                                    Text(
                                        "GHOST REVELATION CLUE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AshGrey
                                    )
                                    Text(
                                        friend.ghostDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BoneWhite
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Guess slot
                        if (friend.guessedCorrectly == null) {
                            Text(
                                "Guess Ghost Media Type (Costs 10 Ash. Correct = wins +25 Ash!)",
                                style = MaterialTheme.typography.labelSmall,
                                color = AshGrey,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("photo", "voice", "text", "video").forEach { mt ->
                                    Button(
                                        onClick = { viewModel.guessGhostMediaType(friend.id, mt) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CarbonBlack, contentColor = AshGrey),
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, AshGrey.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .testTag("guess_${friend.username}_$mt"),
                                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                                    ) {
                                        Text(mt.uppercase(Locale.ROOT), fontSize = 10.sp)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val outcomeIcon = if (friend.guessedCorrectly == true) Icons.Default.CheckCircle else Icons.Default.Close
                                val outcomeColor = if (friend.guessedCorrectly == true) PhoenixOrange else Color.Red
                                val outcomeText = if (friend.guessedCorrectly == true) "CORRECT GUESS (+25 ASH!)" else "WRONG GUESS (-10 ASH)"

                                Icon(outcomeIcon, contentDescription = "Outcome", tint = outcomeColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    outcomeText,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = outcomeColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsShopScreen(
    viewModel: AshesViewModel,
    user: UserEntity?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                "RITUAL OFFERINGS",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                color = BoneWhite
            )
            Text(
                "Buy luxury, ominous upgrades to escape the paradox of digital memory loss.",
                style = MaterialTheme.typography.bodyLarge,
                color = AshGrey
            )
        }

        val products = listOf(
            OfferItem(
                "eternal_flame",
                "Eternal Flame Plan",
                "BURNS METADATA CLOUD SYNC",
                "€3.99/mo",
                "Burnt files are silently encrypted & backed up. Accurately addresses loss anxiety.",
                250
            ),
            OfferItem(
                "phoenix_pack",
                "Phoenix Pack Consumable",
                "INSTANT DETRITUS RESTORE",
                "€0.99",
                "Instantly repair 5 crumbled monument blocks during unexpected decay events.",
                60
            ),
            OfferItem(
                "architect_bundle",
                "Architect Obsidian Bundle",
                "EXCLUSIVE STATUTE INLAY",
                "€4.99",
                "Unlock elite Obsidian blocks + 500 immediate bonus Ash points.",
                500
            ),
            OfferItem(
                "fireproof_insurance",
                "Fireproof Insurance",
                "SABOTAGE PROTECTION",
                "€1.99",
                "3 monument active blocks are immune to Companion sabotage algorithms for 30 days.",
                120
            )
        )

        products.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Charcoal),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PhoenixOrange.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item.tagline,
                            style = MaterialTheme.typography.labelSmall,
                            color = PhoenixOrange
                        )

                        Text(
                            item.price,
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            color = BoneWhite
                        )
                    }

                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = BoneWhite,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshGrey
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.triggerVirtualOffer(item.id, item.price.substring(1).toDoubleOrNull() ?: 1.0, item.ashBonus) },
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonBlack, contentColor = PhoenixOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PhoenixOrange.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .testTag("buy_${item.id}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Buy", modifier = Modifier.size(16.dp))
                            Text("TRANSMUTE (+${item.ashBonus} ASH BONUS)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class OfferItem(
    val id: String,
    val name: String,
    val tagline: String,
    val price: String,
    val description: String,
    val ashBonus: Int
)

data class BuildPlan(
    val title: String,
    val price: Int,
    val blockType: String,
    val description: String
)

@Composable
fun OrbitBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2.5f

            // Dashed circles
            drawCircle(
                color = AshGrey.copy(alpha = 0.15f),
                radius = 120.dp.toPx(),
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f), 0f
                    )
                )
            )
            drawCircle(
                color = AshGrey.copy(alpha = 0.15f),
                radius = 90.dp.toPx(),
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f), 0f
                    )
                )
            )

            // Horizontal gradient rule line
            val brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    AshGrey.copy(alpha = 0.2f),
                    Color.Transparent
                )
            )
            drawLine(
                brush = brush,
                start = Offset(0f, cy),
                end = Offset(size.width, cy),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun TempleArchway(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Archway outline
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .width(110.dp)
                .height(160.dp)
                .border(
                    width = 2.dp,
                    color = BoneWhite,
                    shape = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomEndPercent = 0, bottomStartPercent = 0)
                )
                .drawBehind {
                    // Draw horizontal rule line underneath the archway base
                    val baselineY = size.height + 4.dp.toPx()
                    val lineLength = 140.dp.toPx()
                    drawLine(
                        color = BoneWhite,
                        start = Offset((size.width - lineLength) / 2f, baselineY),
                        end = Offset((size.width + lineLength) / 2f, baselineY),
                        strokeWidth = 2.dp.toPx()
                    )
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Glowing inner core gradient arch
            Box(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .width(55.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomEndPercent = 0, bottomStartPercent = 0))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PhoenixOrange.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Titles beneath the Archway
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                fontSize = 24.sp,
                lineHeight = 28.sp
            ),
            color = BoneWhite,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                fontSize = 10.sp
            ),
            color = AshGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SegmentedStreakBar(
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "CURRENT STREAK",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            ),
            color = AshGrey
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val activeSegments = currentStreak.coerceIn(0, 7)
            for (i in 1..7) {
                val isActive = i <= activeSegments
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (isActive) PhoenixOrange else Charcoal,
                            shape = RoundedCornerShape(1f)
                        )
                )
            }
        }
    }
}

@Composable
fun AuctionScreen(
    viewModel: AshesViewModel,
    user: UserEntity?,
    auctions: List<AshAuction>,
    notifications: List<AuctionNotification>
) {
    var bidAmounts by remember { mutableStateOf(mapOf<String, Int>()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Core Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    "THE ASH AUCTION HOUSE",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 26.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Light,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = BoneWhite,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    "Claims and unblurs pixelated Ghost Memories. Original burners receive 70% of winning bids. The auction house claims 30% to burn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshGrey
                )
            }
        }

        // Active Auctions subtitle label
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ACTIVE AUCTIONS (24H)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    ),
                    color = PhoenixOrange
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Charcoal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${user?.ashBalance ?: 0} ASH AVAILABLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = BoneWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (auctions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Charcoal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Text(
                        "The auction block is empty. Let some memories burn first to highlight them here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshGrey,
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(auctions) { auc ->
                val currentBidValue = bidAmounts[auc.id] ?: (auc.currentHighestBid + 15)
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Charcoal),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header: Owner & remaining duration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (auc.isResolved) AshGrey else PhoenixOrange)
                                )
                                Text(
                                    text = "Burner: ${auc.originalBurner}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BoneWhite
                                )
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (auc.isResolved) CarbonBlack else PhoenixOrange.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = auc.timeLeftLabel.uppercase(Locale.ROOT),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (auc.isResolved) AshGrey else PhoenixOrange
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Ghost visual centerpiece (Highly pixelated simulation)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CarbonBlack)
                                .border(1.dp, Color.White.copy(alpha = 0.03f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val typeIcon = when (auc.mediaType) {
                                    "photo" -> Icons.Default.Face
                                    "voice" -> Icons.Default.Info
                                    "video" -> Icons.Default.PlayArrow
                                    else -> Icons.Default.Edit
                                }
                                
                                Icon(
                                    imageVector = typeIcon,
                                    contentDescription = "MediaType key",
                                    tint = if (auc.isResolved) AshGrey.copy(alpha = 0.3f) else PhoenixOrange.copy(alpha = 0.4f),
                                    modifier = Modifier.size(32.dp)
                                )

                                Text(
                                    text = if (auc.isResolved) "DECRYPTED RITUAL MEMORY" else "PIXELATED SHADOW: ${auc.mediaType.uppercase(Locale.ROOT)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp,
                                        color = if (auc.isResolved) AshGrey else PhoenixOrange
                                    )
                                )

                                Text(
                                    text = if (auc.isResolved) "This Ghost has been claimed from the underworld." else auc.ghostDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AshGrey,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        if (!auc.isResolved) {
                            // Active State
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "HIGHEST BID",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = AshGrey
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            "${auc.currentHighestBid} ASH",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (auc.highestBidder == "You") GoldAccent else PhoenixOrange
                                        )
                                        if (auc.highestBidder == "You") {
                                            Text(
                                                "(You leading)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = GoldAccent
                                            )
                                        } else {
                                            Text(
                                                "by ${auc.highestBidder}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AshGrey
                                            )
                                        }
                                    }
                                }

                                Text(
                                    "Bids: ${auc.bidCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = AshGrey
                                )
                            }

                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))

                            // Action panel to place bids
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Your Bid Target:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BoneWhite
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        "$currentBidValue ASH",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PhoenixOrange
                                    )
                                }

                                // Quick increment elements
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val increments = listOf(10, 25, 50)
                                    increments.forEach { inc ->
                                        OutlinedButton(
                                            onClick = {
                                                val nextBid = currentBidValue + inc
                                                bidAmounts = bidAmounts + (auc.id to nextBid)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BoneWhite),
                                            border = BorderStroke(1.dp, AshGrey.copy(alpha = 0.2f)),
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Text("+$inc")
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Submit Bid Button
                                    Button(
                                        onClick = {
                                            viewModel.placeBid(auc.id, currentBidValue)
                                            bidAmounts = bidAmounts + (auc.id to (currentBidValue + 15))
                                        },
                                        enabled = currentBidValue > auc.currentHighestBid,
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(48.dp)
                                            .testTag("submit_bid_${auc.id}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PhoenixOrange,
                                            contentColor = CarbonBlack,
                                            disabledContainerColor = AshGrey.copy(alpha = 0.2f),
                                            disabledContentColor = AshGrey
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "PLACE BID",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    // FORCE SETTLE / FAST FORWARD
                                    Button(
                                        onClick = {
                                            viewModel.resolveAuction(auc.id)
                                        },
                                        modifier = Modifier
                                            .weight(0.7f)
                                            .height(48.dp)
                                            .testTag("resolve_bid_${auc.id}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Charcoal,
                                            contentColor = AshGrey
                                        ),
                                        border = BorderStroke(1.dp, AshGrey.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "SETTLE",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Resolved state
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))

                            if (auc.isWonByMe) {
                                // You won and unblurred!
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = "Won", tint = GoldAccent)
                                        Text(
                                            text = "🏆 REVEALED TRUTH",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GoldAccent
                                        )
                                    }

                                    Text(
                                        text = "Title: ${auc.originalTitle}",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = BoneWhite
                                    )
                                    Text(
                                        text = "Memory Description: \"${auc.originalDetails}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AshGrey,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(GoldAccent.copy(alpha = 0.1f))
                                            .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            "This memory belongs to your collection in the Eternal Fire. You paid ${auc.currentHighestBid} Ash.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = GoldAccent
                                        )
                                    }
                                }
                            } else {
                                // Locked / someone else won
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "🔒 CLOSED",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AshGrey
                                    )
                                    Text(
                                        text = "Owner: ${auc.highestBidder} bought the Ghost for ${auc.currentHighestBid} Ash.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AshGrey
                                    )
                                    
                                    if (auc.originalBurner.contains("You", ignoreCase = true)) {
                                        val earnAmount = (auc.currentHighestBid * 0.7).toInt()
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(PhoenixOrange.copy(alpha = 0.1f))
                                                .border(1.dp, PhoenixOrange.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                "Your auction closed. You pocketed your 70% original burner share: +$earnAmount ASH!",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = PhoenixOrange
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ledger of recent activity title
        item {
            Text(
                "RITUAL AUCTION LEDGER",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                ),
                color = AshGrey,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (notifications.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Charcoal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "No ledger reports or bids logged this week.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AshGrey,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(notifications) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Charcoal.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.02f)),
                    modifier = Modifier.fillMaxWidth().testTag("notification_item")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Notification Detail Icon",
                            tint = if (log.message.contains("Congrats") || log.message.contains("won")) GoldAccent else PhoenixOrange.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = log.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = BoneWhite
                        )
                    }
                }
            }
        }
    }
}
