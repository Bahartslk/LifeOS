package com.lifeos.app.features.profile.presentation

/**
 * All Turkish UI copy for the Profile feature, centralized per the pattern
 * established by `HomeStrings`/`TravelStrings`/`PlannerStrings`/
 * `AiAssistantStrings`.
 */
internal object ProfileStrings {

    // Header — profile.png "Bahar Toslak" + avatar
    const val AVATAR_CONTENT_DESCRIPTION = "Profil fotoğrafı"
    const val EDIT_PROFILE_ACTION = "Profili Düzenle"
    const val FALLBACK_NAME = "Kullanıcı"
    const val FALLBACK_EMAIL = "E-posta bulunamadı"

    // Account
    const val ACCOUNT_TITLE = "Hesap"
    const val SECURITY_ACTION = "Güvenlik"
    const val SECURITY_COMING_SOON = "Güvenlik ayarları yakında kullanıma sunulacak."
    const val CHANGE_PASSWORD_ACTION = "Şifre Değiştir"

    // Preferences
    const val PREFERENCES_TITLE = "Tercihler"
    const val APPEARANCE_LABEL = "Görünüm"
    const val APPEARANCE_SYSTEM = "Sistem"
    const val APPEARANCE_LIGHT = "Açık"
    const val APPEARANCE_DARK = "Koyu"
    const val LANGUAGE_ACTION = "Dil"
    const val LANGUAGE_VALUE = "Türkçe"
    const val LANGUAGE_COMING_SOON = "Dil seçenekleri yakında kullanıma sunulacak."
    const val AI_PREFERENCES_ACTION = "AI Tercihleri"
    const val AI_PREFERENCES_COMING_SOON = "AI tercihleri yakında kullanıma sunulacak."
    const val TRAVEL_PREFERENCES_ACTION = "Seyahat Tercihleri"
    const val TRAVEL_PREFERENCES_COMING_SOON = "Seyahat tercihleri yakında kullanıma sunulacak."

    // Notifications
    const val NOTIFICATIONS_TITLE = "Bildirimler"
    const val NOTIFICATION_SETTINGS_ACTION = "Bildirim Ayarları"
    const val NOTIFICATION_SETTINGS_COMING_SOON = "Bildirim ayarları yakında kullanıma sunulacak."
    const val REMINDER_SETTINGS_ACTION = "Hatırlatıcı Ayarları"
    const val REMINDER_SETTINGS_COMING_SOON = "Hatırlatıcı ayarları yakında kullanıma sunulacak."

    // About
    const val ABOUT_TITLE = "Hakkında"
    const val PRIVACY_POLICY_ACTION = "Gizlilik Politikası"
    const val PRIVACY_POLICY_COMING_SOON = "Gizlilik politikası yakında kullanıma sunulacak."
    const val TERMS_OF_SERVICE_ACTION = "Kullanım Şartları"
    const val TERMS_OF_SERVICE_COMING_SOON = "Kullanım şartları yakında kullanıma sunulacak."
    const val APP_VERSION_LABEL = "Uygulama Sürümü"
    const val APP_VERSION_VALUE = "1.0.0"

    // Session
    const val SESSION_TITLE = "Oturum"
    const val LOGOUT_ACTION = "Çıkış Yap"
    const val LOGOUT_CONFIRMATION_TITLE = "Çıkış Yap"
    const val LOGOUT_CONFIRMATION_MESSAGE = "Hesabınızdan çıkış yapmak istediğinizden emin misiniz?"
    const val LOGOUT_CONFIRMATION_CONFIRM = "Çıkış Yap"
    const val LOGOUT_CONFIRMATION_DISMISS = "Vazgeç"
    const val LOGOUT_IN_PROGRESS = "Çıkış yapılıyor..."
    const val LOGOUT_ERROR_MESSAGE = "Çıkış yapılamadı. Lütfen tekrar deneyin."

