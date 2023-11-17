package dev.brianchuquiruna.clonaciondevoces.studyhall

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.brianchuquiruna.clonaciondevoces.VoiceItemResponse
import dev.brianchuquiruna.clonaciondevoces.databinding.ItemStudyHallBinding

class StudyHallViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemStudyHallBinding.bind(view)

    fun bind(voiceItemResponse: VoiceItemResponse, onItemSelected: (VoiceItemResponse) -> Unit) {
        binding.tvVoiceName.text = voiceItemResponse.name
        binding.root.setOnClickListener { onItemSelected(voiceItemResponse) }
    }
}