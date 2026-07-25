package com.lifeos.app.features.travel.presentation.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.InfoRow
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.features.travel.domain.model.FlightInfo
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Flight Information" (this task's requirement — see this feature's
 * "Deviations from Stitch" note). Reuses [AppCard] + [InfoRow].
 */
@Composable
fun FlightSection(
    flight: FlightInfo,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.FLIGHT_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg)) {
            InfoRow(
                label = TravelStrings.FLIGHT_AIRLINE_LABEL,
                value = flight.airline,
                modifier = Modifier.weight(1f),
            )
            InfoRow(
                label = TravelStrings.FLIGHT_NUMBER_LABEL,
                value = flight.flightNumber,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg)) {
            InfoRow(
                label = TravelStrings.FLIGHT_DEPARTURE_LABEL,
                value = "${flight.departureAirport} · ${flight.departureTimeLabel}",
                modifier = Modifier.weight(1f),
            )
            InfoRow(
                label = TravelStrings.FLIGHT_ARRIVAL_LABEL,
                value = "${flight.arrivalAirport} · ${flight.arrivalTimeLabel}",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg)) {
            InfoRow(
                label = TravelStrings.FLIGHT_TERMINAL_LABEL,
                value = flight.terminal,
                modifier = Modifier.weight(1f),
            )
            InfoRow(
                label = TravelStrings.FLIGHT_GATE_LABEL,
                value = flight.gate ?: TravelStrings.FLIGHT_GATE_TBD,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun FlightSectionPreview() {
    LifeOSTheme {
        FlightSection(
            flight = FlightInfo(
                airline = "Turkish Airlines",
                flightNumber = "TK2014",
                departureAirport = "İstanbul (IST)",
                departureTimeLabel = "10:45",
                arrivalAirport = "Nevşehir (NAV)",
                arrivalTimeLabel = "12:15",
                terminal = "Dış Hatlar Terminali",
                gate = null,
            ),
        )
    }
}