    // Edit name dialog
    const val EDIT_NAME_TITLE = "Adını düzenle"
    const val EDIT_NAME_LABEL = "Ad Soyad"
    const val EDIT_NAME_SAVE = "Kaydet"
    const val EDIT_NAME_SAVING = "Kaydediliyor..."
    const val EDIT_NAME_DISMISS = "Vazgeç"
    const val EDIT_NAME_EMPTY_ERROR = "Ad soyad gerekli."
    const val EDIT_NAME_TOO_SHORT_ERROR = "Ad soyad en az 2 karakter olmalı."
    const val EDIT_NAME_TOO_LONG_ERROR = "Ad soyad en fazla 100 karakter olabilir."
    const val EDIT_NAME_ERROR = "Ad güncellenemedi. Bağlantını kontrol edip tekrar dene."
    const val EDIT_NAME_SUCCESS = "Adın güncellendi."

    // Change password screen
    const val CHANGE_PASSWORD_TITLE = "Şifre Değiştir"
    const val CHANGE_PASSWORD_BACK = "Geri"
    const val CHANGE_PASSWORD_INFO =
        "Şifren değişince diğer cihazlardaki oturumların kapatılır; bu cihazda oturumun açık kalır."
    const val CURRENT_PASSWORD_LABEL = "Mevcut şifre"
    const val NEW_PASSWORD_LABEL = "Yeni şifre"
    const val CONFIRM_NEW_PASSWORD_LABEL = "Yeni şifre tekrar"
    const val CHANGE_PASSWORD_SUBMIT = "Şifreyi Değiştir"
    const val CHANGE_PASSWORD_IN_PROGRESS = "Değiştiriliyor..."
    const val CURRENT_PASSWORD_REQUIRED = "Mevcut şifre gerekli."
    const val CURRENT_PASSWORD_WRONG = "Mevcut şifre yanlış."
    const val NEW_PASSWORD_REQUIRED = "Yeni şifre gerekli."
    const val NEW_PASSWORD_TOO_SHORT = "Şifre en az 8 karakter olmalı."
    const val NEW_PASSWORD_TOO_LONG = "Şifre en fazla 72 karakter olabilir."
    const val NEW_PASSWORD_SAME_AS_CURRENT = "Yeni şifre mevcut şifreden farklı olmalı."
    const val CONFIRM_PASSWORD_REQUIRED = "Şifre tekrarı gerekli."
    const val PASSWORDS_DO_NOT_MATCH = "Şifreler eşleşmiyor."
    const val CHANGE_PASSWORD_TOO_MANY_ATTEMPTS = "Çok fazla deneme, biraz sonra tekrar dene."
    const val CHANGE_PASSWORD_ERROR = "Şifre değiştirilemedi. Bağlantını kontrol edip tekrar dene."
    const val CHANGE_PASSWORD_SUCCESS = "Şifren değiştirildi. Diğer cihazlardaki oturumların kapatıldı."

    // Delete account
    const val DELETE_ACCOUNT_ACTION = "Hesabı Sil"
    const val DELETE_ACCOUNT_TITLE = "Hesabı sil"
    const val DELETE_ACCOUNT_WARNING = "Bu işlem geri alınamaz, tüm görevlerin ve seyahatlerin silinir."
    const val DELETE_ACCOUNT_PASSWORD_PROMPT = "Devam etmek için şifreni gir."
    const val DELETE_ACCOUNT_PASSWORD_LABEL = "Şifre"
    const val DELETE_ACCOUNT_CONFIRM = "Hesabı kalıcı olarak sil"
    const val DELETE_ACCOUNT_IN_PROGRESS = "Siliniyor..."
    const val DELETE_ACCOUNT_DISMISS = "Vazgeç"
    const val DELETE_ACCOUNT_PASSWORD_REQUIRED = "Şifre gerekli."
    const val DELETE_ACCOUNT_WRONG_PASSWORD = "Şifre yanlış. Hesabın silinmedi."
    const val DELETE_ACCOUNT_TOO_MANY_ATTEMPTS = "Çok fazla deneme, biraz sonra tekrar dene."
    const val DELETE_ACCOUNT_ERROR = "Hesap silinemedi. Bağlantını kontrol edip tekrar dene."

    // Screen states
    const val LOAD_ERROR_MESSAGE = "Profil yüklenemedi. Lütfen tekrar deneyin."
}
