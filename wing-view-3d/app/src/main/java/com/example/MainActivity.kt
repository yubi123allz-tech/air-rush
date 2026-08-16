package com.example

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.initialize(this)
        RetroAudioSynthesizer.init(this)
        setContent {
            MyApplicationTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Flappy3DGameScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        RetroAudioSynthesizer.stopMusicLoop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            RetroAudioSynthesizer.startMusicLoop()
        } else {
            RetroAudioSynthesizer.stopMusicLoop()
        }
    }
}

@Composable
fun Flappy3DGameScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val gameEngine = remember { GameEngine(context) }

    val screenState by gameEngine.screenState.collectAsState()
    val score by gameEngine.score.collectAsState()
    val highScore by gameEngine.highScore.collectAsState()
    val isNewHighScore by gameEngine.isNewHighScore.collectAsState()
    val currentSkin by gameEngine.selectedSkin.collectAsState()
    val currentWeather by gameEngine.selectedWeather.collectAsState()
    val isSoundEnabled by gameEngine.isSoundEnabled.collectAsState()
    val coins by gameEngine.coins.collectAsState()
    val unlockedSkins by gameEngine.unlockedSkins.collectAsState()
    val unlockedWeathers by gameEngine.unlockedWeathers.collectAsState()
    val selectedLanguage by gameEngine.selectedLanguage.collectAsState()
    val playCount by gameEngine.playCount.collectAsState()

    // Show Interstitial Ad every 4 games upon GAMEOVER
    LaunchedEffect(screenState, playCount) {
        if (screenState == ScreenState.GAMEOVER && playCount > 0 && playCount % 4 == 0) {
            AdManager.showAd(context)
        }
    }

    // Smooth physics tick loop
    LaunchedEffect(screenState) {
        if (screenState == ScreenState.PLAYING) {
            var lastTime = System.nanoTime()
            while (true) {
                withFrameNanos { frameTimeNanos ->
                    val elapsedSeconds = ((frameTimeNanos - lastTime) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    lastTime = frameTimeNanos
                    gameEngine.update(elapsedSeconds)
                }
            }
        }
    }

    // Interactive tap-to-flap area covering the screen
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (screenState == ScreenState.PLAYING) {
                    gameEngine.tapToFlap()
                }
            }
    ) {
        // --- 3D PERSPECTIVE RENDERING CANVAS ---
        Game3DCanvas(
            gameEngine = gameEngine,
            skin = currentSkin,
            weather = currentWeather,
            modifier = Modifier.fillMaxSize()
        )

        // --- HUD OVERLAY (Only during PLAYING) ---
        if (screenState == ScreenState.PLAYING) {
            PlayingHUD(
                score = score,
                coins = coins,
                playerY = gameEngine.playerY,
                speed = gameEngine.speedZ,
                isSoundEnabled = isSoundEnabled,
                onToggleSound = { gameEngine.toggleSound() }
            )
        }

        // --- MAIN MENU OVERLAY ---
        AnimatedVisibility(
            visible = screenState == ScreenState.MENU,
            enter = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.94f),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            MainMenuOverlay(
                highScore = highScore,
                coins = coins,
                unlockedSkins = unlockedSkins,
                selectedSkin = currentSkin,
                unlockedWeathers = unlockedWeathers,
                selectedWeather = currentWeather,
                selectedLanguage = selectedLanguage,
                isSoundEnabled = isSoundEnabled,
                onStartGame = { gameEngine.startGame() },
                onSkinSelect = { gameEngine.selectOrBuySkin(it) },
                onWeatherSelect = { gameEngine.selectOrBuyWeather(it) },
                onLanguageSelect = { gameEngine.setLanguage(it) },
                onToggleSound = { gameEngine.toggleSound() }
            )
        }

        // --- GAME OVER OVERLAY ---
        AnimatedVisibility(
            visible = screenState == ScreenState.GAMEOVER,
            enter = fadeIn(animationSpec = tween(450)) + scaleIn(initialScale = 0.90f),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            GameOverOverlay(
                score = score,
                highScore = highScore,
                isNewBest = isNewHighScore,
                coins = coins,
                selectedLanguage = selectedLanguage,
                onRestart = { gameEngine.startGame() },
                onMainMenu = { gameEngine.resetWorld(); gameEngine.screenStateValue(ScreenState.MENU) }
            )
        }
    }
}

