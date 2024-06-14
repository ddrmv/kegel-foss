package com.example.kegelfoss

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.kegelfoss.ui.theme.KegelFOSSTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale


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
            KegelFOSSThemeWithDarkMode(settingsManager) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TabLayout(settingsManager, Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun KegelFOSSThemeWithDarkMode(
    settingsManager: SettingsManager,
    content: @Composable () -> Unit
) {
    val settings by settingsManager.settingsFlow.collectAsState()
    KegelFOSSTheme(darkTheme = settings.darkMode, content = content)
}


/**
 * SettingsManager is a class that manages the settings of the app.
 * It loads and saves the settings from and to SharedPreferences.
 */

data class Settings(
    var squeezeSeconds: Int,
    var relaxSeconds: Int,
    var repetitions: Int,
    var vibrationEnabled: Boolean,
    var soundEnabled: Boolean,
    var darkMode: Boolean,
    var totalTime: Int,
    var completedSets: Int
)


class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _settingsFlow by lazy { MutableStateFlow(loadSettings()) }
    val settingsFlow: StateFlow<Settings> = _settingsFlow

    fun loadSettings(): Settings {
        return Settings(
            squeezeSeconds = sharedPreferences.getInt("squeezeSeconds", 3),
            relaxSeconds = sharedPreferences.getInt("relaxSeconds", 3),
            repetitions = sharedPreferences.getInt("repetitions", 10),
            vibrationEnabled = sharedPreferences.getBoolean("vibrationEnabled", true),
            soundEnabled = sharedPreferences.getBoolean("soundEnabled", false),
            darkMode = sharedPreferences.getBoolean("darkMode", false),
            totalTime = sharedPreferences.getInt("totalTime", 0),
            completedSets = sharedPreferences.getInt("completedSets", 0)
        )
    }

    fun saveSettings(settings: Settings) {
        sharedPreferences.edit()
            .putInt("squeezeSeconds", settings.squeezeSeconds)
            .putInt("relaxSeconds", settings.relaxSeconds)
            .putInt("repetitions", settings.repetitions)
            .putBoolean("vibrationEnabled", settings.vibrationEnabled)
            .putBoolean("soundEnabled", settings.soundEnabled)
            .putBoolean("darkMode", settings.darkMode)
            .putInt("totalTime", settings.totalTime)
            .putInt("completedSets", settings.completedSets)
            .apply()

        _settingsFlow.value = settings
    }
}


/**
 * TabLayout displays three tabs: Exercise, Stats, and Settings and the content of the selected tab.
 */

data class TabItem(val title: String, val icon: Painter)

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
                    text = { Text(text = tabItem.title) },
                    icon = { Icon(tabItem.icon, contentDescription = null) },
                    selected = currentTab == index,
                    onClick = { setCurrentTab(index) }
                )
            }
        }
        when (currentTab) {
            0 -> ExerciseScreen(settingsManager)
            1 -> StatsScreen(settingsManager)
            2 -> SettingsScreen(settingsManager)
        }
    }
}

/**
 * ExerciseScreen displays the exercise screen, which is the main screen of the app.
 *
 *
 *
 *
 *
 */

@Composable
fun ExerciseScreen(settingsManager: SettingsManager) {
    val settings = settingsManager.loadSettings()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Squeeze ${settings.squeezeSeconds}s  -  Relax ${settings.relaxSeconds}s  -  Times ${settings.repetitions}x",
                    modifier = Modifier.padding(
                        top = 24.dp,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                )
                Text(
                    text = "Total time for set: " + (settings.repetitions * (settings.squeezeSeconds + settings.relaxSeconds)).toString() + "s",
                    modifier = Modifier
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ExerciseProgressIndicator(
                settingsManager,
                settings.squeezeSeconds,
                settings.relaxSeconds,
                settings.repetitions,
                settings.vibrationEnabled
            )
        }
    }
}

