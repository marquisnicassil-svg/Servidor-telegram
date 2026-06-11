package com.example.data.api

import retrofit2.http.*

interface GmailApiService {
    @GET("gmail/v1/users/{userId}/profile")
    suspend fun getProfile(
        @Header("Authorization") authHeader: String,
        @Path("userId") userId: String = "me"
    ): GmailProfileResponse

    @GET("gmail/v1/users/{userId}/messages")
    suspend fun listMessages(
        @Header("Authorization") authHeader: String,
        @Path("userId") userId: String = "me",
        @Query("maxResults") maxResults: Int = 10,
        @Query("q") query: String? = null
    ): GmailListMessagesResponse

    @GET("gmail/v1/users/{userId}/messages/{id}")
    suspend fun getMessageDetails(
        @Header("Authorization") authHeader: String,
        @Path("userId") userId: String = "me",
        @Path("id") messageId: String
    ): GmailMessageDetailsResponse

    @POST("gmail/v1/users/{userId}/messages/send")
    suspend fun sendMessage(
        @Header("Authorization") authHeader: String,
        @Path("userId") userId: String = "me",
        @Body request: GmailSendRequest
    ): GmailSendResponse
}

data class GmailProfileResponse(
    val emailAddress: String,
    val messagesTotal: Int? = null,
    val threadsTotal: Int? = null
)

data class GmailListMessagesResponse(
    val messages: List<GmailMessageSummary>? = null,
    val nextPageToken: String? = null,
    val resultSizeEstimate: Int? = null
)

data class GmailMessageSummary(
    val id: String,
    val threadId: String
)

data class GmailMessageDetailsResponse(
    val id: String,
    val threadId: String,
    val labelIds: List<String>? = null,
    val snippet: String? = null,
    val internalDate: String? = null,
    val payload: GmailPayload? = null
)

data class GmailPayload(
    val partId: String? = null,
    val mimeType: String? = null,
    val filename: String? = null,
    val headers: List<GmailHeader>? = null,
    val body: GmailBody? = null,
    val parts: List<GmailPayload>? = null
)

data class GmailHeader(
    val name: String,
    val value: String
)

data class GmailBody(
    val size: Int? = null,
    val data: String? = null
)

data class GmailSendRequest(
    val raw: String
)

data class GmailSendResponse(
    val id: String,
    val threadId: String,
    val labelIds: List<String>? = null
)
