package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sin

enum class ScreenState {
    MENU, PLAYING, GAMEOVER
}

enum class AppLanguage(val code: String, val displayName: String) {
    KOREAN("ko", "한국어"),
    ENGLISH("en", "English"),
    JAPANESE("ja", "日本語"),
    CHINESE("zh", "中文"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch")
}

fun AppLanguage.getSkinName(skin: BirdSkin): String = when(this) {
    AppLanguage.KOREAN -> when(skin) {
        BirdSkin.YELLOW_BIRD -> "카나리아 비행기"
        BirdSkin.DRONE -> "사이버 드론"
        BirdSkin.PIXEL_BAT -> "픽셀 박쥐"
        BirdSkin.NIGHT_FURY -> "나이트 퓨리"
        BirdSkin.SPACE_ROCKET -> "스타 로켓"
        BirdSkin.PHOENIX -> "불사조 호크"
        BirdSkin.GOLDEN_EAGLE -> "황금 독수리"
        BirdSkin.RAINBOW_PEGASUS -> "무지개 페가수스"
    }
    AppLanguage.ENGLISH -> skin.displayName
    AppLanguage.JAPANESE -> when(skin) {
        BirdSkin.YELLOW_BIRD -> "イエローバード"
        BirdSkin.DRONE -> "サイバードローン"
        BirdSkin.PIXEL_BAT -> "ピクセルバット"
        BirdSkin.NIGHT_FURY -> "ナイトフューリー"
        BirdSkin.SPACE_ROCKET -> "スターロケット"
        BirdSkin.PHOENIX -> "フェニックス"
        BirdSkin.GOLDEN_EAGLE -> "ゴールデンイーグル"
        BirdSkin.RAINBOW_PEGASUS -> "レインボーペガサス"
    }
    AppLanguage.CHINESE -> when(skin) {
        BirdSkin.YELLOW_BIRD -> "黄蜂战机"
        BirdSkin.DRONE -> "赛博无人机"
        BirdSkin.PIXEL_BAT -> "像素蝠"
        BirdSkin.NIGHT_FURY -> "夜煞战机"
        BirdSkin.SPACE_ROCKET -> "星际火箭"
        BirdSkin.PHOENIX -> "不死鸟"
        BirdSkin.GOLDEN_EAGLE -> "黄金猎鹰"
        BirdSkin.RAINBOW_PEGASUS -> "彩虹飞马"
    }
    AppLanguage.SPANISH -> when(skin) {
        BirdSkin.YELLOW_BIRD -> "Pájaro Amarillo"
        BirdSkin.DRONE -> "Dron Ciber"
        BirdSkin.PIXEL_BAT -> "Murciélago Píxel"
        BirdSkin.NIGHT_FURY -> "Furia Nocturna"
        BirdSkin.SPACE_ROCKET -> "Cohete Estelar"
        BirdSkin.PHOENIX -> "Fénix"
        BirdSkin.GOLDEN_EAGLE -> "Águila Dorada"
        BirdSkin.RAINBOW_PEGASUS -> "Pégaso Arcoíris"
    }
    AppLanguage.FRENCH -> when(skin) {
        BirdSkin.YELLOW_BIRD -> "Oiseau Jaune"
        BirdSkin.DRONE -> "Drone Cyber"
        BirdSkin.PIXEL_BAT -> "Chauve-souris Pixel"
        BirdSkin.NIGHT_FURY -> "Furie Nocturne"
        BirdSkin.SPACE_ROCKET -> "Fusée Stellaire"
        BirdSkin.PHOENIX -> "Phénix"
        BirdSkin.GOLDEN_EAGLE -> "Aigle d'Or"
        BirdSkin.RAINBOW_PEGASUS -> "Pégase Arc-en-ciel"
    }
    AppLanguage.GERMAN -> when(skin) {
        BirdSkin.YELLOW_BIRD -> "Gelber Vogel"
        BirdSkin.DRONE -> "Cyber-Drohne"
        BirdSkin.PIXEL_BAT -> "Pixel-Fledermaus"
        BirdSkin.NIGHT_FURY -> "Nachtschatten"
        BirdSkin.SPACE_ROCKET -> "Sternenrakete"
        BirdSkin.PHOENIX -> "Phönix"
        BirdSkin.GOLDEN_EAGLE -> "Goldener Adler"
        BirdSkin.RAINBOW_PEGASUS -> "Regenbogen-Pegasus"
    }
}

fun AppLanguage.getWeatherName(weather: WeatherTheme): String = when(this) {
    AppLanguage.KOREAN -> weather.displayName
    AppLanguage.ENGLISH -> when(weather) {
        WeatherTheme.SUNSET -> "Sunset Glow"
        WeatherTheme.SUNNY_DAY -> "Clear Day"
        WeatherTheme.CYBER_NIGHT -> "Cyber Night"
        WeatherTheme.RAINY_STORM -> "Stormy Rain"
        WeatherTheme.AURORA_DAWN -> "Aurora Dawn"
        WeatherTheme.SNOWY_WINTER -> "Snowy Winter"
        WeatherTheme.SAKURA_SPRING -> "Sakura Spring"
    }
    AppLanguage.JAPANESE -> when(weather) {
        WeatherTheme.SUNSET -> "夕焼けの夕日"
        WeatherTheme.SUNNY_DAY -> "快晴の昼"
        WeatherTheme.CYBER_NIGHT -> "サイバーナイト"
        WeatherTheme.RAINY_STORM -> "暴風雨"
        WeatherTheme.AURORA_DAWN -> "オーロラの夜明け"
        WeatherTheme.SNOWY_WINTER -> "雪国の冬"
        WeatherTheme.SAKURA_SPRING -> "桜の春"
    }
    AppLanguage.CHINESE -> when(weather) {
        WeatherTheme.SUNSET -> "日落余晖"
        WeatherTheme.SUNNY_DAY -> "晴朗白昼"
        WeatherTheme.CYBER_NIGHT -> "赛博之夜"
        WeatherTheme.RAINY_STORM -> "暴风雨"
        WeatherTheme.AURORA_DAWN -> "极光黎明"
        WeatherTheme.SNOWY_WINTER -> "雪景寒冬"
        WeatherTheme.SAKURA_SPRING -> "樱花盛春"
    }
    AppLanguage.SPANISH -> when(weather) {
        WeatherTheme.SUNSET -> "Atardecer"
        WeatherTheme.SUNNY_DAY -> "Día Despejado"
        WeatherTheme.CYBER_NIGHT -> "Noche Ciber"
        WeatherTheme.RAINY_STORM -> "Tormenta"
        WeatherTheme.AURORA_DAWN -> "Aurora de Alba"
        WeatherTheme.SNOWY_WINTER -> "Invierno Nevado"
        WeatherTheme.SAKURA_SPRING -> "Primavera Sakura"
    }
    AppLanguage.FRENCH -> when(weather) {
        WeatherTheme.SUNSET -> "Coucher de Soleil"
        WeatherTheme.SUNNY_DAY -> "Jour Dégagé"
        WeatherTheme.CYBER_NIGHT -> "Nuit Cyber"
        WeatherTheme.RAINY_STORM -> "Tempête"
        WeatherTheme.AURORA_DAWN -> "Aurore Polaire"
        WeatherTheme.SNOWY_WINTER -> "Hiver Enneigé"
        WeatherTheme.SAKURA_SPRING -> "Printemps Sakura"
    }
    AppLanguage.GERMAN -> when(weather) {
        WeatherTheme.SUNSET -> "Sonnenuntergang"
        WeatherTheme.SUNNY_DAY -> "Klarer Tag"
        WeatherTheme.CYBER_NIGHT -> "Cyber-Nacht"
        WeatherTheme.RAINY_STORM -> "Gewittersturm"
        WeatherTheme.AURORA_DAWN -> "Polarlicht"
        WeatherTheme.SNOWY_WINTER -> "Verschneiter Winter"
        WeatherTheme.SAKURA_SPRING -> "Kirschblütenfrühling"
    }
}

fun AppLanguage.getText(key: String): String = when(this) {
    AppLanguage.KOREAN -> when(key) {
        "tap_to_fly" -> "비행 시작"
        "high_score" -> "최고 기록"
        "tab_vehicle" -> "기체 스킨"
        "tab_weather" -> "날씨 / 시간"
        "status_ready" -> "READY"
        "status_active" -> "ACTIVE"
        "game_over" -> "FLIGHT TERMINATED"
        "score" -> "SCORE"
        "best" -> "BEST"
        "new_record" -> "NEW RECORD!"
        "btn_menu" -> "MENU"
        "btn_retry" -> "RETRY"
        "select_language" -> "언어 선택"
        else -> key
    }
    AppLanguage.ENGLISH -> when(key) {
        "tap_to_fly" -> "TAP TO FLY"
        "high_score" -> "HIGH SCORE"
        "tab_vehicle" -> "VEHICLES"
        "tab_weather" -> "WEATHER"
        "status_ready" -> "READY"
        "status_active" -> "ACTIVE"
        "game_over" -> "FLIGHT TERMINATED"
        "score" -> "SCORE"
        "best" -> "BEST"
        "new_record" -> "NEW RECORD!"
        "btn_menu" -> "MENU"
        "btn_retry" -> "RETRY"
        "select_language" -> "Language"
        else -> key
    }
    AppLanguage.JAPANESE -> when(key) {
        "tap_to_fly" -> "タップで飛行"
        "high_score" -> "ハイスコア"
        "tab_vehicle" -> "機体スキン"
        "tab_weather" -> "天気 / 時間"
        "status_ready" -> "READY"
        "status_active" -> "ACTIVE"
        "game_over" -> "飛行終了"
        "score" -> "スコア"
        "best" -> "ベスト"
        "new_record" -> "新記録!"
        "btn_menu" -> "メニュー"
        "btn_retry" -> "リトライ"
        "select_language" -> "言語 (Language)"
        else -> key
    }
    AppLanguage.CHINESE -> when(key) {
        "tap_to_fly" -> "点击起飞"
        "high_score" -> "最高纪录"
        "tab_vehicle" -> "载具皮肤"
        "tab_weather" -> "天气 / 时间"
        "status_ready" -> "就绪"
        "status_active" -> "使用中"
        "game_over" -> "飞行结束"
        "score" -> "得分"
        "best" -> "最佳"
        "new_record" -> "新纪录!"
        "btn_menu" -> "主菜单"
        "btn_retry" -> "重新开始"
        "select_language" -> "语言 (Language)"
        else -> key
    }
    AppLanguage.SPANISH -> when(key) {
        "tap_to_fly" -> "TOCAR PARA VOLAR"
        "high_score" -> "MEJOR PUNTUACIÓN"
        "tab_vehicle" -> "NAVES"
        "tab_weather" -> "CLIMA"
        "status_ready" -> "LISTO"
        "status_active" -> "ACTIVO"
        "game_over" -> "VUELO TERMINADO"
        "score" -> "PUNTOS"
        "best" -> "MEJOR"
        "new_record" -> "¡NUEVO RÉCORD!"
        "btn_menu" -> "MENÚ"
        "btn_retry" -> "REINTENTAR"
        "select_language" -> "Idioma (Language)"
        else -> key
    }
    AppLanguage.FRENCH -> when(key) {
        "tap_to_fly" -> "TOUCHER POUR VOLER"
        "high_score" -> "MEILLEUR SCORE"
        "tab_vehicle" -> "VAISSEAUX"
        "tab_weather" -> "MÉTÉO"
        "status_ready" -> "PRÊT"
        "status_active" -> "ACTIF"
        "game_over" -> "VOL TERMINÉ"
        "score" -> "SCORE"
        "best" -> "MEILLEUR"
        "new_record" -> "NOUVEAU RECORD!"
        "btn_menu" -> "MENU"
        "btn_retry" -> "RÉESSAYER"
        "select_language" -> "Langue (Language)"
        else -> key
    }
    AppLanguage.GERMAN -> when(key) {
        "tap_to_fly" -> "TIPPEN ZUM FLIEGEN"
        "high_score" -> "REKORD"
        "tab_vehicle" -> "RAUMSCHIFFE"
        "tab_weather" -> "WETTER"
        "status_ready" -> "BEREIT"
        "status_active" -> "AKTIV"
        "game_over" -> "FLUG BEENDET"
        "score" -> "PUNKTE"
        "best" -> "REKORD"
        "new_record" -> "NEUER REKORD!"
        "btn_menu" -> "MENÜ"
        "btn_retry" -> "NOCHMAL"
        "select_language" -> "Sprache (Language)"
        else -> key
    }
}

enum class CelestialType {
    SUNSET_SUN,
    NOON_SUN,
    CYBER_MOON,
    STORM_LIGHTNING,
    AURORA_SKY,
    WINTER_MOON,
    SAKURA_SUN
}

enum class WeatherEffect {
    NONE,
    RAIN,
    SNOW,
    NEON_SPARKS,
    SAKURA_PETALS
}

enum class WeatherTheme(
    val displayName: String,
    val description: String,
    val emoji: String,
    val price: Int,
    val skyColors: List<Color>,
    val groundColors: List<Color>,
    val gridColor: Color,
    val pipeBaseColor: Color,
    val pipeCollarColor: Color,
    val celestialType: CelestialType,
    val weatherEffect: WeatherEffect
) {
    SUNSET(
        displayName = "황혼의 석양 (Sunset)",
        description = "노을빛 석양과 오렌지 하늘",
        emoji = "🌅",
        price = 0,
        skyColors = listOf(
            Color(0xFF0D1B2A),
            Color(0xFF1B263B),
            Color(0xFF4A148C),
            Color(0xFF880E4F),
            Color(0xFFE65100),
            Color(0xFFFFB300)
        ),
        groundColors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF4CAF50)),
        gridColor = Color(0xFF81C784).copy(alpha = 0.45f),
        pipeBaseColor = Color(0xFF00C853),
        pipeCollarColor = Color(0xFF00E5FF),
        celestialType = CelestialType.SUNSET_SUN,
        weatherEffect = WeatherEffect.NONE
    ),
    SUNNY_DAY(
        displayName = "쾌청한 낮 (Clear Day)",
        description = "맑고 푸른 하늘과 눈부신 햇살",
        emoji = "☀️",
        price = 0,
        skyColors = listOf(
            Color(0xFF0277BD),
            Color(0xFF039BE5),
            Color(0xFF29B6F6),
            Color(0xFF4FC3F7),
            Color(0xFF81D4FA),
            Color(0xFFE0F7FA)
        ),
        groundColors = listOf(Color(0xFF2E7D32), Color(0xFF388E3C), Color(0xFF66BB6A)),
        gridColor = Color(0xFFA5D6A7).copy(alpha = 0.50f),
        pipeBaseColor = Color(0xFF43A047),
        pipeCollarColor = Color(0xFFFFD54F),
        celestialType = CelestialType.NOON_SUN,
        weatherEffect = WeatherEffect.NONE
    ),
    CYBER_NIGHT(
        displayName = "사이버 나이트 (Cyber Night)",
        description = "네온빛 가득한 딥블루 밤하늘",
        emoji = "🌃",
        price = 45,
        skyColors = listOf(
            Color(0xFF03001E),
            Color(0xFF7303C0),
            Color(0xFFEC38BC),
            Color(0xFF00D2FF)
        ),
        groundColors = listOf(Color(0xFF0F0C20), Color(0xFF1A103C), Color(0xFF2D1B69)),
        gridColor = Color(0xFFFF007F).copy(alpha = 0.60f),
        pipeBaseColor = Color(0xFF7B1FA2),
        pipeCollarColor = Color(0xFF00E5FF),
        celestialType = CelestialType.CYBER_MOON,
        weatherEffect = WeatherEffect.NEON_SPARKS
    ),
    RAINY_STORM(
        displayName = "폭풍우 (Stormy Rain)",
        description = "시원하게 쏟아지는 빗줄기",
        emoji = "🌧️",
        price = 60,
        skyColors = listOf(
            Color(0xFF101419),
            Color(0xFF1C232B),
            Color(0xFF2C353F),
            Color(0xFF3A4750)
        ),
        groundColors = listOf(Color(0xFF1B242A), Color(0xFF23313B), Color(0xFF2E3E4C)),
        gridColor = Color(0xFF64B5F6).copy(alpha = 0.40f),
        pipeBaseColor = Color(0xFF37474F),
        pipeCollarColor = Color(0xFF00E5FF),
        celestialType = CelestialType.STORM_LIGHTNING,
        weatherEffect = WeatherEffect.RAIN
    ),
    AURORA_DAWN(
        displayName = "오로라 새벽 (Aurora)",
        description = "신비롭고 아름다운 에메랄드 오로라",
        emoji = "🌌",
        price = 85,
        skyColors = listOf(
            Color(0xFF050515),
            Color(0xFF0B192C),
            Color(0xFF004D40),
            Color(0xFF00BFA5),
            Color(0xFF64FFDA)
        ),
        groundColors = listOf(Color(0xFF00272B), Color(0xFF004D40), Color(0xFF00796B)),
        gridColor = Color(0xFF64FFDA).copy(alpha = 0.50f),
        pipeBaseColor = Color(0xFF00897B),
        pipeCollarColor = Color(0xFFE040FB),
        celestialType = CelestialType.AURORA_SKY,
        weatherEffect = WeatherEffect.NONE
    ),
    SNOWY_WINTER(
        displayName = "설원 겨울 (Snowy Winter)",
        description = "하늘에서 살포시 날리는 하얀 눈",
        emoji = "❄️",
        price = 110,
        skyColors = listOf(
            Color(0xFF1A237E),
            Color(0xFF283593),
            Color(0xFF3F51B5),
            Color(0xFF7986CB),
            Color(0xFFC5CAE9)
        ),
        groundColors = listOf(Color(0xFF37474F), Color(0xFF78909C), Color(0xFFECEFF1)),
        gridColor = Color(0xFFE0F7FA).copy(alpha = 0.55f),
        pipeBaseColor = Color(0xFF0288D1),
        pipeCollarColor = Color(0xFFFFFFFF),
        celestialType = CelestialType.WINTER_MOON,
        weatherEffect = WeatherEffect.SNOW
    ),
    SAKURA_SPRING(
        displayName = "벚꽃 봄 (Sakura Spring)",
        description = "살랑이는 바람에 흩날리는 벚꽃잎",
        emoji = "🌸",
        price = 140,
        skyColors = listOf(
            Color(0xFF4A154B),
            Color(0xFF6A1B9A),
            Color(0xFFAB47BC),
            Color(0xFFF48FB1),
            Color(0xFFF8BBD0)
        ),
        groundColors = listOf(Color(0xFF4A148C), Color(0xFF880E4F), Color(0xFFC2185B)),
        gridColor = Color(0xFFFF4081).copy(alpha = 0.50f),
        pipeBaseColor = Color(0xFFD81B60),
        pipeCollarColor = Color(0xFFFF80AB),
        celestialType = CelestialType.SAKURA_SUN,
        weatherEffect = WeatherEffect.SAKURA_PETALS
    )
}

