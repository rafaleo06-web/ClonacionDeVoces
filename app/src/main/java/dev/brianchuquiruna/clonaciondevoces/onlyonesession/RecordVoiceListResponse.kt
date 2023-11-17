package dev.brianchuquiruna.clonaciondevoces.onlyonesession

import java.util.Date

data class RecordVoiceListResponse(
    val response: MutableList<RecordVoiceResponse>
)

data class RecordVoiceResponse(
    val id: String,
    val title:String,
    val urlRecord:String,
    val date: Date
)
