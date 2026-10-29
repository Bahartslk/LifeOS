package com.lifeos.app.features.ai.presentation.chat

import com.lifeos.app.features.ai.domain.usecase.SendChatMessageUseCase

object AiChatStrings {
    const val TITLE = "Sohbet"
    const val BACK_DESCRIPTION = "Geri dön"
    const val INPUT_LABEL = "Mesajın"
    const val INPUT_PLACEHOLDER = "Asistana bir şey sor…"
    const val SEND_DESCRIPTION = "Mesajı gönder"
    const val TYPING = "Asistan yazıyor…"
    const val FAILED_LABEL = "Gönderilemedi"
    const val RETRY_ACTION = "Tekrar dene"
    const val SEND_ERROR_MESSAGE = "Mesaj gönderilemedi. Bağlantını kontrol edip tekrar dene."
    const val LIMIT_REACHED_MESSAGE = "Bugünlük mesaj hakkın doldu, yarın görüşürüz."
    const val EMPTY_TITLE = "Asistana sor"
    const val EMPTY_DESCRIPTION = "Görevlerin ve seyahatlerin hakkında kısa sorular sorabilirsin. Asistan yalnızca öneri yapar."
    const val SUGGESTIONS_TITLE = "Örnek sorular"
    const val ENTRY_ACTION = "Asistanla sohbet et"

    val SUGGESTIONS = listOf(
        "Bugün neye odaklanmalıyım?",
        "Yarına nasıl hazırlanayım?",
        "Bu hafta neleri yetiştirmeliyim?",
    )

    fun remainingMessages(count: Int) = "Bugün $count mesaj hakkın kaldı"

    fun characterCount(length: Int) = "$length/${SendChatMessageUseCase.MAX_MESSAGE_LENGTH}"

    fun characterCountDescription(length: Int) =
        "${SendChatMessageUseCase.MAX_MESSAGE_LENGTH} karakterden $length karakter kullanıldı"

    fun userMessageDescription(text: String) = "Sen: $text"

    fun assistantMessageDescription(text: String) = "Asistan: $text"

    fun suggestionDescription(text: String) = "Örnek soruyu gönder: $text"
}
