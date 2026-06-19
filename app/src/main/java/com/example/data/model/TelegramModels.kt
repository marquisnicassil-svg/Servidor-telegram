package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TelegramUser(
    val id: Long,
    @Json(name = "is_bot") val isBot: Boolean,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String? = null,
    val username: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramChat(
    val id: Long,
    @Json(name = "first_name") val firstName: String? = null,
    @Json(name = "last_name") val lastName: String? = null,
    val username: String? = null,
    val type: String
)

@JsonClass(generateAdapter = true)
data class TelegramMessage(
    @Json(name = "message_id") val messageId: Long,
    val from: TelegramUser? = null,
    val chat: TelegramChat,
    val date: Long,
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramUpdate(
    @Json(name = "update_id") val updateId: Long,
    val message: TelegramMessage? = null
)

@JsonClass(generateAdapter = true)
data class TelegramUpdatesResponse(
    val ok: Boolean,
    val result: List<TelegramUpdate> = emptyList(),
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramGetMeResponse(
    val ok: Boolean,
    val result: TelegramUser? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramSendMessageRequest(
    @Json(name = "chat_id") val chatId: Long,
    val text: String,
    @Json(name = "parse_mode") val parseMode: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramSendMessageResponse(
    val ok: Boolean,
    val result: TelegramMessage? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramWebhookInfo(
    val url: String = "",
    @Json(name = "has_custom_certificate") val hasCustomCertificate: Boolean = false,
    @Json(name = "pending_update_count") val pendingUpdateCount: Int = 0,
    @Json(name = "last_error_date") val lastErrorDate: Long? = null,
    @Json(name = "last_error_message") val lastErrorMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramWebhookResponse(
    val ok: Boolean,
    val result: TelegramWebhookInfo? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramSimpleResponse(
    val ok: Boolean,
    val description: String? = null
)

