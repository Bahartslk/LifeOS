package com.lifeos.app.features.auth.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifeos.app.core.designsystem.theme.LifeOSSpacing
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.designsystem.theme.LifeOSVioletBase
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The centered "prompt + tappable action" row repeated at the bottom of
 * Login ("Hesabınız yok mu? Hesap Oluştur") and Register ("Zaten hesabınız
 * var mı? Giriş Yap") — identical structure, different copy and target
 * screen, extracted once both screens had copy-pasted it.
 */
@Composable
fun AuthFooterPrompt(
    promptText: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = promptText, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(LifeOSSpacing.xs))
        Text(
            text = actionText,
            style = MaterialTheme.typography.labelLarge,
            color = LifeOSVioletBase,
            modifier = Modifier.clickable(onClick = onActionClick),
        )
    }
}

@Preview
@Composable
private fun AuthFooterPromptPreview() {
    LifeOSTheme {
        AuthFooterPrompt(
            promptText = "Hesabınız yok mu?",
            actionText = "Hesap Oluştur",
            onActionClick = {},
        )
    }
}