enum class BirdSkin(val displayName: String, val baseColor: Color, val accentColor: Color, val price: Int) {
    YELLOW_BIRD("Yellow Bird", Color(0xFFFFD54F), Color(0xFFFF9800), 0),
    DRONE("Cyber Drone", Color(0xFFB0BEC5), Color(0xFF00E676), 15),
    PIXEL_BAT("Pixel Bat", Color(0xFF7E57C2), Color(0xFFE040FB), 35),
    NIGHT_FURY("Night Fury", Color(0xFF212121), Color(0xFF2979FF), 50),
    SPACE_ROCKET("Star Rocket", Color(0xFFEF5350), Color(0xFF00E5FF), 75),
    PHOENIX("Phoenix", Color(0xFFFF3D00), Color(0xFFFFEA00), 110),
    GOLDEN_EAGLE("Golden Eagle", Color(0xFFFFD700), Color(0xFFFFFFFF), 160),
    RAINBOW_PEGASUS("Rainbow Pegasus", Color(0xFFE91E63), Color(0xFF00E5FF), 220)
}

data class CoinObstacle(
    val id: Int,
    val x: Float,
    val y: Float,
    val z: Float,
    var collected: Boolean = false,
    val radius: Float = 0.35f,
    val isTunnelCoin: Boolean = false
)

