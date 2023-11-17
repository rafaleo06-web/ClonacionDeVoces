package dev.brianchuquiruna.clonaciondevoces.learning

import java.util.Date

data class TopicLearnedListResponse(
    val response: MutableList<TopicLearnedResponse>
)

data class TopicLearnedResponse(
    val id:String,
    val date: Date,
    var description: String,
    val title: String,
    var qualification:String
)
