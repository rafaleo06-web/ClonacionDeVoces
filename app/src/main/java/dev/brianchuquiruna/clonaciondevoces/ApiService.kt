package dev.brianchuquiruna.clonaciondevoces

import dev.brianchuquiruna.clonaciondevoces.parents.loadarchives.VoiceIdDeleteResponse
import dev.brianchuquiruna.clonaciondevoces.parents.loadarchives.VoiceIdResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {
    @GET("/v1/voices")
    suspend fun getAllVoices(@Header(AUTHORIZATION) token: String): Response<AllVoicesDataResponse>

    @Streaming
    @POST("/v1/text-to-speech/{voice_id}")
    suspend fun textToSpeech(
        @Path("voice_id") idVoice: String,
        @Query("optimize_streaming_latency") optimizeStreamingLatency: Int,
        @Query("output_format") outputFormat: String,
        @Header(AUTHORIZATION) token: String,
        @Body dto: TextToSpechDto
    ): ResponseBody


    @Multipart
    @POST("/v1/voices/add")
    suspend fun addVoice(
        @Header(AUTHORIZATION) token: String,
        @Part("name") name: String,
        @Part files: MultipartBody.Part,
        @Part("description") description: String
    ): Response<VoiceIdResponse>

    @DELETE("/v1/voices/{voice_id}")
    suspend fun deleteVoice(
        @Header(AUTHORIZATION) token: String,
        @Path("voice_id") voiceId: String
    ):Response<VoiceIdDeleteResponse>
}