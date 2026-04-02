package com.example.bastionarmor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bastionarmor.domain.model.*
import com.example.bastionarmor.presentation.GameViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val state by viewModel.gameState.collectAsState()
    val board by viewModel.board.collectAsState()
    
    // Scale factors to map logical 800x600 to actual screen size
    var scaleX by remember { mutableStateOf(1f) }
    var scaleY by remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Stats Bar
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF313244))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem("Wave", state.currentWave.toString(), Color(0xFFCBA6F7))
                    StatItem("Gold", "${state.playerGold}g", Color(0xFFF9E2AF))
                    StatItem("Lives", state.playerLives.toString(), Color(0xFFF38BA8))
                    StatItem("Score", state.score.toString(), Color(0xFFA6E3A1))
                }
            }

            // Game Canvas
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                // Calculate scale based on the available area (logical board is 800x600)
                scaleX = constraints.maxWidth.toFloat() / 800f
                scaleY = constraints.maxHeight.toFloat() / 600f

                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11111B))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(state.selectedTowerType) {
                                detectTapGestures { offset ->
                                    if (state.selectedTowerType != null) {
                                        // Convert tap pixels to logical coordinates
                                        val logicalPos = Position(
                                            offset.x / scaleX,
                                            offset.y / scaleY
                                        )
                                        viewModel.placeTower(state.selectedTowerType!!, logicalPos)
                                    }
                                }
                            }
                    ) {
                        // Drawing using logical-to-pixel scaling
                        drawValidTowerZones(board, state.selectedTowerType, scaleX, scaleY)
                        drawPath(board.path, scaleX, scaleY)
                        
                        state.towers.forEach { tower ->
                            drawTower(tower, state.selectedTowerType != null, scaleX, scaleY)
                        }

                        state.enemies.forEach { enemy ->
                            drawEnemy(enemy, scaleX, scaleY)
                        }

                        drawShots(state.towers, state.enemies, scaleX, scaleY)
                    }
                }
            }

            // Tower Selection
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF313244))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Select Tower", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(TowerType.entries) { type ->
                            TowerButton(
                                type = type,
                                isSelected = state.selectedTowerType == type,
                                canAfford = state.canAfford(type.baseCost),
                                onClick = {
                                    viewModel.selectTowerType(if (state.selectedTowerType == type) null else type)
                                }
                            )
                        }
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.startWave() },
                    modifier = Modifier.weight(1f),
                    enabled = state.gameStatus != GameStatus.PLAYING,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFA6E3A1),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        when (state.gameStatus) {
                            GameStatus.WAITING_TO_START -> "Start Wave ${state.currentWave}"
                            GameStatus.PLAYING -> "Wave in Progress"
                            GameStatus.WAVE_COMPLETE -> "Start Next Wave"
                            GameStatus.GAME_OVER -> "Game Over"
                            GameStatus.VICTORY -> "Victory!"
                            else -> "Start Wave"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = { viewModel.resetGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF38BA8))
                ) {
                    Text(if (state.gameStatus == GameStatus.PLAYING) "Cancel" else "Reset", color = Color.White)
                }
            }
        }
    }
}

private fun DrawScope.drawValidTowerZones(board: GameBoard, selectedTowerType: TowerType?, scaleX: Float, scaleY: Float) {
    if (selectedTowerType != null) {
        board.getValidTowerZones().forEach { position ->
            val center = Offset(position.x * scaleX, position.y * scaleY)
            drawCircle(
                Color.Green.copy(alpha = 0.2f),
                radius = 20f * scaleX,
                center = center
            )
            drawCircle(
                Color.Green.copy(alpha = 0.5f),
                radius = 20f * scaleX,
                center = center,
                style = Stroke(width = 2f)
            )
        }
    }
}

private fun DrawScope.drawPath(path: List<Position>, scaleX: Float, scaleY: Float) {
    if (path.isEmpty()) return
    for (i in 0 until path.size - 1) {
        drawLine(
            Color(0xFF45475A),
            Offset(path[i].x * scaleX, path[i].y * scaleY),
            Offset(path[i+1].x * scaleX, path[i+1].y * scaleY),
            strokeWidth = 12f * scaleX
        )
    }
    path.forEach { pos ->
        drawCircle(Color(0xFF585B70), radius = 4f * scaleX, center = Offset(pos.x * scaleX, pos.y * scaleY))
    }
}