@Composable
fun Game3DCanvas(
    gameEngine: GameEngine,
    skin: BirdSkin,
    weather: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "canvas_anim")
    val propRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "propeller"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val camY = (gameEngine.playerY * 0.65f).coerceIn(-2.8f, 3.2f)
        val camZ = gameEngine.playerZ
        val targetRoll = (gameEngine.playerVelocityY * 0.35f).coerceIn(-3.0f, 3.0f)

        // 1. Dynamic Sky & Horizon Gradient with Celestial Object based on Weather
        drawSkyBackground(weather, width, height)

        rotate(degrees = targetRoll, pivot = Offset(width / 2f, height / 2f)) {
            // 2. Parallax Silhouette Mountains
            drawParallaxMountains(width, height, camZ)

            // 3. Infinite Scrolling Ground Grid
            draw3DGroundGrid(weather, width, height, camY, camZ)

            // 4. Infinite Scrolling Ceiling Canopy
            draw3DCeilingGrid(width, height, camY, camZ)

            // 5. Scenery (Trees & Clouds)
            val scenerySorted = gameEngine.sceneryItems.sortedByDescending { it.z }
            for (scenery in scenerySorted) {
                drawScenery3D(scenery, camY, camZ, width, height)
            }

            // 6. 3D Pipes with Theme Colors & Ground Shadows
            val pipesSorted = gameEngine.activePipes.sortedByDescending { it.z }
            for (pipe in pipesSorted) {
                drawPipeObstacle3D(pipe, weather, camY, camZ, width, height)
            }

            // 7. 3D Coins with Ground Shadows & Specular Shine
            val coinsSorted = gameEngine.activeCoins.sortedByDescending { it.z }
            for (coin in coinsSorted) {
                drawCoin3D(coin, camY, camZ, width, height)
            }

            // 8. Dynamic Weather Particles (Rain, Snow, Neon Sparks, Sakura)
            drawWeatherParticles3D(gameEngine.weatherParticleItems, weather, camY, camZ, width, height)

            // 9. Speed wind particles
            drawSpeedLines(gameEngine.speedLines, camY, camZ, width, height)
        }

        // 10. First-person Cockpit Wings
        drawFirstPersonWingsAndBeak(width, height, gameEngine.gameTime, skin, propRotation)
    }
}

// --- 3D CANVAS DRAWING FUNCTIONS ---

fun DrawScope.drawSkyBackground(weather: WeatherTheme, width: Float, height: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = weather.skyColors,
            startY = 0f,
            endY = height * 0.65f
        ),
        size = Size(width, height)
    )

    val horizonY = height * 0.5f + 50f

    when (weather.celestialType) {
        CelestialType.SUNSET_SUN -> {
            val sunCenter = Offset(width * 0.5f, horizonY - 40f)
            drawCircle(Color(0xFFFFD54F).copy(alpha = 0.25f), radius = 90.dp.toPx(), center = sunCenter)
            drawCircle(Color(0xFFFFECB3).copy(alpha = 0.6f), radius = 50.dp.toPx(), center = sunCenter)
            drawCircle(Color.White, radius = 28.dp.toPx(), center = sunCenter)
        }
        CelestialType.NOON_SUN -> {
            val sunCenter = Offset(width * 0.7f, height * 0.18f)
            drawCircle(Color(0xFFFFF9C4).copy(alpha = 0.35f), radius = 110.dp.toPx(), center = sunCenter)
            drawCircle(Color(0xFFFFEE58).copy(alpha = 0.8f), radius = 45.dp.toPx(), center = sunCenter)
            drawCircle(Color.White, radius = 26.dp.toPx(), center = sunCenter)
        }
        CelestialType.CYBER_MOON -> {
            val moonCenter = Offset(width * 0.78f, height * 0.22f)
            drawCircle(Color(0xFFE040FB).copy(alpha = 0.35f), radius = 70.dp.toPx(), center = moonCenter)
            drawCircle(Color(0xFF00E5FF), radius = 35.dp.toPx(), center = moonCenter)
            drawCircle(Color(0xFF03001E), radius = 30.dp.toPx(), center = Offset(moonCenter.x - 10f, moonCenter.y - 8f))
        }
        CelestialType.STORM_LIGHTNING -> {
            val cloudCenter = Offset(width * 0.5f, height * 0.22f)
            drawCircle(Color(0xFF263238).copy(alpha = 0.8f), radius = 120.dp.toPx(), center = cloudCenter)
            drawCircle(Color(0xFF37474F).copy(alpha = 0.9f), radius = 90.dp.toPx(), center = Offset(width * 0.32f, height * 0.20f))
            drawCircle(Color(0xFF37474F).copy(alpha = 0.9f), radius = 90.dp.toPx(), center = Offset(width * 0.68f, height * 0.20f))
        }
        CelestialType.AURORA_SKY -> {
            val path = Path().apply {
                moveTo(0f, height * 0.12f)
                cubicTo(width * 0.25f, height * 0.05f, width * 0.5f, height * 0.22f, width, height * 0.08f)
                lineTo(width, height * 0.28f)
                cubicTo(width * 0.5f, height * 0.38f, width * 0.25f, height * 0.22f, 0f, height * 0.30f)
                close()
            }
            drawPath(
                path,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF00BFA5).copy(alpha = 0.5f),
                        Color(0xFF64FFDA).copy(alpha = 0.7f),
                        Color(0xFF1DE9B6).copy(alpha = 0.4f)
                    )
                )
            )
        }
        CelestialType.WINTER_MOON -> {
            val moonCenter = Offset(width * 0.8f, height * 0.18f)
            drawCircle(Color(0xFFE8EAF6).copy(alpha = 0.3f), radius = 60.dp.toPx(), center = moonCenter)
            drawCircle(Color(0xFFFAFAFA), radius = 30.dp.toPx(), center = moonCenter)
        }
        CelestialType.SAKURA_SUN -> {
            val sunCenter = Offset(width * 0.5f, horizonY - 50f)
            drawCircle(Color(0xFFF8BBD0).copy(alpha = 0.4f), radius = 100.dp.toPx(), center = sunCenter)
            drawCircle(Color(0xFFF48FB1).copy(alpha = 0.7f), radius = 55.dp.toPx(), center = sunCenter)
            drawCircle(Color.White, radius = 30.dp.toPx(), center = sunCenter)
        }
    }
}

