package dev.brianchuquiruna.clonaciondevoces.alllearningsesions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.learning.TopicLearnedResponse

class AllLearningSesionsAdapter (
    var topicLearnedResponseList: List<TopicLearnedResponse> = emptyList(),
    private val onItemSelected: (TopicLearnedResponse) -> Unit
) :
    RecyclerView.Adapter<AllLearningSesionsViewHolder>() {

    fun updateList(list: List<TopicLearnedResponse>) {
        topicLearnedResponseList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllLearningSesionsViewHolder {
        return AllLearningSesionsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_study_hall, parent, false)
        )
    }

    override fun onBindViewHolder(viewholder: AllLearningSesionsViewHolder, position: Int) {
        viewholder.bind(topicLearnedResponseList[position],onItemSelected)
    }

    override fun getItemCount() = topicLearnedResponseList.size

}