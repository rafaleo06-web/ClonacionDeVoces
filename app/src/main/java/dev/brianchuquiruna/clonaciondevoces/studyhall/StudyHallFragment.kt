package dev.brianchuquiruna.clonaciondevoces.studyhall

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.brianchuquiruna.clonaciondevoces.API_KEY_VOICE_SERVICE
import dev.brianchuquiruna.clonaciondevoces.AllVoicesDataResponse
import dev.brianchuquiruna.clonaciondevoces.ApiService
import dev.brianchuquiruna.clonaciondevoces.CreateRetrofit
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_PARENTS
import dev.brianchuquiruna.clonaciondevoces.GetFirebase.db
import dev.brianchuquiruna.clonaciondevoces.GetRetrofit
import dev.brianchuquiruna.clonaciondevoces.MainActivity
import dev.brianchuquiruna.clonaciondevoces.PARENTS
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.VoiceItemResponse
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentStudyHallBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit


class StudyHallFragment : Fragment() {
    private lateinit var binding:FragmentStudyHallBinding
    private lateinit var actividad:MainActivity
    private lateinit var adapter: StudyHallAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudyHallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        actividad = activity as MainActivity
        getFilterVoices()
    }

    private fun initUI() {
        setStudyHallAdapter()
    }

    private fun setStudyHallAdapter() {
        adapter = StudyHallAdapter { voiceItemResponse ->  navigateToDetail(voiceItemResponse) }
        binding.rvVoice.setHasFixedSize(true)
        binding.rvVoice.layoutManager = LinearLayoutManager(context)
        binding.rvVoice.adapter = adapter
    }

    private fun getAllVoices(createRetrofit: ApiService) {
        binding.progressBar.isGone = false
        lifecycleScope.launch(Dispatchers.IO) {
            val myResponse: Response<AllVoicesDataResponse> = createRetrofit.getAllVoices(API_KEY_VOICE_SERVICE)
            if (myResponse.isSuccessful) {
                Log.i("elevenlabs", "funciona :)")
                val response: AllVoicesDataResponse? = myResponse.body()
                Log.i("elevenlabs", response.toString())
                response?.voices?.let {
                    actividad.runOnUiThread {
                        //Mostar la lista de voces
                        adapter.updateList(response.voices)
                        binding.progressBar.isGone = true
                    }
                }
            } else {
                Log.i("elevenlabs", "No funciona :(")
            }
        }
    }

    private fun getFilterVoices() {

        binding.progressBar.isGone = false
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getTeacherEmail().collect() { email ->
                db.collection(FIREBASE_PARENTS).whereEqualTo("associateProfesor", email).get()
                    .addOnSuccessListener { querySnapshot ->
                        val listVoices= mutableListOf<VoiceItemResponse>()
                        // Itera a través de los documentos que cumplen con el filtro
                        for (document in querySnapshot.documents) {
                            // Accede a los datos de cada documento
//                            val datos = document.data

                            // Haz lo que necesites con los datos
//                            println("ID del documento: ${document.id}")
//                            println("Datos: $datos")
                            document.getString("voiceId")?.let {voiceId ->
                                document.getString("name")?.let { name ->
                                    listVoices.add(VoiceItemResponse(voiceId, name))
                                }
                            }
                        }

                        actividad.runOnUiThread {
                            //Mostar la lista de voces
                            adapter.updateList(listVoices.toList())
                            binding.progressBar.isGone = true
                        }
                    }.addOnFailureListener { exception ->
                        // Maneja errores si la consulta falla
                        println("Error al llamar Firebase: $exception")
                    }
            }

        }
    }

    private fun navigateToDetail(voiceItemResponse: VoiceItemResponse) {
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.saveVoiceId(voiceItemResponse)
            actividad.saveName(voiceItemResponse)
        }
        findNavController().navigate(R.id.action_studyHallFragment_to_chooseOptionFragment)
    }

}