fun DrawScope.drawParallaxMountains(width: Float, height: Float, camZ: Float) {
    val horizonY = height * 0.5f + 50f

    val farPath = Path().apply {
        moveTo(-width * 2f, horizonY)
        var currentX = -width * 2f
        while (currentX < width * 3f) {
            lineTo(currentX + width * 0.4f, horizonY - 130f)
            lineTo(currentX + width * 0.8f, horizonY)
            currentX += width * 0.8f
        }
        lineTo(width * 3f, horizonY)
        close()
    }
    drawPath(farPath, Color(0xFF311B92).copy(alpha = 0.65f))

    val midPath = Path().apply {
        moveTo(-width * 2f, horizonY)
        var currentX = -width * 2f
        while (currentX < width * 3f) {
            lineTo(currentX + width * 0.5f, horizonY - 180f)
            lineTo(currentX + width * 1.0f, horizonY)
            currentX += width * 1.0f
        }
        lineTo(width * 3f, horizonY)
        close()
    }
    drawPath(midPath, Color(0xFF1A237E).copy(alpha = 0.85f))
}

fun DrawScope.draw3DGroundGrid(weather: WeatherTheme, width: Float, height: Float, camY: Float, camZ: Float) {
    val floorY = -4.5f
    val horizonY = height * 0.5f + 50f

    drawRect(
        brush = Brush.verticalGradient(
            colors = weather.groundColors,
            startY = horizonY,
            endY = height
        ),
        topLeft = Offset(-width, horizonY),
        size = Size(width * 3f, height - horizonY + 200f)
    )

    val lineCount = 12
    val spacingX = 2.5f

    for (i in -lineCount..lineCount) {
        val worldX = i * spacingX
        val pFar = project(worldX, floorY, camZ + 35.0f, width, height, 0f, camY, camZ)
        val pNear = project(worldX, floorY, camZ + 1.2f, width, height, 0f, camY, camZ)
        drawLine(
            color = weather.gridColor,
            start = pFar,
            end = pNear,
            strokeWidth = 2.dp.toPx()
        )
    }

    val spacingZ = 3.5f
    val offsetZ = camZ % spacingZ
    val firstLineZ = camZ - offsetZ + spacingZ

    for (z in 0..12) {
        val lineZ = firstLineZ + (z * spacingZ)
        val pLeft = project(-20f, floorY, lineZ, width, height, 0f, camY, camZ)
        val pRight = project(20f, floorY, lineZ, width, height, 0f, camY, camZ)
        drawLine(
            color = weather.gridColor,
            start = pLeft,
            end = pRight,
            strokeWidth = 1.8.dp.toPx()
        )
    }
}

