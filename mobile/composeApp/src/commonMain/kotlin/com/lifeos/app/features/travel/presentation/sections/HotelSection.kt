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
import com.lifeos.app.features.travel.domain.model.AccommodationInfo
import com.lifeos.app.features.travel.presentation.TravelStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "Accommodation" (this task's requirement — see this feature's
 * "Deviations from Stitch" note). Reuses [AppCard] + [InfoRow], the same
 * pattern [FlightSection] uses.
 */
@Composable
fun HotelSection(
    accommodation: AccommodationInfo,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(text = TravelStrings.HOTEL_TITLE, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Text(text = accommodation.hotelName, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(LifeOSSpacing.sm))
        InfoRow(label = TravelStrings.HOTEL_ADDRESS_LABEL, value = accommodation.address)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        InfoRow(label = TravelStrings.HOTEL_ROOM_LABEL, value = accommodation.roomType)
        Spacer(modifier = Modifier.height(LifeOSSpacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.lg)) {
            InfoRow(
                label = TravelStrings.HOTEL_CHECK_IN_LABEL,
                value = accommodation.checkInLabel,
                modifier = Modifier.weight(1f),
            )
            InfoRow(
                label = TravelStrings.HOTEL_CHECK_OUT_LABEL,
                value = accommodation.checkOutLabel,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun HotelSectionPreview() {
    LifeOSTheme {
        HotelSection(
            accommodation = AccommodationInfo(
                hotelName = "Museum Hotel",
                address = "Tekelli Mahallesi, Göreme, Nevşehir",
                roomType = "Deluxe Mağara Süiti",
                checkInLabel = "15:00",
                checkOutLabel = "11:00",
            ),
        )
    }
}
