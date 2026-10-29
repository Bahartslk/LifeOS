package com.lifeos.app.features.auth.presentation

import com.lifeos.app.features.auth.domain.validation.ValidationError

/**
 * All Turkish UI copy for the Authentication feature, centralized so it can
 * be audited/updated in one place — mirrors the pattern established by
 * `core/designsystem/DesignSystemStrings.kt`, scoped to this feature instead
 * of being cross-feature generic.
 *
 * Code identifiers (this object's name, its properties) stay in English;
 * only the string *values* are Turkish, per this task's language rule.
 */
internal object AuthStrings {

    // Splash — splash.png
    const val SPLASH_TAGLINE = "AKILLI SAKİNLİK"
    const val SPLASH_SYNCING = "Dünyanız senkronize ediliyor…"
    const val SPLASH_SUBTITLE = "Zihinsel netlik sizi bekliyor"

    // Onboarding — onboarding-1/2/3.png
    const val ONBOARDING_SKIP = "Geç"
    const val ONBOARDING_NEXT_STEP = "Sonraki Adım"
    const val ONBOARDING_NEXT = "İleri"
    const val ONBOARDING_GET_STARTED = "Başlayın"
    const val ONBOARDING_LEGAL_FOOTER = "Devam ederek Hizmet Şartları'nı ve Gizlilik Politikası'nı kabul etmiş olursunuz."

    const val ONBOARDING_PAGE1_EYEBROW = "YÖNETİCİ İŞLEV MERKEZİ"
    const val ONBOARDING_PAGE1_TITLE = "Günlük hayatınızı tek bir yerden yönetin."
    const val ONBOARDING_PAGE1_DESCRIPTION =
        "Görevleri, seyahatleri ve daha fazlasını yapay zeka ile düzenleyin. " +
            "Rutininize zeka ve huzur katmak için tasarlanmış sofistike araç."

    const val ONBOARDING_PAGE2_TITLE = "Yapay zeka asistanınız görevleri ve seyahatleri düzenlemenize yardımcı olur."
    const val ONBOARDING_PAGE2_DESCRIPTION = "Parmaklarınızın ucunda kişiselleştirilmiş planlama."

    const val ONBOARDING_PAGE3_EYEBROW = "BAŞLAMAYA HAZIR"
    const val ONBOARDING_PAGE3_TITLE = "Daha akıllı planlayın, düzenli kalın ve daha fazlasını başarın."
    const val ONBOARDING_PAGE3_DESCRIPTION =
        "LifeOS ile yolculuğunuza bugün başlayın. Yapay zeka zekası ile huzurlu " +
            "tasarımın mükemmel uyumunu deneyimleyin."

    // Shared auth header — login.png
    const val APP_NAME = "LifeOS"

    // Login — login.png
    const val LOGIN_TITLE = "Tekrar hoş geldiniz"
    const val LOGIN_SUBTITLE = "Akıllı çalışma alanınıza adım atın."
    const val LOGIN_EMAIL_LABEL = "E-posta"
    const val LOGIN_EMAIL_PLACEHOLDER = "isim@sirket.com"
    const val LOGIN_PASSWORD_LABEL = "Şifre"
    const val LOGIN_FORGOT_PASSWORD = "Şifremi Unuttum?"
    const val LOGIN_SUBMIT = "Giriş Yap"
    const val LOGIN_OR_DIVIDER = "VEYA"
    const val LOGIN_CONTINUE_WITH_GOOGLE = "Google ile Devam Et"
    const val LOGIN_NO_ACCOUNT = "Hesabınız yok mu?"
    const val LOGIN_CREATE_ACCOUNT = "Hesap Oluştur"
    const val LOGIN_COMING_SOON = "Bu özellik yakında kullanıma sunulacak."
    const val LOGIN_INVALID_CREDENTIALS = "E-posta veya şifre hatalı."
    const val LOGIN_CONNECTION_ERROR = "Giriş yapılamadı. Bağlantınızı kontrol edip tekrar deneyin."
    const val SESSION_EXPIRED_MESSAGE = "Oturumun sona erdi, lütfen tekrar giriş yap."
    const val ACCOUNT_DELETED_MESSAGE = "Hesabın ve tüm verilerin silindi."