data class SceneryItem(
    val x: Float,
    val y: Float,
    val z: Float,
    val type: SceneryType,
    val scale: Float = 1.0f
)

enum class SceneryType {
    TREE_TRUNK, TREE_LEAVES, CLOUD, SIDE_PILLAR
}

data class PipeObstacle(
    val id: Int,
    val z: Float,
    val gapCenter: Float,
    val gapSize: Float = 2.1f,
    val width: Float = 1.6f,
    val depth: Float = 0.8f,
    var scored: Boolean = false
)

data class SpeedLine(
    var x: Float,
    var y: Float,
    var z: Float,
    var length: Float,
    var speed: Float
)

data class WeatherParticleItem(
    var x: Float,
    var y: Float,
    var z: Float,
    var speedY: Float,
    var speedZ: Float,
    var size: Float,
    var angle: Float = 0f
)

class GameEngine(private val context: Context) {
    private val prefs = context.getSharedPreferences("flappy3d_prefs", Context.MODE_PRIVATE)

    // Game states exposed to UI
    private val _screenState = MutableStateFlow(ScreenState.MENU)
    val screenState: StateFlow<ScreenState> = _screenState

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _highScore = MutableStateFlow(prefs.getInt("high_score", 0))
    val highScore: StateFlow<Int> = _highScore