@Composable
fun ExerciseProgressIndicator(
    settingsManager: SettingsManager,
    squeezeSeconds: Int,
    relaxSeconds: Int,
    repetitions: Int,
    vibrationEnabled: Boolean
) {
    val settings by settingsManager.settingsFlow.collectAsState()
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(progress, label = "Progress Animation")

    var currentSeconds by remember { mutableIntStateOf(0) }
    var currentPhase by remember { mutableStateOf("Squeeze") }
    var currentRep by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    var exerciseJob by remember { mutableStateOf<Job?>(null) }
    var isRunning by remember { mutableStateOf(false) }

    // Get the Vibrator service
    val context = LocalContext.current
    val vibrator = remember { ContextCompat.getSystemService(context, Vibrator::class.java) }

    // Function to vibrate the phone
    fun vibrate() {
        if (vibrationEnabled) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                if (vibrator != null) {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        }
    }

    LaunchedEffect(isRunning) {
        if (isRunning && exerciseJob == null) {
            exerciseJob = coroutineScope.launch {
                for (rep in 0 until repetitions) {
                    currentRep = rep + 1

                    // Squeeze phase
                    currentPhase = "Squeeze"
                    vibrate()
                    for (i in 0..squeezeSeconds) {
                        progress = i.toFloat() / squeezeSeconds
                        currentSeconds = i
                        delay(1000L)
                    }

                    // Relax phase
                    currentPhase = "Relax"
                    vibrate()
                    for (i in relaxSeconds downTo 0) {
                        progress = i.toFloat() / relaxSeconds
                        currentSeconds = i
                        delay(1000L)
                    }
                }

                val newSettings = settings.copy(
                    completedSets = settings.completedSets + 1,
                    totalTime = settings.totalTime + repetitions * (squeezeSeconds + relaxSeconds)
                )
                settingsManager.saveSettings(newSettings)

                isRunning = false // End of exercise, reset running state
                exerciseJob = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = 1f,
                    strokeWidth = 20.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    modifier = Modifier
                        .width(260.dp)
                        .height(260.dp)
                )
                CircularProgressIndicator(
                    progress = animatedProgress,
                    strokeWidth = 20.dp,
                    color = if (currentPhase == "Squeeze") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .width(260.dp)
                        .height(260.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        text = "$currentSeconds s",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentPhase == "Squeeze") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = currentPhase,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentPhase == "Squeeze") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "($currentRep/$repetitions)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentPhase == "Squeeze") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    // Start the coroutine
                    isRunning = true
                },
                enabled = !isRunning
            ) {
                Text("Start")
            }

            Button(
                onClick = {
                    // Stop the coroutine
                    exerciseJob?.cancel()
                    exerciseJob = null
                    isRunning = false
                    // Reset the state variables
                    progress = 0f
                    currentSeconds = 0
                    currentPhase = "Squeeze"
                    currentRep = 0
                },
                enabled = isRunning
            ) {
                Text("Reset")
            }
        }
    }
}


@Composable
fun StatsScreen(settingsManager: SettingsManager) {
    val settings by settingsManager.settingsFlow.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .width(300.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Completed Sets:", fontSize = 24.sp)
                    Text("${settings.completedSets}", fontSize = 48.sp)
                }
            }
        }

        Card(
            modifier = Modifier
                .padding(16.dp)
                .width(300.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Time:", fontSize = 24.sp)
                val hours = settings.totalTime / 3600
                val minutes = (settings.totalTime % 3600) / 60
                val seconds = settings.totalTime % 60
                Text(
                    String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds),
                    fontSize = 48.sp
                )
            }
        }
    }
}


/**
 * SettingsScreen displays the settings of the app and allows the user to change them.
 *
 *
 *
 *
 *
 */

@Composable
fun SettingsScreen(settingsManager: SettingsManager) {
    val settings by settingsManager.settingsFlow.collectAsState()

    SettingsOptionStepper(
        title = "Squeeze Seconds",
        value = settings.squeezeSeconds,
        onValueChange = { newValue ->
            val newSettings = settings.copy(squeezeSeconds = newValue)
            settingsManager.saveSettings(newSettings)
        }
    )

    SettingsOptionStepper(
        title = "Relax Seconds",
        value = settings.relaxSeconds,
        onValueChange = { newValue ->
            val newSettings = settings.copy(relaxSeconds = newValue)
            settingsManager.saveSettings(newSettings)
        }
    )

    SettingsOptionStepper(
        title = "Repetitions",
        value = settings.repetitions,
        onValueChange = { newValue ->
            val newSettings = settings.copy(repetitions = newValue)
            settingsManager.saveSettings(newSettings)
        }
    )

    SettingsOptionToggle(
        title = "Vibration",
        value = settings.vibrationEnabled,
        onValueChange = { isChecked ->
            val newSettings = settings.copy(vibrationEnabled = isChecked)
            settingsManager.saveSettings(newSettings)
        }
    )

    SettingsOptionToggle(
        title = "Sound",
        value = settings.soundEnabled,
        onValueChange = { isChecked ->
            val newSettings = settings.copy(soundEnabled = isChecked)
            settingsManager.saveSettings(newSettings)
        }
    )

    SettingsOptionToggle(
        title = "Dark Mode",
        value = settings.darkMode,
        onValueChange = { isChecked ->
            val newSettings = settings.copy(darkMode = isChecked)
            settingsManager.saveSettings(newSettings)
        }
    )
}


@Composable
fun SettingsOptionStepper(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                if (value > 1) {
                    onValueChange(value - 1)
                }
            }) {
                Text("<")
            }
            Box(modifier = Modifier.width(42.dp), contentAlignment = Alignment.Center) {
                Text(text = value.toString())
            }
            Button(onClick = { onValueChange(value + 1) }) {
                Text(">")
            }
        }
    }
}


@Composable
fun SettingsOptionToggle(
    title: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title)
        Switch(
            checked = value,
            onCheckedChange = onValueChange
        )
    }
}