fun DrawScope.draw3DCeilingGrid(width: Float, height: Float, camY: Float, camZ: Float) {
    val ceilingY = 5.5f
    val colorGrid = Color(0xFF00E5FF).copy(alpha = 0.30f)

    val lineCount = 10
    val spacingX = 2.5f

    for (i in -lineCount..lineCount) {
        val worldX = i * spacingX
        val pFar = project(worldX, ceilingY, camZ + 35.0f, width, height, 0f, camY, camZ)
        val pNear = project(worldX, ceilingY, camZ + 1.2f, width, height, 0f, camY, camZ)
        drawLine(
            color = colorGrid,
            start = pFar,
            end = pNear,
            strokeWidth = 1.5.dp.toPx()
        )
    }

    val spacingZ = 3.5f
    val offsetZ = camZ % spacingZ
    val firstLineZ = camZ - offsetZ + spacingZ

    for (z in 0..12) {
        val lineZ = firstLineZ + (z * spacingZ)
        val pLeft = project(-20f, ceilingY, lineZ, width, height, 0f, camY, camZ)
        val pRight = project(20f, ceilingY, lineZ, width, height, 0f, camY, camZ)
        drawLine(
            color = colorGrid,
            start = pLeft,
            end = pRight,
            strokeWidth = 1.2.dp.toPx()
        )
    }
}

fun DrawScope.drawScenery3D(scenery: SceneryItem, camY: Float, camZ: Float, width: Float, height: Float) {
    if (scenery.z < camZ + 0.5f) return

    when (scenery.type) {
        SceneryType.TREE_TRUNK -> {
            draw3DBox(
                minX = scenery.x - 0.25f, maxX = scenery.x + 0.25f,
                minY = scenery.y, maxY = scenery.y + 1.5f,
                minZ = scenery.z - 0.25f, maxZ = scenery.z + 0.25f,
                baseColor = Color(0xFF6D4C41),
                camY = camY, camZ = camZ,
                width = width, height = height
            )
        }
        SceneryType.TREE_LEAVES -> {
            draw3DBox(
                minX = scenery.x - 0.9f, maxX = scenery.x + 0.9f,
                minY = scenery.y, maxY = scenery.y + 1.2f,
                minZ = scenery.z - 0.9f, maxZ = scenery.z + 0.9f,
                baseColor = Color(0xFF1B5E20),
                camY = camY, camZ = camZ,
                width = width, height = height
            )
            draw3DBox(
                minX = scenery.x - 0.6f, maxX = scenery.x + 0.6f,
                minY = scenery.y + 1.0f, maxY = scenery.y + 1.8f,
                minZ = scenery.z - 0.6f, maxZ = scenery.z + 0.6f,
                baseColor = Color(0xFF4CAF50),
                camY = camY, camZ = camZ,
                width = width, height = height
            )
        }
        SceneryType.CLOUD -> {
            draw3DBox(
                minX = scenery.x - 1.5f, maxX = scenery.x + 1.5f,
                minY = scenery.y, maxY = scenery.y + 0.7f,
                minZ = scenery.z - 0.8f, maxZ = scenery.z + 0.8f,
                baseColor = Color(0xFFECEFF1).copy(alpha = 0.88f),
                camY = camY, camZ = camZ,
                width = width, height = height
            )
        }
        else -> {}
    }
}

fun DrawScope.drawPipeObstacle3D(
    pipe: PipeObstacle,
    weather: WeatherTheme,
    camY: Float,
    camZ: Float,
    width: Float,
    height: Float
) {
    if (pipe.z < camZ + 0.5f) return

    val halfWidth = pipe.width / 2f
    val halfDepth = pipe.depth / 2f
    val halfGap = pipe.gapSize / 2f

    val pipeBottomLimit = -4.5f
    val pipeTopLimit = 5.5f

    // Projected Shadow on Floor for pipe base
    val shadowP1 = project(-halfWidth - 0.2f, -4.5f, pipe.z - halfDepth - 0.2f, width, height, 0f, camY, camZ)
    val shadowP2 = project(halfWidth + 0.2f, -4.5f, pipe.z + halfDepth + 0.2f, width, height, 0f, camY, camZ)
    drawRect(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = Offset(minOf(shadowP1.x, shadowP2.x), minOf(shadowP1.y, shadowP2.y)),
        size = Size(Math.abs(shadowP2.x - shadowP1.x), Math.abs(shadowP2.y - shadowP1.y).coerceAtLeast(10f))
    )

    // 1. Bottom Pipe Column
    val bottomPipeTopY = pipe.gapCenter - halfGap
    draw3DBox(
        minX = -halfWidth, maxX = halfWidth,
        minY = pipeBottomLimit, maxY = bottomPipeTopY - 0.3f,
        minZ = pipe.z - halfDepth, maxZ = pipe.z + halfDepth,
        baseColor = weather.pipeBaseColor,
        camY = camY, camZ = camZ,
        width = width, height = height
    )
    // Bottom Pipe Collar (Lip)
    draw3DBox(
        minX = -halfWidth - 0.14f, maxX = halfWidth + 0.14f,
        minY = bottomPipeTopY - 0.35f, maxY = bottomPipeTopY,
        minZ = pipe.z - halfDepth - 0.08f, maxZ = pipe.z + halfDepth + 0.08f,
        baseColor = weather.pipeCollarColor,
        camY = camY, camZ = camZ,
        width = width, height = height
    )

    // 2. Top Pipe Column
    val topPipeBottomY = pipe.gapCenter + halfGap
    draw3DBox(
        minX = -halfWidth, maxX = halfWidth,
        minY = topPipeBottomY + 0.3f, maxY = pipeTopLimit,
        minZ = pipe.z - halfDepth, maxZ = pipe.z + halfDepth,
        baseColor = weather.pipeBaseColor.darken(0.12f),
        camY = camY, camZ = camZ,
        width = width, height = height
    )
    // Top Pipe Collar (Lip)
    draw3DBox(
        minX = -halfWidth - 0.14f, maxX = halfWidth + 0.14f,
        minY = topPipeBottomY, maxY = topPipeBottomY + 0.35f,
        minZ = pipe.z - halfDepth - 0.08f, maxZ = pipe.z + halfDepth + 0.08f,
        baseColor = weather.pipeCollarColor,
        camY = camY, camZ = camZ,
        width = width, height = height
    )
}