    private val _isNewHighScore = MutableStateFlow(false)
    val isNewHighScore: StateFlow<Boolean> = _isNewHighScore

    private val _selectedSkin = MutableStateFlow(
        BirdSkin.valueOf(prefs.getString("selected_skin", BirdSkin.YELLOW_BIRD.name) ?: BirdSkin.YELLOW_BIRD.name)
    )
    val selectedSkin: StateFlow<BirdSkin> = _selectedSkin

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled

    private val _playCount = MutableStateFlow(prefs.getInt("play_count", 0))
    val playCount: StateFlow<Int> = _playCount

    // Coins & Skin Shop properties
    private val _coins = MutableStateFlow(prefs.getInt("total_coins", 0))
    val coins: StateFlow<Int> = _coins

    private val _unlockedSkins = MutableStateFlow<Set<BirdSkin>>(setOf(BirdSkin.YELLOW_BIRD))
    val unlockedSkins: StateFlow<Set<BirdSkin>> = _unlockedSkins

    private val _comboStreak = MutableStateFlow(0)
    val comboStreak: StateFlow<Int> = _comboStreak

    private val _runCoins = MutableStateFlow(0)
    val runCoins: StateFlow<Int> = _runCoins

    // Weather & Time of Day shop properties
    private val _selectedWeather = MutableStateFlow(
        try {
            WeatherTheme.valueOf(prefs.getString("selected_weather", WeatherTheme.SUNNY_DAY.name) ?: WeatherTheme.SUNNY_DAY.name)
        } catch (e: Exception) { WeatherTheme.SUNNY_DAY }
    )
    val selectedWeather: StateFlow<WeatherTheme> = _selectedWeather

