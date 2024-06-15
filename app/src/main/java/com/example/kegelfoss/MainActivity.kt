package com.example.kegelfoss

import android.content.Context
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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.kegelfoss.ui.theme.KegelFOSSTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale


/**
 * MainActivity is the entry point of the app. It sets up the UI and the settings manager.
 */

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(this)
        settingsViewModel = SettingsViewModel(settingsRepository)
        enableEdgeToEdge()
        setContent {
            KegelFOSSThemeWithDarkMode(settingsViewModel) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TabLayout(settingsViewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun KegelFOSSThemeWithDarkMode(
    settingsViewModel: SettingsViewModel,
    content: @Composable () -> Unit
) {
    val darkMode by settingsViewModel.darkModeFlow.collectAsState()
    KegelFOSSTheme(darkTheme = darkMode, content = content)
}


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferenceKeys {
        val squeezeSeconds = intPreferencesKey("squeezeSecondsKey")
        val relaxSeconds = intPreferencesKey("relaxSecondsKey")
        val repetitions = intPreferencesKey("repetitionsKey")
        val vibrationEnabled = booleanPreferencesKey("vibrationEnabledKey")
        val soundEnabled = booleanPreferencesKey("soundEnabledKey")
        val darkMode = booleanPreferencesKey("darkModeKey")
        val totalTime = intPreferencesKey("totalTimeKey")
        val completedSets: Preferences.Key<Int> = intPreferencesKey("completedSetsKey")
    }

    val settingsFlow: Flow<Settings> = context.dataStore.data
        .map { preferences ->
            Settings(
                squeezeSeconds = preferences[PreferenceKeys.squeezeSeconds] ?: 3,
                relaxSeconds = preferences[PreferenceKeys.relaxSeconds] ?: 3,
                repetitions = preferences[PreferenceKeys.repetitions] ?: 10,
                vibrationEnabled = preferences[PreferenceKeys.vibrationEnabled] ?: true,
                soundEnabled = preferences[PreferenceKeys.soundEnabled] ?: false,
                darkMode = preferences[PreferenceKeys.darkMode] ?: false,
                totalTime = preferences[PreferenceKeys.totalTime] ?: 0,
                completedSets = preferences[PreferenceKeys.completedSets] ?: 0
            )
        }

    suspend fun updateSettings(settings: Settings) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.squeezeSeconds] = settings.squeezeSeconds
            preferences[PreferenceKeys.relaxSeconds] = settings.relaxSeconds
            preferences[PreferenceKeys.repetitions] = settings.repetitions
            preferences[PreferenceKeys.vibrationEnabled] = settings.vibrationEnabled
            preferences[PreferenceKeys.soundEnabled] = settings.soundEnabled
            preferences[PreferenceKeys.darkMode] = settings.darkMode
            preferences[PreferenceKeys.totalTime] = settings.totalTime
            preferences[PreferenceKeys.completedSets] = settings.completedSets
        }
    }
}

data class Settings(
    val squeezeSeconds: Int,
    val relaxSeconds: Int,
    val repetitions: Int,
    val vibrationEnabled: Boolean,
    val soundEnabled: Boolean,
    val darkMode: Boolean,
    val totalTime: Int,
    val completedSets: Int
)

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _settingsFlow: StateFlow<Settings> = settingsRepository.settingsFlow
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            Settings(3, 3, 10,
                vibrationEnabled = true,
                soundEnabled = false,
                darkMode = false,
                totalTime = 0,
                completedSets = 0
            )
        )

    val settingsFlow: StateFlow<Settings> = _settingsFlow

    val squeezeSecondsFlow: StateFlow<Int> = settingsFlow.map { it.squeezeSeconds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 3)

    val relaxSecondsFlow: StateFlow<Int> = settingsFlow.map { it.relaxSeconds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 3)

    val repetitionsFlow: StateFlow<Int> = settingsFlow.map { it.repetitions }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 10)

    val vibrationEnabledFlow: StateFlow<Boolean> = settingsFlow.map { it.vibrationEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    val soundEnabledFlow: StateFlow<Boolean> = settingsFlow.map { it.soundEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val darkModeFlow: StateFlow<Boolean> = settingsFlow.map { it.darkMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val totalTimeFlow: StateFlow<Int> = settingsFlow.map { it.totalTime }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val completedSetsFlow: StateFlow<Int> = settingsFlow.map { it.completedSets }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    fun updateSettings(settings: Settings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings)
        }
    }
}


/**
 * TabLayout displays three tabs: Exercise, Stats, and Settings and the content of the selected tab.
 */

data class TabItem(val title: String, val icon: Painter)

