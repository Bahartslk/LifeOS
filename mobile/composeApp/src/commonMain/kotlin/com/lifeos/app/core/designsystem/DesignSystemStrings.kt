package com.lifeos.app.core.designsystem

/**
 * LifeOS is a Turkish-language application: every user-facing string must be
 * in Turkish, per CLAUDE.md, while code (identifiers, files, packages)
 * stays in English.
 *
 * Most design-system components take their visible text as parameters —
 * screens (not yet implemented) own that copy. This object exists only for
 * the small number of strings a component needs *internally* as a sane
 * default (an icon's accessibility label, a generic retry button) so that
 * default is never accidentally left in English. Centralizing them here
 * also gives a single, auditable list of "built-in" UI copy.
 *
 * If LifeOS ever needs a second language, this is the seam to replace with
 * Compose Multiplatform's resource system (`Res.string.*`) — not needed for
 * a single-language app today, so it isn't introduced ahead of that need.
 */
internal object DesignSystemStrings {
    const val PASSWORD_SHOW = "Şifreyi göster"
    const val PASSWORD_HIDE = "Şifreyi gizle"

    const val SEARCH_PLACEHOLDER = "Ara"
    const val SEARCH_CLEAR = "Aramayı temizle"

    const val LOADING = "Yükleniyor"
    const val AI_THINKING = "Hazırlanıyor..."

    const val ERROR_TITLE = "Bir şeyler ters gitti"
    const val ERROR_RETRY = "Tekrar Dene"

    const val BOTTOM_SHEET_DISMISS = "Kapatmak için aşağı kaydırın"
    const val DIALOG_CLOSE = "Kapat"
}