    private val _unlockedWeathers = MutableStateFlow<Set<WeatherTheme>>(getSavedUnlockedWeathers())
    val unlockedWeathers: StateFlow<Set<WeatherTheme>> = _unlockedWeathers

    // Language property
    private val _selectedLanguage = MutableStateFlow(
        try {
            AppLanguage.valueOf(prefs.getString("selected_language", AppLanguage.KOREAN.name) ?: AppLanguage.KOREAN.name)
        } catch (e: Exception) { AppLanguage.KOREAN }
    )
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage

    fun setLanguage(lang: AppLanguage) {
        _selectedLanguage.value = lang
        prefs.edit().putString("selected_language", lang.name).apply()
        RetroAudioSynthesizer.playCoin()
    }

    // Physical values
    var playerY = 0.0f
    var playerVelocityY = 0.0f
    var playerZ = 0.0f
    var speedZ = 6.5f

    // Visual elements
    val activePipes = mutableListOf<PipeObstacle>()
    val activeCoins = mutableListOf<CoinObstacle>()
    val sceneryItems = mutableListOf<SceneryItem>()
    val speedLines = mutableListOf<SpeedLine>()
    val weatherParticleItems = mutableListOf<WeatherParticleItem>()

    // Helpers
    var gameTime = 0.0f
    var lastPipeZ = 12.0f
    var pipeIdCounter = 0
    var coinIdCounter = 0
    val playerRadius = 0.32f

