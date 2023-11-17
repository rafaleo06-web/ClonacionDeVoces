package dev.brianchuquiruna.clonaciondevoces.learning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.studyhall.StudyHallViewHolder

class LearningAdapter(
    var topicLearnedList: List<TopicLearnedResponse> = emptyList(),
    private val teacherOrParents:Boolean = true,
    private val onItemSelected: (TopicLearnedResponse) -> Unit
) :
    RecyclerView.Adapter<LearningViewHolder>() {

    fun updateList(list: List<TopicLearnedResponse>) {
        topicLearnedList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearningViewHolder {
        return LearningViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_learning, parent, false)
        )
    }

    override fun onBindViewHolder(viewholder: LearningViewHolder, position: Int) {
        viewholder.bind(topicLearnedList[position],onItemSelected,teacherOrParents)
    }

    override fun getItemCount() = topicLearnedList.size

}