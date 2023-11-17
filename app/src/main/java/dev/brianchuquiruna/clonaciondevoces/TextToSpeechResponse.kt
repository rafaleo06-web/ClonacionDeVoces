package dev.brianchuquiruna.clonaciondevoces

import android.provider.ContactsContract.Data
import com.google.gson.annotations.SerializedName

data class TextToSpeechResponse(
    @SerializedName("voice") val speechVoice:Data
)