@Composable
fun TabLayout(settingsViewModel: SettingsViewModel, modifier: Modifier = Modifier) {
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
            0 -> ExerciseScreen(settingsViewModel)
            1 -> StatsScreen(settingsViewModel)
            2 -> SettingsScreen(settingsViewModel)
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
fun ExerciseScreen(settingsViewModel: SettingsViewModel) {
    val squeezeSeconds by settingsViewModel.squeezeSecondsFlow.collectAsState()
    val relaxSeconds by settingsViewModel.relaxSecondsFlow.collectAsState()
    val repetitions by settingsViewModel.repetitionsFlow.collectAsState()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Squeeze ${squeezeSeconds}s  -  Relax ${relaxSeconds}s  -  Times ${repetitions}x",
                    modifier = Modifier.padding(
                        top = 24.dp,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                )
                Text(
                    text = "Total time for set: " + (repetitions * (squeezeSeconds + relaxSeconds)).toString() + "s",
                    modifier = Modifier
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ExerciseProgressIndicator(settingsViewModel)
        }
    }
}


@Composable
fun ExerciseProgressIndicator(settingsViewModel: SettingsViewModel) {
    val squeezeSeconds by settingsViewModel.squeezeSecondsFlow.collectAsState()
    val relaxSeconds by settingsViewModel.relaxSecondsFlow.collectAsState()
    val repetitions by settingsViewModel.repetitionsFlow.collectAsState()
    val vibrationEnabled by settingsViewModel.vibrationEnabledFlow.collectAsState()

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
                    for (i in 0 .. squeezeSeconds) {
                        progress = i.toFloat() / squeezeSeconds
                        currentSeconds = squeezeSeconds - i
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

                // Update the total time and completed sets
                settingsViewModel.updateSettings(
                    settingsViewModel.settingsFlow.value.copy(
                        totalTime = settingsViewModel.settingsFlow.value.totalTime + repetitions * (squeezeSeconds + relaxSeconds),
                        completedSets = settingsViewModel.settingsFlow.value.completedSets + 1
                    )
                )

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
fun StatsScreen(settingsViewModel: SettingsViewModel) {
    val completedSets by settingsViewModel.completedSetsFlow.collectAsState()
    val totalTime by settingsViewModel.totalTimeFlow.collectAsState()

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
                    Text("$completedSets", fontSize = 48.sp)
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
                val hours = totalTime / 3600
                val minutes = (totalTime % 3600) / 60
                val seconds = totalTime % 60
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
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val squeezeSeconds by settingsViewModel.squeezeSecondsFlow.collectAsState()
    val relaxSeconds by settingsViewModel.relaxSecondsFlow.collectAsState()
    val repetitions by settingsViewModel.repetitionsFlow.collectAsState()
    val vibrationEnabled by settingsViewModel.vibrationEnabledFlow.collectAsState()
    val soundEnabled by settingsViewModel.soundEnabledFlow.collectAsState()
    val darkMode by settingsViewModel.darkModeFlow.collectAsState()

    SettingsOptionStepper(
        title = "Squeeze Seconds",
        value = squeezeSeconds,
        onValueChange = { newValue ->
            coroutineScope.launch {
                settingsViewModel.updateSettings(
                    settingsViewModel.settingsFlow.value.copy(squeezeSeconds = newValue)
                )
            }
        }
    )

    SettingsOptionStepper(
        title = "Relax Seconds",
        value = relaxSeconds,
        onValueChange = { newValue ->
            coroutineScope.launch {
                settingsViewModel.updateSettings(
                    settingsViewModel.settingsFlow.value.copy(relaxSeconds = newValue)
                )
            }
        }
    )

    SettingsOptionStepper(
        title = "Repetitions",
        value = repetitions,
        onValueChange = { newValue ->
            coroutineScope.launch {
                settingsViewModel.updateSettings(
                    settingsViewModel.settingsFlow.value.copy(repetitions = newValue)
                )
            }
        }
    )

    SettingsOptionToggle(
        title = "Vibration",
        value = vibrationEnabled,
        onValueChange = { newValue ->
            coroutineScope.launch {
                settingsViewModel.updateSettings(
                    settingsViewModel.settingsFlow.value.copy(vibrationEnabled = newValue)
                )
            }
        }
    )

    SettingsOptionToggle(
        title = "Sound",
        value = soundEnabled,
        onValueChange = { newValue ->
            coroutineScope.launch {
                settingsViewModel.updateSettings(
                    settingsViewModel.settingsFlow.value.copy(soundEnabled = newValue)
                )
            }
        }
    )

    SettingsOptionToggle(
        title = "Dark Mode",
        value = darkMode,
        onValueChange = { newValue ->
            coroutineScope.launch {
                settingsViewModel.updateSettings(
                    settingsViewModel.settingsFlow.value.copy(darkMode = newValue)
                )
            }
        }
    )

    Button(
        onClick = {
            coroutineScope.launch {
                settingsViewModel.updateSettings(
                    settingsViewModel.settingsFlow.value.copy(totalTime = 0, completedSets = 0)
                )
            }
        },
        modifier = Modifier.padding(24.dp)
    ) {
        Text("Reset Stats")
    }
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