fun DrawScope.drawWeatherParticles3D(
    particles: List<WeatherParticleItem>,
    weather: WeatherTheme,
    camY: Float,
    camZ: Float,
    width: Float,
    height: Float
) {
    for (p in particles) {
        val p1 = project(p.x, p.y, p.z, width, height, 0f, camY, camZ)
        if (p1.x in -50f..width + 50f && p1.y in -50f..height + 50f) {
            when (weather.weatherEffect) {
                WeatherEffect.RAIN -> {
                    val p2 = project(p.x, p.y - 0.7f, p.z, width, height, 0f, camY, camZ)
                    drawLine(
                        color = Color(0xFF80DEEA).copy(alpha = 0.75f),
                        start = p1,
                        end = p2,
                        strokeWidth = 2.dp.toPx()
                    )
                }
                WeatherEffect.SNOW -> {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.88f),
                        radius = (p.size * 18f).dp.toPx(),
                        center = p1
                    )
                }
                WeatherEffect.NEON_SPARKS -> {
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.9f),
                        radius = (p.size * 12f).dp.toPx(),
                        center = p1
                    )
                }
                WeatherEffect.SAKURA_PETALS -> {
                    drawCircle(
                        color = Color(0xFFF48FB1).copy(alpha = 0.85f),
                        radius = (p.size * 16f).dp.toPx(),
                        center = p1
                    )
                }
                WeatherEffect.NONE -> {}
            }
        }
    }
}

fun DrawScope.drawCoin3D(coin: CoinObstacle, camY: Float, camZ: Float, width: Float, height: Float) {
    if (coin.z < camZ + 0.5f || coin.collected) return

    val pCoin = project(coin.x, coin.y, coin.z, width, height, 0f, camY, camZ)
    val rz = maxOf(coin.z - camZ, 0.05f)
    val f = 340f * (width / 800f)
    val projRadius = (coin.radius / rz) * f

    if (projRadius > 0.5f && pCoin.x in -projRadius..width + projRadius && pCoin.y in -projRadius..height + projRadius) {
        // Floor Shadow for Coin
        val pShadow = project(coin.x, -4.45f, coin.z, width, height, 0f, camY, camZ)
        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = projRadius * 0.8f,
            center = pShadow
        )

        if (coin.isTunnelCoin) {
            // Neon Cyan & Magenta Special Tunnel Coin
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = projRadius * 1.2f,
                center = pCoin,
                style = Stroke(width = maxOf(2f, projRadius * 0.25f))
            )
            drawCircle(
                color = Color(0xFFD500F9),
                radius = projRadius,
                center = pCoin
            )
            drawCircle(
                color = Color.White,
                radius = projRadius * 0.40f,
                center = pCoin
            )
        } else {
            // Shiny 3D Gold Coin
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = 0.4f),
                radius = projRadius * 1.25f,
                center = pCoin
            )
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = projRadius,
                center = pCoin
            )
            drawCircle(
                color = Color(0xFFFF8F00),
                radius = projRadius,
                center = pCoin,
                style = Stroke(width = maxOf(2f, projRadius * 0.18f))
            )
            drawCircle(
                color = Color(0xFFFFF9C4),
                radius = projRadius * 0.45f,
                center = pCoin
            )
        }
    }
}

