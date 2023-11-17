package dev.brianchuquiruna.clonaciondevoces

import com.google.gson.annotations.SerializedName

data class AllVoicesDataResponse(
    @SerializedName("voices") val voices: List<VoiceItemResponse>
)

data class VoiceItemResponse(
    @SerializedName("voice_id") val voiceId: String,
    @SerializedName("name") val name: String,
)