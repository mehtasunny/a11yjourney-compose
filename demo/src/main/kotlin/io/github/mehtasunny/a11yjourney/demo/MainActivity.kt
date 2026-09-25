package io.github.mehtasunny.a11yjourney.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mehtasunny.a11yjourney.compose.A11yTheme
import io.github.mehtasunny.a11yjourney.compose.ActionButton
import io.github.mehtasunny.a11yjourney.compose.ScreenHeading

/** Screens the demo can open. `adb shell am start ... --es screen trip` opens one directly. */
enum class Screen { Home, Clinic, Trip, Benefits }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val start = when (intent?.getStringExtra("screen")?.lowercase()) {
            "clinic" -> Screen.Clinic
            "trip" -> Screen.Trip
            "benefits" -> Screen.Benefits
            else -> Screen.Home
        }
        setContent {
            A11yTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    DemoApp(start)
                }
            }
        }
    }
}

@Composable
fun DemoApp(start: Screen) {
    var screen by rememberSaveable { mutableStateOf(start) }
    BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (screen) {
            Screen.Home -> HomeScreen(onOpen = { screen = it })
            Screen.Clinic -> ClinicCheckInScreen()
            Screen.Trip -> TripPlannerScreen()
            Screen.Benefits -> BenefitsFormScreen()
        }
    }
}

@Composable
private fun HomeScreen(onOpen: (Screen) -> Unit) {
    ScreenHeading("A11yJourney Compose demo")
    Text(
        "Three example screens built only from accessible components. Every screen works " +
            "with TalkBack, at the largest font size, and with switch access.",
        style = MaterialTheme.typography.bodyLarge,
    )
    ActionButton("Clinic check-in", onClick = { onOpen(Screen.Clinic) }, modifier = Modifier.fillMaxWidth())
    ActionButton("Trip planner", onClick = { onOpen(Screen.Trip) }, modifier = Modifier.fillMaxWidth())
    ActionButton("Benefits application", onClick = { onOpen(Screen.Benefits) }, modifier = Modifier.fillMaxWidth())
}
