package dev.brianchuquiruna.clonaciondevoces.onlyonesession

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.databinding.ItemLearningBinding
import dev.brianchuquiruna.clonaciondevoces.databinding.ItemOnlyOneSessionVoiceBinding
import dev.brianchuquiruna.clonaciondevoces.learning.TopicLearnedResponse

class OnlyOneSessionViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemOnlyOneSessionVoiceBinding.bind(view)

   fun bind(recordVoiceResponse: RecordVoiceResponse, onItemSelected: (RecordVoiceResponse, ImageView) -> Unit) {
        with(binding){
            tvTitle.text = recordVoiceResponse.title
            root.setOnClickListener {
                onItemSelected(recordVoiceResponse,ivPlay)
            }
        }
    }
}