fun DrawScope.drawSpeedLines(lines: List<SpeedLine>, camY: Float, camZ: Float, width: Float, height: Float) {
    for (line in lines) {
        val pStart = project(line.x, line.y, camZ + line.z, width, height, 0f, camY, camZ)
        val pEnd = project(line.x, line.y, camZ + line.z + line.length, width, height, 0f, camY, camZ)

        if (pStart.x in 0f..width && pStart.y in 0f..height) {
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = pStart,
                end = pEnd,
                strokeWidth = 1.8.dp.toPx()
            )
        }
    }
}

fun DrawScope.drawFirstPersonWingsAndBeak(
    width: Float,
    height: Float,
    gameTime: Float,
    skin: BirdSkin,
    propRotation: Float
) {
    val flapPhase = sin(gameTime * 18f)

    when (skin) {
        BirdSkin.YELLOW_BIRD -> {
            val wingYOffset = flapPhase * 55f
            val centerY = height * 0.58f

            val leftWingPath = Path().apply {
                moveTo(0f, centerY + 120f)
                cubicTo(
                    width * 0.10f, centerY - 100f + wingYOffset,
                    width * 0.26f, centerY - 90f + wingYOffset,
                    width * 0.34f, centerY - 20f + wingYOffset
                )
                cubicTo(
                    width * 0.25f, centerY + 70f + wingYOffset,
                    width * 0.12f, centerY + 120f,
                    0f, centerY + 200f
                )
                close()
            }
            drawPath(leftWingPath, Color(0xFFFFD54F))
            drawPath(leftWingPath, Color(0xFFFF9800), style = Stroke(width = 3.5.dp.toPx()))

            val rightWingPath = Path().apply {
                moveTo(width, centerY + 120f)
                cubicTo(
                    width * 0.90f, centerY - 100f + wingYOffset,
                    width * 0.74f, centerY - 90f + wingYOffset,
                    width * 0.66f, centerY - 20f + wingYOffset
                )
                cubicTo(
                    width * 0.75f, centerY + 70f + wingYOffset,
                    width * 0.88f, centerY + 120f,
                    width, centerY + 200f
                )
                close()
            }
            drawPath(rightWingPath, Color(0xFFFFD54F))
            drawPath(rightWingPath, Color(0xFFFF9800), style = Stroke(width = 3.5.dp.toPx()))
        }
        BirdSkin.DRONE -> {
            val centerY = height * 0.55f
            drawRect(Color(0xFF37474F), topLeft = Offset(0f, centerY + 80f), size = Size(width * 0.22f, 40f))
            drawRect(Color(0xFF37474F), topLeft = Offset(width * 0.78f, centerY + 80f), size = Size(width * 0.22f, 40f))
            drawCircle(Color(0xFF00E676), radius = 16.dp.toPx(), center = Offset(width * 0.11f, centerY + 100f))
            drawCircle(Color(0xFF00E676), radius = 16.dp.toPx(), center = Offset(width * 0.89f, centerY + 100f))
        }
        else -> {
            val wingYOffset = flapPhase * 55f
            val centerY = height * 0.58f

            val leftWingPath = Path().apply {
                moveTo(0f, centerY + 120f)
                cubicTo(
                    width * 0.10f, centerY - 100f + wingYOffset,
                    width * 0.26f, centerY - 90f + wingYOffset,
                    width * 0.34f, centerY - 20f + wingYOffset
                )
                cubicTo(
                    width * 0.25f, centerY + 70f + wingYOffset,
                    width * 0.12f, centerY + 120f,
                    0f, centerY + 200f
                )
                close()
            }
            drawPath(leftWingPath, skin.baseColor)
            drawPath(leftWingPath, skin.accentColor, style = Stroke(width = 3.5.dp.toPx()))

            val rightWingPath = Path().apply {
                moveTo(width, centerY + 120f)
                cubicTo(
                    width * 0.90f, centerY - 100f + wingYOffset,
                    width * 0.74f, centerY - 90f + wingYOffset,
                    width * 0.66f, centerY - 20f + wingYOffset
                )
                cubicTo(
                    width * 0.75f, centerY + 70f + wingYOffset,
                    width * 0.88f, centerY + 120f,
                    width, centerY + 200f
                )
                close()
            }
            drawPath(rightWingPath, skin.baseColor)
            drawPath(rightWingPath, skin.accentColor, style = Stroke(width = 3.5.dp.toPx()))
        }
    }
}

fun project(
    x: Float, y: Float, z: Float,
    width: Float, height: Float,
    camX: Float, camY: Float, camZ: Float
): Offset {
    val rx = x - camX
    val ry = y - camY
    val rz = maxOf(z - camZ, 0.05f)

    val f = 340f * (width / 800f)
    val px = width / 2f + (rx / rz) * f
    val py = height / 2f - (ry / rz) * f
    return Offset(px, py)
}

