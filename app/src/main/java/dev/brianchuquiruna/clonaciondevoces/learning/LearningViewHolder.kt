package dev.brianchuquiruna.clonaciondevoces.learning

import android.view.View
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import dev.brianchuquiruna.clonaciondevoces.VoiceItemResponse
import dev.brianchuquiruna.clonaciondevoces.databinding.ItemLearningBinding
import dev.brianchuquiruna.clonaciondevoces.databinding.ItemStudyHallBinding

class LearningViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemLearningBinding.bind(view)

    fun bind(topicLearnedResponse: TopicLearnedResponse, onItemSelected: (TopicLearnedResponse) -> Unit, teacherOrParents:Boolean) {
        if (!teacherOrParents) binding.etEdit.isGone = true
        binding.tvActivityLearned.text = topicLearnedResponse.title
        binding.root.setOnClickListener { onItemSelected(topicLearnedResponse) }
    }
}