    init {
        RetroAudioSynthesizer.setSoundEnabled(_isSoundEnabled.value)
        _unlockedSkins.value = getSavedUnlockedSkins()
        _unlockedWeathers.value = getSavedUnlockedWeathers()
        resetWorld()
    }

    private fun getSavedUnlockedSkins(): Set<BirdSkin> {
        val raw = prefs.getString("unlocked_skins", BirdSkin.YELLOW_BIRD.name) ?: BirdSkin.YELLOW_BIRD.name
        return raw.split(",").mapNotNull {
            try { BirdSkin.valueOf(it) } catch (e: Exception) { null }
        }.toSet()
    }

    private fun saveUnlockedSkins(set: Set<BirdSkin>) {
        val raw = set.joinToString(",") { it.name }
        prefs.edit().putString("unlocked_skins", raw).apply()
    }

    private fun getSavedUnlockedWeathers(): Set<WeatherTheme> {
        val defaultWeathers = "${WeatherTheme.SUNNY_DAY.name},${WeatherTheme.SUNSET.name}"
        val raw = prefs.getString("unlocked_weathers", defaultWeathers) ?: defaultWeathers
        return raw.split(",").mapNotNull {
            try { WeatherTheme.valueOf(it) } catch (e: Exception) { null }
        }.toSet()
    }

    private fun saveUnlockedWeathers(set: Set<WeatherTheme>) {
        val raw = set.joinToString(",") { it.name }
        prefs.edit().putString("unlocked_weathers", raw).apply()
    }

    fun buyWeather(weather: WeatherTheme): Boolean {
        val currentCoins = _coins.value
        if (currentCoins >= weather.price && !_unlockedWeathers.value.contains(weather)) {
            val nextCoins = currentCoins - weather.price
            _coins.value = nextCoins
            prefs.edit().putInt("total_coins", nextCoins).apply()

            val nextSet = _unlockedWeathers.value + weather
            _unlockedWeathers.value = nextSet
            saveUnlockedWeathers(nextSet)

            changeWeather(weather)
            RetroAudioSynthesizer.playRecord()
            return true
        }
        return false
    }

    fun selectOrBuyWeather(weather: WeatherTheme) {
        if (_unlockedWeathers.value.contains(weather)) {
            changeWeather(weather)
        } else {
            buyWeather(weather)
        }
    }

    fun changeWeather(weather: WeatherTheme) {
        _selectedWeather.value = weather
        prefs.edit().putString("selected_weather", weather.name).apply()
        spawnWeatherParticles()
    }

    fun buySkin(skin: BirdSkin): Boolean {
        val currentCoins = _coins.value
        if (currentCoins >= skin.price && !_unlockedSkins.value.contains(skin)) {
            val nextCoins = currentCoins - skin.price
            _coins.value = nextCoins
            prefs.edit().putInt("total_coins", nextCoins).apply()

            val nextSet = _unlockedSkins.value + skin
            _unlockedSkins.value = nextSet
            saveUnlockedSkins(nextSet)

            changeSkin(skin)
            RetroAudioSynthesizer.playRecord()
            return true
        }
        return false
    }

    fun selectOrBuySkin(skin: BirdSkin) {
        if (_unlockedSkins.value.contains(skin)) {
            changeSkin(skin)
        } else {
            buySkin(skin)
        }
    }

    fun screenStateValue(state: ScreenState) {
        _screenState.value = state
    }

    fun toggleSound() {
        val next = !_isSoundEnabled.value
        _isSoundEnabled.value = next
        prefs.edit().putBoolean("sound_enabled", next).apply()
        RetroAudioSynthesizer.setSoundEnabled(next)
        if (next) {
            RetroAudioSynthesizer.startMusicLoop()
        }
    }

    fun changeSkin(skin: BirdSkin) {
        _selectedSkin.value = skin
        prefs.edit().putString("selected_skin", skin.name).apply()
    }

    fun startGame() {
        resetWorld()
        _screenState.value = ScreenState.PLAYING
        _isNewHighScore.value = false
    }

    fun tapToFlap() {
        if (_screenState.value == ScreenState.PLAYING) {
            playerVelocityY = 4.6f // upward jump impulse
            RetroAudioSynthesizer.playFlap()
        }
    }