private fun DrawScope.drawTower(tower: Tower, showRange: Boolean, scaleX: Float, scaleY: Float) {
    val towerColor = when (tower.type) {
        TowerType.GUNNER -> Color(0xFF89B4FA)
        TowerType.SENTRY -> Color(0xFFA6E3A1)
        TowerType.TRIPLE_SHOOTER -> Color(0xFFF9E2AF)
        TowerType.THROWING_AXE -> Color(0xFFF38BA8)
    }
    val center = Offset(tower.position.x * scaleX, tower.position.y * scaleY)
    val radius = 18f * scaleX

    // Range preview
    if (showRange) {
        drawCircle(towerColor.copy(alpha = 0.1f), radius = tower.range * scaleX, center = center)
        drawCircle(towerColor.copy(alpha = 0.2f), radius = tower.range * scaleX, center = center, style = Stroke(width = 1f))
    }

    // Base
    drawCircle(color = towerColor, radius = radius, center = center)
    drawCircle(color = Color.White.copy(alpha = 0.5f), radius = radius, center = center, style = Stroke(width = 2f))
    
    // Level indicator (small pips)
    repeat(tower.level) { i ->
        drawCircle(
            Color.White,
            radius = 3f * scaleX,
            center = Offset(center.x - radius + (i * 8f * scaleX), center.y + radius + 5f * scaleY)
        )
    }
}

private fun DrawScope.drawEnemy(enemy: Enemy, scaleX: Float, scaleY: Float) {
    val color = when (enemy.type) {
        EnemyType.BASIC -> Color(0xFFF38BA8)
        EnemyType.FAST -> Color(0xFFA6E3A1)
        EnemyType.TANK -> Color(0xFFFAB387)
        EnemyType.BOSS -> Color(0xFFCBA6F7)
    }
    val center = Offset(enemy.position.x * scaleX, enemy.position.y * scaleY)
    val radius = 12f * scaleX
    
    drawCircle(color = color, radius = radius, center = center)
    
    // Health bar
    val hbW = 24f * scaleX
    val hbH = 4f * scaleY
    val healthPct = enemy.health.toFloat() / enemy.maxHealth
    val hbX = center.x - hbW/2
    val hbY = center.y - radius - 8f * scaleY
    
    drawRect(Color.Red.copy(alpha = 0.5f), Offset(hbX, hbY), Size(hbW, hbH))
    drawRect(Color.Green, Offset(hbX, hbY), Size(hbW * healthPct, hbH))
}

private fun DrawScope.drawShots(towers: List<Tower>, enemies: List<Enemy>, scaleX: Float, scaleY: Float) {
    val currentTime = System.currentTimeMillis()
    towers.forEach { tower ->
        // Show shots that happened in the last 150ms for persistence
        if (currentTime - tower.lastShotTime < 150) {
            val target = enemies.firstOrNull { enemy ->
                tower.position.distanceTo(enemy.position) <= tower.range + 20f // slight buffer for visuals
            }
            if (target != null) {
                drawLine(
                    color = Color.Yellow.copy(alpha = 0.7f),
                    start = Offset(tower.position.x * scaleX, tower.position.y * scaleY),
                    end = Offset(target.position.x * scaleX, target.position.y * scaleY),
                    strokeWidth = 3f * scaleX
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        Text(text = value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
private fun TowerButton(type: TowerType, isSelected: Boolean, canAfford: Boolean, onClick: () -> Unit) {
    val containerColor = when {
        isSelected -> Color(0xFFCBA6F7)
        canAfford -> Color(0xFF45475A)
        else -> Color(0xFF313244)
    }
    val contentColor = if (isSelected) Color.Black else if (canAfford) Color.White else Color.White.copy(alpha = 0.4f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier.size(width = 90.dp, height = 70.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(type.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 12.sp)
            Text("${type.baseCost}g", fontSize = 10.sp, color = if (canAfford) Color(0xFFF9E2AF) else Color(0xFFF38BA8))
        }
    }
}
