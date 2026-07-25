package com.lifeos.app.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.app.core.designsystem.components.AiBadge
import com.lifeos.app.core.designsystem.components.AiThinkingIndicator
import com.lifeos.app.core.designsystem.components.AppAvatar
import com.lifeos.app.core.designsystem.components.AppBottomNavigation
import com.lifeos.app.core.designsystem.components.AppBottomNavigationItem
import com.lifeos.app.core.designsystem.components.AppCard
import com.lifeos.app.core.designsystem.components.AppFab
import com.lifeos.app.core.designsystem.components.AppGreetingTopBar
import com.lifeos.app.core.designsystem.components.AppNotesField
import com.lifeos.app.core.designsystem.components.AppOutlinedButton
import com.lifeos.app.core.designsystem.components.AppPrimaryButton
import com.lifeos.app.core.designsystem.components.AppSecondaryButton
import com.lifeos.app.core.designsystem.components.AppTextField
import com.lifeos.app.core.designsystem.components.AppTopBar
import com.lifeos.app.core.designsystem.components.Badge
import com.lifeos.app.core.designsystem.components.EmptyState
import com.lifeos.app.core.designsystem.components.ErrorView
import com.lifeos.app.core.designsystem.components.EyebrowChip
import com.lifeos.app.core.designsystem.components.GradientCard
import com.lifeos.app.core.designsystem.components.InfoRow
import com.lifeos.app.core.designsystem.components.LoadingIndicator
import com.lifeos.app.core.designsystem.components.OptionChipRow
import com.lifeos.app.core.designsystem.components.PasswordTextField
import com.lifeos.app.core.designsystem.components.PhotoOverlayCard
import com.lifeos.app.core.designsystem.components.QuickActionTile
import com.lifeos.app.core.designsystem.components.SearchField
import com.lifeos.app.core.designsystem.components.SectionHeader
import com.lifeos.app.core.designsystem.components.SkeletonCard
import com.lifeos.app.core.designsystem.components.StatusChip
import com.lifeos.app.core.designsystem.components.SuggestionChip
import com.lifeos.app.core.designsystem.theme.LifeOSSize
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSStatusHighPriority
import com.lifeos.app.core.designsystem.theme.LifeOSStatusHighPriorityContainer
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A visual gallery of every component in this design system, grouped by
 * category, for quick review against the Stitch designs without having to
 * navigate a real screen.
 *
 * [AppModalBottomSheet], [AppDialog] (and [ConfirmationDialog]), and
 * [AppSnackbar] are intentionally not previewed here: all three render as
 * an overlay/window (`Dialog`, `ModalBottomSheet`, a `Scaffold`'s
 * `SnackbarHost`), which a static `@Preview` cannot meaningfully show — those
 * are best verified by running the app.
 */

@Preview
@Composable
private fun ButtonsPreview() = LifeOSPreview {
    AppPrimaryButton(text = "Giriş Yap", onClick = {}, trailingIcon = Icons.Filled.ArrowForward)
    AppSecondaryButton(text = "Kontrol Listesini Tamamla", onClick = {})
    AppOutlinedButton(text = "Özellikleri Keşfet", onClick = {})
    AppPrimaryButton(text = "Yükleniyor", onClick = {}, loading = true)
    AppPrimaryButton(text = "Devre Dışı", onClick = {}, enabled = false)
}

@Preview
@Composable
private fun TextFieldsPreview() = LifeOSPreview {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("Kapadokya") }
    var notes by remember { mutableStateOf("") }

    AppTextField(
        value = email,
        onValueChange = { email = it },
        label = "E-posta",
        placeholder = "isim@sirket.com",
    )
    PasswordTextField(
        value = password,
        onValueChange = { password = it },
        label = "Şifre",
        forgotPasswordLabel = "Şifremi Unuttum?",
        onForgotPasswordClick = {},
    )
    SearchField(value = query, onValueChange = { query = it })
    AppNotesField(
        notes = notes,
        onNotesChanged = { notes = it },
        label = "Notlar",
        placeholder = "Notlarınızı buraya ekleyin...",
    )
    InfoRow(label = "Son Tarih", value = "Yarın, 14:00")
}

