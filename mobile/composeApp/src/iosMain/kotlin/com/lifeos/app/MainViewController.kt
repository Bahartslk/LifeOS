package com.lifeos.app

import androidx.compose.ui.window.ComposeUIViewController
import com.lifeos.app.core.di.initKoin
import platform.UIKit.UIViewController

private var koinInitialized = false

fun MainViewController(): UIViewController {
    if (!koinInitialized) {
        initKoin()
        koinInitialized = true
    }
    return ComposeUIViewController { App() }
}
