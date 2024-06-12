package com.example.kegelfoss

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.kegelfoss.ui.theme.KegelFOSSTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


/**
 * MainActivity is the entry point of the app. It sets up the UI and the settings manager.
 */

class MainActivity : ComponentActivity() {
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this)
        enableEdgeToEdge()
        setContent {
            KegelFOSSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TabLayout(settingsManager, Modifier.padding(innerPadding))
                }
            }
        }
    }
}


/**
 * SettingsManager is a class that manages the settings of the app.
 * It loads and saves the settings from and to SharedPreferences.
 */

data class Settings(
    var squeezeSeconds: Int = 3,
    var relaxSeconds: Int = 3,
    var reps: Int = 10,
    var vibrationEnabled: Boolean = true,
    var soundEnabled: Boolean = false,
    var darkMode: Boolean = false
)


class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _settingsFlow by lazy { MutableStateFlow(loadSettings()) }
    val settingsFlow: StateFlow<Settings> = _settingsFlow

    fun loadSettings(): Settings {
        return Settings(
            squeezeSeconds = sharedPreferences.getInt("squeezeSeconds", 3),
            relaxSeconds = sharedPreferences.getInt("relaxSeconds", 3),
            reps = sharedPreferences.getInt("reps", 10),
            vibrationEnabled = sharedPreferences.getBoolean("vibrationEnabled", true),
            soundEnabled = sharedPreferences.getBoolean("soundEnabled", false),
            darkMode = sharedPreferences.getBoolean("darkMode", false)
        )
    }

    fun saveSettings(settings: Settings) {
        sharedPreferences.edit()
            .putInt("squeezeSeconds", settings.squeezeSeconds)
            .putInt("relaxSeconds", settings.relaxSeconds)
            .putInt("reps", settings.reps)
            .putBoolean("vibrationEnabled", settings.vibrationEnabled)
            .putBoolean("soundEnabled", settings.soundEnabled)
            .putBoolean("darkMode", settings.darkMode)
            .apply()

        _settingsFlow.value = settings
    }
}


/**
 * TabLayout displays three tabs: Exercise, Stats, and Settings and the content of the selected tab.
 */

@Composable
fun TabLayout(settingsManager: SettingsManager, modifier: Modifier = Modifier) {
    val tabs = listOf(
        TabItem("Exercise", painterResource(R.drawable.check_decagram)),
        TabItem("Stats", painterResource(R.drawable.chart_box)),
        TabItem("Settings", painterResource(R.drawable.cog))
    )
    val (currentTab, setCurrentTab) = remember { mutableIntStateOf(0) }
    Column(modifier = modifier) {
        TabRow(selectedTabIndex = currentTab) {
            tabs.forEachIndexed { index, tabItem ->
                Tab(
                    text = { Text(tabItem.title) },
                    icon = { Icon(tabItem.icon, contentDescription = null) },
                    selected = currentTab == index,
                    onClick = { setCurrentTab(index) }
                )
            }
        }
        when (currentTab) {
            0 -> ExerciseScreen(settingsManager)
            1 -> StatsScreen()
            2 -> SettingsScreen(settingsManager)
        }
    }
}


@Composable
fun ExerciseScreen(settingsManager: SettingsManager) {
    val settings = settingsManager.loadSettings()

    if (settings.vibrationEnabled) {
        // TODO: Add code to vibrate the device
        Unit
    }
}

@Composable
fun StatsScreen() {
}


/**
 * SettingsScreen displays the settings of the app and allows the user to change them.
 */

@Composable
fun SettingsScreen(settingsManager: SettingsManager) {
    val settings by settingsManager.settingsFlow.collectAsState()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Vibration")
        Switch(
            checked = settings.vibrationEnabled,
            onCheckedChange = { isChecked ->
                val newSettings = settings.copy(vibrationEnabled = isChecked)
                settingsManager.saveSettings(newSettings)
            }
        )
    }
}

data class TabItem(val title: String, val icon: Painter)
