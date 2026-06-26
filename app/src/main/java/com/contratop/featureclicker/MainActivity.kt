package com.contratop.featureclicker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    primary = Color(0xFFBB86FC),
                    onPrimary = Color.Black
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, label = "button_scale")

    val fakeCode = remember {
        """
package com.matrix.core

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

class NeuralNetwork(val layers: Int) {
    private val weights = DoubleArray(layers * 1024)
    val state = AtomicLong(0)

    suspend fun train(epochs: Int) = coroutineScope {
        for (i in 0 until epochs) {
            launch {
                optimizeLayer(i)
                state.incrementAndGet()
            }
        }
    }

    private fun optimizeLayer(index: Int) {
        val hash = index.hashCode() * 31
        weights[index % weights.size] += hash * 0.001
        if (weights[index % weights.size] > 1.0) {
            normalize()
        }
    }

    private fun normalize() {
        for (i in weights.indices) {
            weights[i] /= 2.0
        }
    }
}

fun main() = runBlocking {
    println("Initializing core systems...")
    val ai = NeuralNetwork(layers = 64)
    ai.train(epochs = 100000)
    println("Training complete. System ready.")
}
        """.trimIndent().repeat(20)
    }

    var revealedChars by remember { mutableIntStateOf(0) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E1E2E), Color(0xFF121212))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feature Clicker") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(paddingValues)
        ) {
            
            // Typing Code Background Effect
            if (state.vfxEnabled) {
                val scrollState = androidx.compose.foundation.rememberScrollState()
                LaunchedEffect(revealedChars) {
                    if (scrollState.maxValue > 0) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = fakeCode.substring(0, revealedChars.coerceAtMost(fakeCode.length)),
                        color = Color.Green.copy(alpha = 0.15f),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${state.commits} Commits",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${state.commitsPerSecond} commits / sec",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF6200EA))
                            )
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            // Reveal more code on click
                            revealedChars += (25..45).random()
                            if (revealedChars > fakeCode.length) revealedChars = 0
                            
                            viewModel.onMainButtonClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💡", fontSize = 80.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { showBottomSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(text = "Mejoras (Upgrades) ⬆️", fontSize = 18.sp, color = Color.White)
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tienda de Mejoras",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        LazyColumn {
                            items(state.upgrades.sortedBy { it.currentCost }) { upgrade ->
                                UpgradeItem(
                                    upgrade = upgrade,
                                    canAfford = state.commits >= upgrade.currentCost,
                                    onBuyClick = { viewModel.buyUpgrade(upgrade.id) }
                                )
                                HorizontalDivider(color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
            
            if (showSettingsDialog) {
                SettingsDialog(
                    state = state,
                    onDismiss = { showSettingsDialog = false },
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleVfx = { viewModel.toggleVfx() },
                    onClearData = { viewModel.clearAllData() }
                )
            }
        }
    }
}

@Composable
fun SettingsDialog(
    state: GameState,
    onDismiss: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleVfx: () -> Unit,
    onClearData: () -> Unit
) {
    var showConfirmClear by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Efectos Visuales (VFX)", color = Color.White)
                    Switch(checked = state.vfxEnabled, onCheckedChange = { onToggleVfx() })
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sonido (Próximamente)", color = Color.White)
                    Switch(checked = state.soundEnabled, onCheckedChange = { onToggleSound() })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { showConfirmClear = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Borrar Progreso", color = Color.White)
                }

                if (showConfirmClear) {
                    AlertDialog(
                        onDismissRequest = { showConfirmClear = false },
                        title = { Text("¿Estás seguro?") },
                        text = { Text("Esto borrará todos tus Commits y mejoras. No se puede deshacer.") },
                        confirmButton = {
                            Button(onClick = { 
                                onClearData()
                                showConfirmClear = false 
                                onDismiss()
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                                Text("Borrar", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmClear = false }) {
                                Text("Cancelar", color = Color.White)
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = Color.White)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun UpgradeItem(upgrade: Upgrade, canAfford: Boolean, onBuyClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = upgrade.iconEmoji, fontSize = 32.sp, modifier = Modifier.padding(end = 16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = upgrade.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = upgrade.description, fontSize = 14.sp, color = Color.LightGray)
            Text(text = "Nivel: ${upgrade.level}", fontSize = 12.sp, color = Color.Gray)
        }
        
        Button(
            onClick = onBuyClick,
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.DarkGray
            )
        ) {
            Text(text = "${upgrade.currentCost}", color = if (canAfford) Color.Black else Color.LightGray)
        }
    }
}
