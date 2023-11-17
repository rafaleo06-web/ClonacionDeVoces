package dev.brianchuquiruna.clonaciondevoces.alllearningsesions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.brianchuquiruna.clonaciondevoces.VoiceItemResponse
import dev.brianchuquiruna.clonaciondevoces.databinding.ItemStudyHallBinding
import dev.brianchuquiruna.clonaciondevoces.learning.TopicLearnedResponse

class AllLearningSesionsViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemStudyHallBinding.bind(view)

    fun bind(topicLearnedResponse: TopicLearnedResponse, onItemSelected: (TopicLearnedResponse) -> Unit) {
        binding.tvVoiceName.text = topicLearnedResponse.title
        binding.root.setOnClickListener { onItemSelected(topicLearnedResponse) }
    }
}