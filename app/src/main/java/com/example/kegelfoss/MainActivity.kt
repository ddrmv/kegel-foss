package com.example.kegelfoss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.kegelfoss.ui.theme.KegelFOSSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KegelFOSSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TabLayout(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TabLayout(modifier: Modifier = Modifier) {
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
            0 -> CurrentExerciseScreen()
            1 -> UsageStatisticsScreen()
            2 -> SettingsScreen()
        }
    }
}

@Composable
fun CurrentExerciseScreen() {
    // Your code here
}

@Composable
fun UsageStatisticsScreen() {
    // Your code here
}

@Composable
fun SettingsScreen() {
    // Your code here
}

data class TabItem(val title: String, val icon: Painter)

@Preview(showBackground = true)
@Composable
fun TabLayoutPreview() {
    KegelFOSSTheme {
        TabLayout()
    }
}
