package dev.brianchuquiruna.clonaciondevoces.learning

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_PARENTS
import dev.brianchuquiruna.clonaciondevoces.GetFirebase.db
import dev.brianchuquiruna.clonaciondevoces.MainActivity
import dev.brianchuquiruna.clonaciondevoces.PERFIL_ACTIVE
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentLearningBinding
import dev.brianchuquiruna.clonaciondevoces.util.OnlyReadDialog
import dev.brianchuquiruna.clonaciondevoces.util.PerfilActive
import dev.brianchuquiruna.clonaciondevoces.util.ReadDialog
import dev.brianchuquiruna.clonaciondevoces.util.WriteDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class LearningFragment : Fragment() {
    private lateinit var binding: FragmentLearningBinding
    private lateinit var adapter: LearningAdapter
    private lateinit var actividad:MainActivity
    private lateinit var topicLearnedListResponse: TopicLearnedListResponse
    private var email:String = ""
    private lateinit var perfilActive:PerfilActive

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLearningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }


    private fun initUI() {

        actividad = activity as MainActivity
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getPerfilActive().collect(){
                actividad.runOnUiThread {
                    setLearningAdapter(it)
                }
                if (PerfilActive.TEACHER == it) {
                    perfilActive=PerfilActive.TEACHER
                    getTopicsLearned()
                } else if (PerfilActive.PARENTS == it){
                    perfilActive=PerfilActive.PARENTS
                    binding.tvTitle.text = "Progreso educativo"
                    binding.btnAddActivity.isGone = true
                    getTopicsLearnedFromParentSesion()
                }
            }
        }
        initListener()
    }

    private fun initListener() {
        binding.btnAddActivity.setOnClickListener {
            showWriteDialog()
        }
    }

    private fun showWriteDialog() {
        WriteDialog(
            onCancelClickListener = {},
            onSubmitClickListener = { title:String, description:String, date:Date, qualification:String ->
                binding.progressBar.isGone = false
                db.collection(FIREBASE_PARENTS).document(email).collection("topicsLearned").document().set(
                    hashMapOf(
                        "date" to date,
                        "title" to title,
                        "qualification" to qualification.ifEmpty { "0" },
                        "description" to description
                    )
                )
                binding.progressBar.isGone = true
            }
        ).show(parentFragmentManager, "writeDialog")
    }

    private fun showReadDialog(id:String,title:String,description:String,date: Date, qualification:Int) {
        if(perfilActive==PerfilActive.TEACHER){
            ReadDialog(
                title = title,
                description=description,
                date = date,
                qualification = qualification,
                onCancelClickListener = {},
                onSubmitClickListener = {descriptionDialog:String,qualificationDialog:String ->
                    binding.progressBar.isGone = false
                    db.collection(FIREBASE_PARENTS).document(email).collection("topicsLearned").document(id).update("description",descriptionDialog)
                    db.collection(FIREBASE_PARENTS).document(email).collection("topicsLearned").document(id).update("qualification",qualificationDialog)
                    topicLearnedListResponse.response.find { response -> id == response.id }.apply {
                        this?.description = descriptionDialog
                        this?.qualification = qualificationDialog
                    }

                    binding.progressBar.isGone = true
                }
            ).show(parentFragmentManager, "readDialog")
        }else if (perfilActive==PerfilActive.PARENTS){
            OnlyReadDialog(
                title = title,
                description=description,
                date = date,
                qualification = qualification,
                onCancelClickListener = {},
                onSubmitClickListener = {}
            ).show(parentFragmentManager, "onlyReadDialog")
        }

    }

    private fun setLearningAdapter(perfilActive: PerfilActive) {
        if(perfilActive == PerfilActive.TEACHER){
            adapter = LearningAdapter { topicLearnedResponse ->  navigateToDetail(topicLearnedResponse) }
        } else if(perfilActive == PerfilActive.PARENTS){
            adapter = LearningAdapter ( teacherOrParents =false, onItemSelected = { topicLearnedResponse -> navigateToDetail(topicLearnedResponse) } )
        }

        binding.rvLearning.setHasFixedSize(true)
        binding.rvLearning.layoutManager = LinearLayoutManager(context)
        binding.rvLearning.adapter = adapter

        if(perfilActive == PerfilActive.TEACHER){
            val callback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // Esto se ejecuta cuando el elemento se arrastra hacia la izquierda o hacia la derecha
                    // Puedes realizar una acción aquí
                    Toast.makeText(context, "Se elimino ${topicLearnedListResponse.response[viewHolder.adapterPosition].title}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.isGone = false
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.collection(FIREBASE_PARENTS).document(email).collection("topicsLearned").document(
                            topicLearnedListResponse.response[viewHolder.adapterPosition].id
                        ).delete()

                        binding.progressBar.isGone = true
                    }
                }
            }
            val itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper.attachToRecyclerView(binding.rvLearning)
        }
    }

    private fun navigateToDetail(topicLearnedResponse: TopicLearnedResponse) {
        showReadDialog(topicLearnedResponse.id,topicLearnedResponse.title,topicLearnedResponse.description,topicLearnedResponse.date,topicLearnedResponse.qualification.toInt())
    }

    private fun getTopicsLearned(){
        binding.progressBar.isGone = false
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getVoiceId().collect() { voiceId ->
                db.collection(FIREBASE_PARENTS).whereEqualTo("voiceId", voiceId).get()
                    .addOnSuccessListener { querySnapshot ->
                        val document = querySnapshot.documents[0]
                        email = document.id
                        Log.i("email", email)
                        db.collection(FIREBASE_PARENTS).document(document.id).collection("topicsLearned").get()
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
                                    radar()
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

    private fun radar(){
        var entries:MutableList<RadarEntry> = mutableListOf()
        var sum = 0f
        val labels= mutableListOf<String>()
        topicLearnedListResponse.response.map { topicLearned ->
            entries.add(RadarEntry(33.3f* topicLearned.qualification.toFloat()))
            labels.add(if (topicLearned.title.length >= 10) topicLearned.title.substring(0,10)+"..." else topicLearned.title)
            sum += topicLearned.qualification.toFloat()
        }
        var dataset = RadarDataSet(entries, "Nota promedio : ${sum/topicLearnedListResponse.response.size}")
        dataset.setColor(Color.BLUE)
        var listDataset: MutableList<IRadarDataSet> = mutableListOf()
        listDataset.add(dataset)
        val data = RadarData(listDataset)
        data.setDrawValues(false)
        binding.chartRadar.data = data
        binding.chartRadar.description.isEnabled = false
        binding.chartRadar.xAxis.valueFormatter= IndexAxisValueFormatter(labels)
        binding.chartRadar.animateXY(1000,1000)
        binding.chartRadar.invalidate()
    }

    private fun getTopicsLearnedFromParentSesion(){
        binding.progressBar.isGone = false
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getParentEmail().collect(){parentEmail ->
                db.collection(FIREBASE_PARENTS).document(parentEmail).collection("topicsLearned").get()
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
                            radar()
                        }
                    }.addOnFailureListener { exception ->
                        println("Error al recuperar los topicos aprendidos de Firebase: $exception")
                    }
            }

        }
    }
}