fun DrawScope.draw3DBox(
    minX: Float, maxX: Float,
    minY: Float, maxY: Float,
    minZ: Float, maxZ: Float,
    baseColor: Color,
    camY: Float, camZ: Float,
    width: Float, height: Float
) {
    if (maxZ < camZ) return

    val v001 = project(minX, minY, maxZ, width, height, 0f, camY, camZ)
    val v101 = project(maxX, minY, maxZ, width, height, 0f, camY, camZ)
    val v111 = project(maxX, maxY, maxZ, width, height, 0f, camY, camZ)
    val v011 = project(minX, maxY, maxZ, width, height, 0f, camY, camZ)

    val v000 = project(minX, minY, minZ, width, height, 0f, camY, camZ)
    val v100 = project(maxX, minY, minZ, width, height, 0f, camY, camZ)
    val v110 = project(maxX, maxY, minZ, width, height, 0f, camY, camZ)
    val v010 = project(minX, maxY, minZ, width, height, 0f, camY, camZ)

    val topColor = baseColor.lighten(0.22f)
    val sideColor = baseColor.darken(0.18f)
    val bottomColor = baseColor.darken(0.35f)
    val frontColor = baseColor

    if (camY > minY) {
        drawQuad(v001, v101, v100, v000, bottomColor)
    }
    if (camY < maxY) {
        drawQuad(v010, v110, v111, v011, topColor)
    }
    drawQuad(v001, v000, v010, v011, sideColor)
    drawQuad(v100, v101, v111, v110, sideColor)
    drawQuad(v000, v100, v110, v010, frontColor)
}

fun DrawScope.drawQuad(p1: Offset, p2: Offset, p3: Offset, p4: Offset, color: Color) {
    val path = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        lineTo(p4.x, p4.y)
        close()
    }
    drawPath(path, color)
}

fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

// --- UI OVERLAYS & HUD ---

@Composable
fun PlayingHUD(
    score: Int,
    coins: Int,
    playerY: Float,
    speed: Float,
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Coins Counter in Top-Left (Glassmorphism Pill)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$coins",
                color = Color(0xFFFFD54F),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Central Top Score Pill
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 10.dp)
                .shadow(12.dp, CircleShape)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFFFFD54F).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(horizontal = 28.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$score",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
        }

        // Speedometer Pill in Top-Right
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 48.dp)
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${(speed * 18).toInt()}",
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Vertical Altitude Telemetry Gauge
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(44.dp)
                .height(210.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Cloud,
                contentDescription = "Ceiling",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))

            val normHeight = ((playerY + 3.5f) / 7.0f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .padding(horizontal = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .background(Color.White.copy(alpha = 0.25f))
                        .align(Alignment.Center)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-normHeight * 120f).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD54F))
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Terrain,
                contentDescription = "Floor",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
        }

        // Quick Sound Toggle Button
        IconButton(
            onClick = onToggleSound,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .testTag("sound_toggle")
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = "Toggle Sound",
                tint = Color.White
            )
        }
    }
}