@Preview
@Composable
private fun CardsPreview() = LifeOSPreview {
    AppCard {
        Text("Bugünkü Görevler", style = MaterialTheme.typography.titleMedium)
        Text("3 görev tamamlandı", style = MaterialTheme.typography.bodyMedium)
    }
    GradientCard {
        Text("Günün Özeti", style = MaterialTheme.typography.titleLarge)
        Text("Yapay zeka tarafından hazırlandı", style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
private fun ChipsAndBadgesPreview() = LifeOSPreview {
    SectionHeader(title = "Bugünün Öncelikleri", actionLabel = "Tümünü Yönet", onActionClick = {})
    EyebrowChip(label = "YAPAY ZEKA DESTEKLİ", leadingIcon = Icons.Filled.Home)
    StatusChip(
        label = "YÜKSEK ÖNCELİK",
        containerColor = LifeOSStatusHighPriorityContainer,
        contentColor = LifeOSStatusHighPriority,
    )
    SuggestionChip(label = "İstanbul'da bir durak ekle?", onClick = {})
    SuggestionChip(label = "Macera", onClick = {}, selected = true)
    Badge(label = "TAMAMLANDI")
    AiBadge(label = "AI Optimize Edilmiş Plan", icon = Icons.Filled.FlightTakeoff)
    OptionChipRow(
        options = listOf("Tümü", "İş", "Kişisel"),
        selectedOption = "İş",
        labelFor = { it },
        onOptionSelected = {},
    )
}

@Preview
@Composable
private fun TilesPreview() = LifeOSPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
        QuickActionTile(label = "Yeni Görev", icon = Icons.Filled.AddTask, onClick = {}, modifier = Modifier.weight(1f))
        QuickActionTile(label = "Seyahat Oluştur", icon = Icons.Filled.FlightTakeoff, onClick = {}, modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun StatesPreview() = LifeOSPreview {
    EmptyState(
        title = "Henüz seyahatiniz yok",
        description = "İlk seyahatinizi oluşturun ve planlamaya başlayın.",
        icon = Icons.Filled.FlightTakeoff,
    )
    LoadingIndicator(modifier = Modifier.height(LOADING_INDICATOR_PREVIEW_HEIGHT))
    AiThinkingIndicator()
    SkeletonCard()
    ErrorView(description = "İnternet bağlantınızı kontrol edin.", onRetry = {})
}

@Preview
@Composable
private fun NavigationPreview() = LifeOSPreview {
    AppTopBar(
        title = "Kapadokya",
        navigationIcon = Icons.Filled.Menu,
        navigationContentDescription = "Menü",
        onNavigationClick = {},
    )
    AppGreetingTopBar(
        greeting = "Günaydın, Bahar",
        subtitle = "Yapay zeka tarafından hazırlanan bugünkü özet",
        leadingContent = { AppAvatar(imageUrl = null, contentDescription = null, size = LifeOSSize.avatarSmall) },
        trailingIcon = Icons.Filled.Settings,
        onTrailingClick = {},
    )
    AppBottomNavigation(
        items = listOf(
            AppBottomNavigationItem("Ana Sayfa", Icons.Filled.Home, selected = true, onClick = {}),
            AppBottomNavigationItem("Ayarlar", Icons.Filled.Settings, selected = false, onClick = {}),
        ),
    )
    AppFab(contentDescription = "Yeni ekle", onClick = {})
}

@Preview
@Composable
private fun MediaPreview() = LifeOSPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(LifeOSSpacing.md)) {
        AppAvatar(imageUrl = null, contentDescription = null, initials = "BT")
        AppAvatar(imageUrl = null, contentDescription = null)
    }
    PhotoOverlayCard(
        imageUrl = null,
        contentDescription = null,
        topStartContent = { AiBadge(label = "AI Optimize Edilmiş Plan", icon = Icons.Filled.FlightTakeoff) },
        bottomContent = {
            Text("Kapadokya", style = MaterialTheme.typography.displaySmall)
        },
    )
}

private val LOADING_INDICATOR_PREVIEW_HEIGHT = 120.dp

/** Shared preview scaffold: theme + consistent spacing/padding for every gallery section. */
@Composable
private fun LifeOSPreview(content: @Composable () -> Unit) {
    LifeOSTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LifeOSSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(LifeOSSpacing.md),
        ) {
            content()
        }
    }
}