    fun resetWorld() {
        playerY = 0.0f
        playerVelocityY = 0.0f
        playerZ = 0.0f
        speedZ = 6.5f
        _score.value = 0
        gameTime = 0.0f
        lastPipeZ = 12.0f
        pipeIdCounter = 0
        coinIdCounter = 0

        activePipes.clear()
        activeCoins.clear()
        sceneryItems.clear()
        speedLines.clear()

        // Generate initial scenery, pipes & weather particles
        generateInitialWorld()
        spawnWeatherParticles()
    }

    fun spawnWeatherParticles() {
        weatherParticleItems.clear()
        val theme = _selectedWeather.value
        val count = when (theme.weatherEffect) {
            WeatherEffect.RAIN -> 40
            WeatherEffect.SNOW -> 30
            WeatherEffect.NEON_SPARKS -> 25
            WeatherEffect.SAKURA_PETALS -> 30
            WeatherEffect.NONE -> 0
        }
        for (i in 0 until count) {
            weatherParticleItems.add(
                WeatherParticleItem(
                    x = (Math.random().toFloat() * 12f - 6f),
                    y = (Math.random().toFloat() * 8f - 3f),
                    z = playerZ + (Math.random().toFloat() * 35f),
                    speedY = when (theme.weatherEffect) {
                        WeatherEffect.RAIN -> -(Math.random().toFloat() * 8f + 10f)
                        WeatherEffect.SNOW -> -(Math.random().toFloat() * 1.5f + 1.2f)
                        WeatherEffect.NEON_SPARKS -> (Math.random().toFloat() * 1.2f - 0.6f)
                        WeatherEffect.SAKURA_PETALS -> -(Math.random().toFloat() * 1.8f + 1.0f)
                        WeatherEffect.NONE -> 0f
                    },
                    speedZ = -(Math.random().toFloat() * 2f + 1f),
                    size = Math.random().toFloat() * 0.25f + 0.15f,
                    angle = Math.random().toFloat() * 360f
                )
            )
        }
    }

    private fun generateInitialWorld() {
        // Spawning some initial trees and clouds
        for (z in 5..60 step 6) {
            spawnSceneryAtZ(z.toFloat())
        }

        // Spawn first 4 pipes
        for (i in 0 until 4) {
            spawnPipe()
        }

        // Spawn speed lines
        for (i in 0 until 12) {
            speedLines.add(
                SpeedLine(
                    x = (Math.random().toFloat() * 10f - 5f),
                    y = (Math.random().toFloat() * 6f - 3f),
                    z = (Math.random().toFloat() * 30f),
                    length = (Math.random().toFloat() * 1.5f + 0.5f),
                    speed = (Math.random().toFloat() * 2f + 5f)
                )
            )
        }
    }

    private fun spawnSceneryAtZ(z: Float) {
        // Left tree
        sceneryItems.add(SceneryItem(-5.2f, -4.5f, z, SceneryType.TREE_TRUNK))
        sceneryItems.add(SceneryItem(-5.2f, -3.1f, z, SceneryType.TREE_LEAVES))

        // Right tree
        sceneryItems.add(SceneryItem(5.2f, -4.5f, z, SceneryType.TREE_TRUNK))
        sceneryItems.add(SceneryItem(5.2f, -3.1f, z, SceneryType.TREE_LEAVES))

        // Cloud in sky (alternating left/right)
        val cloudX = if (z.toInt() % 12 == 0) -6.5f else 6.5f
        val cloudY = 4.0f + sin(z) * 0.5f
        sceneryItems.add(SceneryItem(cloudX, cloudY, z, SceneryType.CLOUD, scale = 1.2f))
    }

    private fun spawnPipe() {
        // Pipes are spaced every 12.0f
        val z = lastPipeZ
        lastPipeZ += 12.0f

        // Randomize the height of the gap center (safe zone shifted higher)
        val gapCenter = (Math.random().toFloat() * 2.8f) - 0.4f

        activePipes.add(
            PipeObstacle(
                id = pipeIdCounter++,
                z = z,
                gapCenter = gapCenter,
                gapSize = 4.4f
            )
        )

        // 40% chance to spawn a coin right in the center of the pipe gap (25% chance of special Tunnel Coin!)
        if (Math.random() < 0.40) {
            val isTunnel = Math.random() < 0.25
            activeCoins.add(
                CoinObstacle(
                    id = coinIdCounter++,
                    x = 0f,
                    y = gapCenter,
                    z = z,
                    radius = if (isTunnel) 0.45f else 0.35f,
                    isTunnelCoin = isTunnel
                )
            )
        }
    }

