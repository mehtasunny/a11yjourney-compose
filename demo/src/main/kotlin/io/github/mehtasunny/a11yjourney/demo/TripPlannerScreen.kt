package io.github.mehtasunny.a11yjourney.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import io.github.mehtasunny.a11yjourney.compose.ActionButton
import io.github.mehtasunny.a11yjourney.compose.Arrival
import io.github.mehtasunny.a11yjourney.compose.ArrivalRow
import io.github.mehtasunny.a11yjourney.compose.ArrivalStatus
import io.github.mehtasunny.a11yjourney.compose.IconAction
import io.github.mehtasunny.a11yjourney.compose.LabeledTextField
import io.github.mehtasunny.a11yjourney.compose.LiveArrivalSummary
import io.github.mehtasunny.a11yjourney.compose.ScreenHeading
import io.github.mehtasunny.a11yjourney.compose.StatusBanner
import io.github.mehtasunny.a11yjourney.compose.StatusKind

private val ARRIVALS = listOf(
    Arrival("7", "Downtown", 4),
    Arrival("44", "University District", 9, ArrivalStatus.Delayed),
    Arrival("12", "Harbor Station", 15),
    Arrival("3", "Uptown", null, scheduledTime = "6:15 PM"),
)

@Composable
fun TripPlannerScreen() {
    var from by rememberSaveable { mutableStateOf("Main St and 5th Ave") }
    var to by rememberSaveable { mutableStateOf("") }

    ScreenHeading("Trip planner")
    StatusBanner(
        "Route 7 is detoured around 3rd Avenue until 8 PM. Stops on 3rd are closed.",
        StatusKind.Warning,
    )
    LabeledTextField(value = from, onValueChange = { from = it }, label = "From")
    LabeledTextField(
        value = to,
        onValueChange = { to = it },
        label = "To",
        imeAction = ImeAction.Search,
    )
    ActionButton("Plan trip", onClick = {}, modifier = Modifier.fillMaxWidth())
    Row(verticalAlignment = Alignment.CenterVertically) {
        ScreenHeading("Next departures from Main St and 5th Ave", modifier = Modifier.weight(1f))
        IconAction(icon = Icons.Filled.Refresh, label = "Refresh departures", onClick = {})
    }
    LiveArrivalSummary(ARRIVALS)
    Column {
        ARRIVALS.forEach { arrival ->
            ArrivalRow(arrival, onClick = {})
            HorizontalDivider()
        }
    }
}
