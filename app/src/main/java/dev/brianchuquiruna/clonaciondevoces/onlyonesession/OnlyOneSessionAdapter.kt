package dev.brianchuquiruna.clonaciondevoces.onlyonesession

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.learning.LearningViewHolder
import dev.brianchuquiruna.clonaciondevoces.learning.TopicLearnedResponse

class OnlyOneSessionAdapter(
    var recordVoiceResponse: List<RecordVoiceResponse> = emptyList(),
    private val onItemSelected: (RecordVoiceResponse, ImageView) -> Unit
) :
    RecyclerView.Adapter<OnlyOneSessionViewHolder>() {

    fun updateList(list: List<RecordVoiceResponse>) {
        recordVoiceResponse = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlyOneSessionViewHolder {
        return OnlyOneSessionViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_only_one_session_voice, parent, false)
        )
    }

    override fun onBindViewHolder(viewholder: OnlyOneSessionViewHolder, position: Int) {
        viewholder.bind(recordVoiceResponse[position],onItemSelected)
    }

    override fun getItemCount() = recordVoiceResponse.size

}