@Composable
fun MainMenuOverlay(
    highScore: Int,
    coins: Int,
    unlockedSkins: Set<BirdSkin>,
    selectedSkin: BirdSkin,
    unlockedWeathers: Set<WeatherTheme>,
    selectedWeather: WeatherTheme,
    selectedLanguage: AppLanguage,
    isSoundEnabled: Boolean,
    onStartGame: () -> Unit,
    onSkinSelect: (BirdSkin) -> Unit,
    onWeatherSelect: (WeatherTheme) -> Unit,
    onLanguageSelect: (AppLanguage) -> Unit,
    onToggleSound: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = VEHICLE, 1 = WEATHER

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(20.dp)
    ) {
        // Coin Count Top-Left
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$coins",
                color = Color(0xFFFFD54F),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Mute Button Top-Right
        IconButton(
            onClick = onToggleSound,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .testTag("menu_sound_toggle")
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = "Toggle Sound",
                tint = Color.White
            )
        }

        // Center Content Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "title_bounce")
            val titleScale by infiniteTransition.animateFloat(
                initialValue = 0.96f,
                targetValue = 1.04f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Text(
                text = "FLAPPY 3D",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFFFFD54F),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(titleScale)
                    .shadow(20.dp)
            )
            Text(
                text = "1ST PERSON PILOT",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF00E5FF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // High Score Card
            Box(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${selectedLanguage.getText("high_score")}: ",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$highScore",
                        color = Color(0xFFFFD54F),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // TAP TO FLY Button
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
                modifier = Modifier
                    .testTag("start_game_button")
                    .shadow(16.dp, RoundedCornerShape(32.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF3E2723),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedLanguage.getText("tap_to_fly"),
                    color = Color(0xFF3E2723),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Shop Customization Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text(selectedLanguage.getText("tab_vehicle"), fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFD54F),
                        selectedLabelColor = Color(0xFF3E2723),
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text(selectedLanguage.getText("tab_weather"), fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00E5FF),
                        selectedLabelColor = Color(0xFF00272B),
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            if (selectedTab == 0) {
                // Vehicle Skin Carousel
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(BirdSkin.values()) { skin ->
                        val isSelected = skin == selectedSkin
                        val isUnlocked = unlockedSkins.contains(skin)
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) skin.baseColor.copy(alpha = 0.35f)
                                    else Color.Black.copy(alpha = 0.35f)
                                )
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) skin.baseColor else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { onSkinSelect(skin) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(skin.baseColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = Color.Black.copy(alpha = 0.8f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedLanguage.getSkinName(skin),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isUnlocked) selectedLanguage.getText("status_ready") else "🪙 ${skin.price}",
                                    color = if (isUnlocked) Color(0xFF00E676) else Color(0xFFFFD54F),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            } else {
                // Weather & Time Shop Carousel
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(WeatherTheme.values()) { weather ->
                        val isSelected = weather == selectedWeather
                        val isUnlocked = unlockedWeathers.contains(weather)
                        val weatherIcon = when(weather) {
                            WeatherTheme.SUNSET -> Icons.Default.WbTwilight
                            WeatherTheme.SUNNY_DAY -> Icons.Default.WbSunny
                            WeatherTheme.CYBER_NIGHT -> Icons.Default.NightsStay
                            WeatherTheme.RAINY_STORM -> Icons.Default.Thunderstorm
                            WeatherTheme.AURORA_DAWN -> Icons.Default.AutoAwesome
                            WeatherTheme.SNOWY_WINTER -> Icons.Default.AcUnit
                            WeatherTheme.SAKURA_SPRING -> Icons.Default.LocalFlorist
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.30f)
                                    else Color.Black.copy(alpha = 0.35f)
                                )
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { onWeatherSelect(weather) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = weatherIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF00E5FF) else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedLanguage.getWeatherName(weather),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isUnlocked) selectedLanguage.getText("status_active") else "${weather.price}",
                                    color = if (isUnlocked) Color(0xFF00E5FF) else Color(0xFFFFD54F),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Multi-Language Flag Selector Row
            LanguageSelectorBar(
                selectedLanguage = selectedLanguage,
                onLanguageSelect = onLanguageSelect
            )
        }
    }
}

@Composable
fun LanguageSelectorBar(
    selectedLanguage: AppLanguage,
    onLanguageSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = selectedLanguage.getText("select_language"),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(AppLanguage.values()) { lang ->
                val isSelected = lang == selectedLanguage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) Color(0xFFFFD54F) else Color.Black.copy(alpha = 0.45f)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onLanguageSelect(lang) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lang.displayName,
                        color = if (isSelected) Color(0xFF3E2723) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverOverlay(
    score: Int,
    highScore: Int,
    isNewBest: Boolean,
    coins: Int,
    selectedLanguage: AppLanguage,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 340.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selectedLanguage.getText("game_over"),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFFEF5350),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .shadow(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (medalIcon, medalKey, medalColor) = when {
                        score >= 20 -> Triple(Icons.Default.EmojiEvents, "GOLD CUP", Color(0xFFFFD54F))
                        score >= 8 -> Triple(Icons.Default.MilitaryTech, "SILVER WING", Color(0xFFB0BEC5))
                        else -> Triple(Icons.Default.MilitaryTech, "BRONZE BADGE", Color(0xFFCD7F32))
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = medalIcon,
                            contentDescription = null,
                            tint = medalColor,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Text(
                        text = medalKey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = medalColor,
                        modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = selectedLanguage.getText("score"), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$score",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = selectedLanguage.getText("best"), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isNewBest) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(20.dp).padding(end = 4.dp)
                                    )
                                }
                                Text(
                                    text = "$highScore",
                                    color = if (isNewBest) Color(0xFFFFD54F) else Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    if (isNewBest) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFD54F))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = selectedLanguage.getText("new_record"),
                                color = Color(0xFF3E2723),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onMainMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                        .testTag("menu_button")
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = selectedLanguage.getText("btn_menu"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(start = 6.dp)
                        .testTag("retry_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restart", tint = Color(0xFF3E2723))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = selectedLanguage.getText("btn_retry"), color = Color(0xFF3E2723), fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }
}
