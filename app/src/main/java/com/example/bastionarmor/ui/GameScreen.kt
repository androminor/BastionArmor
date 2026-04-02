package com.example.bastionarmor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bastionarmor.domain.model.Enemy
import com.example.bastionarmor.domain.model.EnemyType
import com.example.bastionarmor.domain.model.GameBoard
import com.example.bastionarmor.domain.model.GameStatus
import com.example.bastionarmor.domain.model.Position
import com.example.bastionarmor.domain.model.Tower
import com.example.bastionarmor.domain.model.TowerType
import com.example.bastionarmor.presentation.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val state by viewModel.gameState.collectAsState()
    val board by viewModel.board.collectAsState()
    
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
                                        val logicalPos = Position(offset.x / scaleX, offset.y / scaleY)
                                        viewModel.placeTower(state.selectedTowerType!!, logicalPos)
                                    }
                                }
                            }
                    ) {
                        drawValidTowerZones(board, state.selectedTowerType, scaleX, scaleY)
                        drawPath(board.path, scaleX, scaleY)
                        
                        state.towers.forEach { tower ->
                            drawTower(tower, state.selectedTowerType != null, scaleX, scaleY)
                        }

                        state.enemies.forEach { enemy ->
                            drawEnemy(enemy, scaleX, scaleY)
                        }

                        drawShots(state.towers, scaleX, scaleY)
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
                            GameStatus.WAVE_COMPLETE -> "Next Wave Ready"
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
            drawCircle(
                Color.Green.copy(alpha = 0.15f),
                radius = 20f * scaleX,
                center = Offset(position.x * scaleX, position.y * scaleY)
            )
        }
    }
}

private fun DrawScope.drawPath(path: List<Position>, scaleX: Float, scaleY: Float) {
    if (path.isEmpty()) return
    for (i in 0 until path.size - 1) {
        drawLine(
            Color(0xFF313244),
            Offset(path[i].x * scaleX, path[i].y * scaleY),
            Offset(path[i+1].x * scaleX, path[i+1].y * scaleY),
            strokeWidth = 20f * scaleX
        )
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
    
    if (showRange) {
        drawCircle(towerColor.copy(alpha = 0.1f), radius = tower.range * scaleX, center = center)
    }

    drawCircle(color = towerColor, radius = 18f * scaleX, center = center)
    drawCircle(color = Color.White.copy(alpha = 0.3f), radius = 18f * scaleX, center = center, style = Stroke(width = 2f))
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
    drawRect(Color.Red, Offset(center.x - hbW/2, center.y - radius - 10f), Size(hbW, hbH))
    drawRect(Color.Green, Offset(center.x - hbW/2, center.y - radius - 10f), Size(hbW * healthPct, hbH))
}

private fun DrawScope.drawShots(towers: List<Tower>, scaleX: Float, scaleY: Float) {
    val currentTime = System.currentTimeMillis()
    towers.forEach { tower ->
        val targetPos = tower.lastShotTargetPos
        if (targetPos != null && currentTime - tower.lastShotTime < 150) {
            drawLine(
                color = Color.Yellow,
                start = Offset(tower.position.x * scaleX, tower.position.y * scaleY),
                end = Offset(targetPos.x * scaleX, targetPos.y * scaleY),
                strokeWidth = 4f * scaleX
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun TowerButton(type: TowerType, isSelected: Boolean, canAfford: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color(0xFFCBA6F7) else if (canAfford) Color(0xFF45475A) else Color(0xFF313244)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = color,
        modifier = Modifier.size(80.dp, 60.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(type.displayName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else Color.White)
            Text("${type.baseCost}g", fontSize = 9.sp, color = if (canAfford) Color(0xFFF9E2AF) else Color(0xFFF38BA8))
        }
    }
}
