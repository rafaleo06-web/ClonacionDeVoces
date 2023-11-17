package dev.brianchuquiruna.clonaciondevoces

data class TextToSpechDto(
    val text: String,
    val model_id: String,
    val voice_settings:VoiceSettingsDto
)

data class VoiceSettingsDto(
    val stability: Double,
    val similarity_boost: Double,
    val style: Double,
    val use_speaker_boost: Boolean
)
