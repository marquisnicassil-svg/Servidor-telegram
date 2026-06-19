package com.example.data.api

import com.example.data.model.TelegramGetMeResponse
import com.example.data.model.TelegramSendMessageRequest
import com.example.data.model.TelegramSendMessageResponse
import com.example.data.model.TelegramUpdatesResponse
import com.example.data.model.TelegramWebhookResponse
import com.example.data.model.TelegramSimpleResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TelegramApiService {
    @GET("bot{token}/getMe")
    suspend fun getMe(
        @Path("token") token: String
    ): TelegramGetMeResponse

    @GET("bot{token}/getUpdates")
    suspend fun getUpdates(
        @Path("token") token: String,
        @Query("offset") offset: Long?,
        @Query("limit") limit: Int?,
        @Query("timeout") timeout: Int?
    ): TelegramUpdatesResponse

    @POST("bot{token}/sendMessage")
    suspend fun sendMessage(
        @Path("token") token: String,
        @Body request: TelegramSendMessageRequest
    ): TelegramSendMessageResponse

    @GET("bot{token}/getWebhookInfo")
    suspend fun getWebhookInfo(
        @Path("token") token: String
    ): TelegramWebhookResponse

    @GET("bot{token}/deleteWebhook")
    suspend fun deleteWebhook(
        @Path("token") token: String
    ): TelegramSimpleResponse
}