    // Register
    const val REGISTER_TITLE = "Hesap Oluştur"
    const val REGISTER_SUBTITLE = "LifeOS ile hayatınızı düzenlemeye bugün başlayın."
    const val REGISTER_FULL_NAME_LABEL = "Ad Soyad"
    const val REGISTER_FULL_NAME_PLACEHOLDER = "Ayşe Yılmaz"
    const val REGISTER_CONFIRM_PASSWORD_LABEL = "Şifre Tekrar"
    const val REGISTER_TERMS_TEXT = "Kullanım Şartları'nı ve Gizlilik Politikası'nı kabul ediyorum"
    const val REGISTER_SUBMIT = "Hesap Oluştur"
    const val REGISTER_HAVE_ACCOUNT = "Zaten hesabınız var mı?"
    const val REGISTER_LOGIN = "Giriş Yap"
    const val REGISTER_EMAIL_ALREADY_EXISTS = "Bu e-posta adresi zaten kayıtlı."
    const val REGISTER_CONNECTION_ERROR = "Hesap oluşturulamadı. Bağlantınızı kontrol edip tekrar deneyin."

    // Forgot Password
    const val FORGOT_PASSWORD_TITLE = "Şifrenizi mi unuttunuz?"
    const val FORGOT_PASSWORD_SUBTITLE = "E-posta adresinizi girin, size 6 haneli bir sıfırlama kodu gönderelim."
    const val FORGOT_PASSWORD_SUBMIT = "Kod Gönder"
    const val FORGOT_PASSWORD_BACK_TO_LOGIN = "Girişe Dön"
    const val PASSWORD_RESET_TOO_MANY_ATTEMPTS = "Çok fazla deneme yapıldı. Lütfen biraz sonra tekrar deneyin."
    const val FORGOT_PASSWORD_ERROR = "Kod gönderilemedi. Bağlantınızı kontrol edip tekrar deneyin."

    // Reset Password
    const val RESET_PASSWORD_TITLE = "Yeni şifre belirleyin"
    fun resetPasswordSubtitle(email: String) =
        "$email adresine bir kayıt varsa 6 haneli bir kod gönderdik. Kodu ve yeni şifrenizi girin."
    const val RESET_PASSWORD_CODE_LABEL = "Doğrulama kodu"
    const val RESET_PASSWORD_CODE_PLACEHOLDER = "6 haneli kod"
    const val RESET_PASSWORD_NEW_PASSWORD_LABEL = "Yeni şifre"
    const val RESET_PASSWORD_CONFIRM_LABEL = "Yeni şifre tekrar"
    const val RESET_PASSWORD_SUBMIT = "Şifreyi Yenile"
    const val RESET_PASSWORD_RESEND = "Kodu tekrar gönder"
    const val RESET_PASSWORD_RESENT = "Yeni bir kod gönderildi. E-postanızı kontrol edin."
    const val RESET_PASSWORD_CODE_INVALID_FORMAT = "6 haneli kodu girin."
    const val RESET_PASSWORD_CODE_WRONG = "Kod hatalı veya süresi dolmuş. Yeni bir kod isteyebilirsiniz."
    const val RESET_PASSWORD_ERROR = "Şifre yenilenemedi. Bağlantınızı kontrol edip tekrar deneyin."
    const val PASSWORD_RESET_MESSAGE = "Şifreniz yenilendi. Yeni şifrenizle giriş yapın."

    // Field validation errors, mapped from the domain's ValidationError
    fun emailError(error: ValidationError): String = when (error) {
        ValidationError.EMPTY -> "E-posta adresi gerekli."
        ValidationError.INVALID_EMAIL_FORMAT -> "Geçerli bir e-posta adresi girin."
        else -> GENERIC_FIELD_ERROR
    }

    fun passwordError(error: ValidationError): String = when (error) {
        ValidationError.EMPTY -> "Şifre gerekli."
        ValidationError.PASSWORD_TOO_SHORT -> "Şifre en az 8 karakter olmalı."
        ValidationError.PASSWORD_TOO_LONG -> "Şifre en fazla 72 karakter olabilir."
        else -> GENERIC_FIELD_ERROR
    }

    fun confirmPasswordError(error: ValidationError): String = when (error) {
        ValidationError.EMPTY -> "Şifre tekrarı gerekli."
        ValidationError.PASSWORDS_DO_NOT_MATCH -> "Şifreler eşleşmiyor."
        else -> GENERIC_FIELD_ERROR
    }

    fun nameError(error: ValidationError): String = when (error) {
        ValidationError.EMPTY -> "Ad soyad gerekli."
        ValidationError.NAME_TOO_SHORT -> "Ad soyad en az 2 karakter olmalı."
        else -> GENERIC_FIELD_ERROR
    }

    const val TERMS_NOT_ACCEPTED_ERROR = "Devam etmek için şartları kabul etmelisiniz."
    private const val GENERIC_FIELD_ERROR = "Bu alanı kontrol edin."
}
