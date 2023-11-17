package dev.brianchuquiruna.clonaciondevoces.studyhall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.VoiceItemResponse

class StudyHallAdapter (
    var voiceList: List<VoiceItemResponse> = emptyList(),
    private val onItemSelected: (VoiceItemResponse) -> Unit
) :
    RecyclerView.Adapter<StudyHallViewHolder>() {

    fun updateList(list: List<VoiceItemResponse>) {
        voiceList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyHallViewHolder {
        return StudyHallViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_study_hall, parent, false)
        )
    }

    override fun onBindViewHolder(viewholder: StudyHallViewHolder, position: Int) {
        viewholder.bind(voiceList[position],onItemSelected)
    }

    override fun getItemCount() = voiceList.size

}