    fun update(deltaTime: Float) {
        if (_screenState.value != ScreenState.PLAYING) return

        gameTime += deltaTime

        // 1. Player Physics (Forward speed & vertical gravity)
        playerZ += speedZ * deltaTime
        playerY += playerVelocityY * deltaTime
        playerVelocityY += -12.5f * deltaTime // Gravity acceleration

        // Cap vertical velocity to avoid super fast falls
        if (playerVelocityY < -15f) playerVelocityY = -15f

        // Increase forward speed dynamically over time to increase difficulty (maintaining max speed)
        speedZ = 6.5f + (playerZ * 0.015f).coerceAtMost(11.5f)

        // 2. Bound constraints (Floor & Ceiling collision) - Widen vertical limits to lower sensitivity
        val floorY = -4.5f
        val ceilingY = 5.5f
        if (playerY - playerRadius <= floorY || playerY + playerRadius >= ceilingY) {
            triggerGameOver()
            return
        }

        // 3. Keep speed lines moving & recycling
        for (line in speedLines) {
            line.z -= line.speed * deltaTime
            if (line.z < 0) {
                line.z = 25f + Math.random().toFloat() * 10f
                line.x = (Math.random().toFloat() * 10f - 5f)
                line.y = (Math.random().toFloat() * 6f - 3f)
            }
        }

        // 3a. Update weather particles
        val effect = _selectedWeather.value.weatherEffect
        if (effect != WeatherEffect.NONE) {
            for (p in weatherParticleItems) {
                p.y += p.speedY * deltaTime
                p.z += p.speedZ * deltaTime
                if (effect == WeatherEffect.SAKURA_PETALS || effect == WeatherEffect.SNOW) {
                    p.x += sin(gameTime * 2f + p.z) * 0.4f * deltaTime
                    p.angle += 45f * deltaTime
                }
                // Recycle particle when it moves out of view or below floor
                if (p.y < -4.5f || p.z < playerZ - 2f) {
                    p.x = (Math.random().toFloat() * 12f - 6f)
                    p.y = 4.5f + Math.random().toFloat() * 2f
                    p.z = playerZ + 25f + Math.random().toFloat() * 10f
                }
            }
        }

        // 3b. Recycle coins and detect coin collections
        activeCoins.removeAll { coin ->
            val behind = coin.z < playerZ - 8.0f
            if (behind) return@removeAll true

            if (!coin.collected) {
                val distZ = Math.abs(playerZ - coin.z)
                val distY = Math.abs(playerY - coin.y)
                if (distZ < playerRadius + coin.radius && distY < playerRadius + coin.radius) {
                    coin.collected = true
                    val reward = if (coin.isTunnelCoin) 5 else 1
                    val nextCoins = _coins.value + reward
                    _coins.value = nextCoins
                    prefs.edit().putInt("total_coins", nextCoins).apply()
                    RetroAudioSynthesizer.playCoin()
                }
            }
            false
        }

        // 4. Recycle Pipes and Scenery
        // Remove pipes that are far behind the player and generate new ones
        activePipes.removeAll { pipe ->
            val behind = pipe.z < playerZ - 8.0f
            if (behind && !pipe.scored) {
                // If it wasn't scored, mark it scored so we don't accidentally check it again
                pipe.scored = true
            }
            behind
        }

        while (activePipes.size < 5) {
            spawnPipe()
        }

        // Recycle scenery items as player moves forward
        sceneryItems.removeAll { item -> item.z < playerZ - 8.0f }
        // Keep spawning scenery as player travels
        val farthestSceneryZ = sceneryItems.maxOfOrNull { it.z } ?: playerZ
        if (farthestSceneryZ < playerZ + 50.0f) {
            spawnSceneryAtZ(farthestSceneryZ + 6.0f)
        }

        // 5. Score detection
        for (pipe in activePipes) {
            if (!pipe.scored && playerZ > pipe.z) {
                pipe.scored = true
                _score.value += 1
                RetroAudioSynthesizer.playScore()

                // Check for new high score
                if (_score.value > _highScore.value) {
                    _highScore.value = _score.value
                    prefs.edit().putInt("high_score", _score.value).apply()
                    if (!_isNewHighScore.value) {
                        _isNewHighScore.value = true
                        RetroAudioSynthesizer.playRecord()
                    }
                }
            }
        }

        // 6. Collision detection with Pipe obstacles
        for (pipe in activePipes) {
            // Check if player is horizontally overlapping the pipe's depth along Z
            val halfDepth = pipe.depth / 2f
            if (playerZ + playerRadius >= pipe.z - halfDepth && playerZ - playerRadius <= pipe.z + halfDepth) {
                // Check if player is colliding vertically with top or bottom pipe
                val halfGap = pipe.gapSize / 2f
                val pipeBottomY = pipe.gapCenter - halfGap
                val pipeTopY = pipe.gapCenter + halfGap

                if (playerY - playerRadius <= pipeBottomY || playerY + playerRadius >= pipeTopY) {
                    triggerGameOver()
                    return
                }
            }
        }
    }

    private fun triggerGameOver() {
        _screenState.value = ScreenState.GAMEOVER
        RetroAudioSynthesizer.playCrash()
        
        val nextPlayCount = _playCount.value + 1
        _playCount.value = nextPlayCount
        prefs.edit().putInt("play_count", nextPlayCount).apply()
    }
}
