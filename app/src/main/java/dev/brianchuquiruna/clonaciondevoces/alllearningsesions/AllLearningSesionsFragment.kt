package dev.brianchuquiruna.clonaciondevoces.alllearningsesions

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_PARENTS
import dev.brianchuquiruna.clonaciondevoces.GetFirebase
import dev.brianchuquiruna.clonaciondevoces.MainActivity
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.VoiceItemResponse
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentAllLearningSesionsBinding
import dev.brianchuquiruna.clonaciondevoces.learning.LearningAdapter
import dev.brianchuquiruna.clonaciondevoces.learning.TopicLearnedListResponse
import dev.brianchuquiruna.clonaciondevoces.learning.TopicLearnedResponse
import dev.brianchuquiruna.clonaciondevoces.studyhall.StudyHallAdapter
import dev.brianchuquiruna.clonaciondevoces.util.PerfilActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class AllLearningSesionsFragment : Fragment() {
    private lateinit var binding: FragmentAllLearningSesionsBinding
    private lateinit var actividad: MainActivity
    private lateinit var adapter: AllLearningSesionsAdapter
    private lateinit var topicLearnedListResponse: TopicLearnedListResponse
    private var email:String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllLearningSesionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        actividad = activity as MainActivity
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getPerfilActive().collect(){
                if(it == PerfilActive.TEACHER){
                    getAllSesions()
                }else if(it == PerfilActive.STUDENT){
                    actividad.runOnUiThread{
                        binding.linearLayout2.isGone = false
                    }
                    getAllSesionsFromParentEmail()
                }
            }
        }
    }

    private fun initUI() {
        setLearningAdapter()
    }

    private fun setLearningAdapter() {
        adapter = AllLearningSesionsAdapter { topicLearnedResponse ->  navigateToDetail(topicLearnedResponse) }
        binding.rvAllSesions.setHasFixedSize(true)
        binding.rvAllSesions.layoutManager = LinearLayoutManager(context)
        binding.rvAllSesions.adapter = adapter
    }

    private fun navigateToDetail(topicLearnedResponse: TopicLearnedResponse) {
        findNavController().navigate(AllLearningSesionsFragmentDirections.actionAllLearningSesionsFragmentToOnlyOneSessionFragment(email = this.email, titleSesion = topicLearnedResponse.title, idSession = topicLearnedResponse.id))
    }

    private fun getAllSesionsFromParentEmail(){
        binding.progressBar.isGone = false
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getParentEmail().collect() { parentEmail ->
                email = parentEmail
                GetFirebase.db.collection(FIREBASE_PARENTS).document(parentEmail).collection("topicsLearned").get()
                    .addOnSuccessListener {
                        topicLearnedListResponse = TopicLearnedListResponse(mutableListOf<TopicLearnedResponse>())
                        for (iterator in it.documents) {
                            iterator?.getString("description")?.let { description->
                                Log.i("TopicsLearned", description)
                                iterator?.getString("title")?.let { title->
                                    Log.i("TopicsLearned", title)
                                    iterator?.getDate("date")?.let { date->
                                        Log.i("TopicsLearned", date.toString())
                                        iterator?.getString("qualification")?.let { qualification ->
                                            Log.i("TopicsLearned", qualification)
                                            topicLearnedListResponse.response.add(TopicLearnedResponse(iterator.id,date, description,title,qualification))
                                        }
                                    }
                                }
                            }
                        }
                        actividad.runOnUiThread {
                            adapter.updateList(topicLearnedListResponse.response.toList())
                            binding.progressBar.isGone = true
                        }
                    }.addOnFailureListener { exception ->
                        println("Error al recuperar los topicos aprendidos de Firebase: $exception")
                    }
            }
        }
    }

    private fun getAllSesions(){
        binding.progressBar.isGone = false
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getVoiceId().collect() { voiceId ->
                GetFirebase.db.collection(FIREBASE_PARENTS).whereEqualTo("voiceId", voiceId).get()
                    .addOnSuccessListener { querySnapshot ->
                        val document = querySnapshot.documents[0]
                        email = document.id
                        Log.i("email", email)
                        GetFirebase.db.collection(FIREBASE_PARENTS).document(document.id).collection("topicsLearned").get()
                            .addOnSuccessListener {
                                topicLearnedListResponse = TopicLearnedListResponse(mutableListOf<TopicLearnedResponse>())
                                for (iterator in it.documents) {
                                    iterator?.getString("description")?.let { description->
                                        Log.i("TopicsLearned", description)
                                        iterator.getString("title")?.let { title->
                                            Log.i("TopicsLearned", title)
                                            iterator.getDate("date")?.let { date->
                                                Log.i("TopicsLearned", date.toString())
                                                iterator.getString("qualification")?.let { qualification ->
                                                    Log.i("TopicsLearned", qualification)
                                                    topicLearnedListResponse.response.add(TopicLearnedResponse(iterator.id,date, description,title,qualification))
                                                }
                                            }
                                        }
                                    }
                                }
                                actividad.runOnUiThread {
                                    adapter.updateList(topicLearnedListResponse.response.toList())
                                    binding.progressBar.isGone = true
                                }
                            }.addOnFailureListener { exception ->
                                println("Error al recuperar los topicos aprendidos de Firebase: $exception")
                            }
                    }.addOnFailureListener { exception ->
                        println("Error al recuperar el email de Firebase: $exception")
                    }
            }
        }
    }

}