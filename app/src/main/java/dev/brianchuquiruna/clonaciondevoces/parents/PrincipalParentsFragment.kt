package dev.brianchuquiruna.clonaciondevoces.parents

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dev.brianchuquiruna.clonaciondevoces.API_KEY_VOICE_SERVICE
import dev.brianchuquiruna.clonaciondevoces.CreateRetrofit
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_PARENTS
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_RECORD_VOICE
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_STUDENTS
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_TOPICS
import dev.brianchuquiruna.clonaciondevoces.GetFirebase.authFire
import dev.brianchuquiruna.clonaciondevoces.GetFirebase.db
import dev.brianchuquiruna.clonaciondevoces.MainActivity
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentPrincipalParentsBinding
import dev.brianchuquiruna.clonaciondevoces.util.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PrincipalParentsFragment : Fragment() {

    private lateinit var binding: FragmentPrincipalParentsBinding
    private lateinit var actividad: MainActivity
    private val createRetrofit = CreateRetrofit()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPrincipalParentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actividad = activity as MainActivity
        initListeners()
    }

    private fun initListeners() {
        with(binding){
            btnLoadArchive.setOnClickListener {
                findNavController().navigate(R.id.action_principalParentsFragment_to_loadArchivesFragment)
            }
            btnWatchLearningProcess.setOnClickListener {
                findNavController().navigate(R.id.action_principalParentsFragment_to_learningFragment)
            }
            btnDeleteAccount.setOnClickListener {
                AlertDialog(
                    message = "¿Usted esta seguro de eliminar esta cuenta?",
                    onSubmitClickListener = {
//                        progressBar.isGone = false
                        lifecycleScope.launch(Dispatchers.IO) {
                            actividad.getParentEmail().collect(){ parentEmail->
                                deleteParentAccount(parentEmail)
                            }
                        }
                        findNavController().navigate(R.id.action_principalParentsFragment_to_logInFragment)
                    },
                    onCancelClickListener ={}
                ).show(parentFragmentManager, "myAlertDialog")
            }
        }
    }

    private fun deleteVoice(voiceId:String){
        lifecycleScope.launch(Dispatchers.IO) {
            val response = createRetrofit.deleteVoice(API_KEY_VOICE_SERVICE, voiceId)
            Log.i("deleteVoice","Borrar voz status: ${response.body()?.status.orEmpty()}")
        }
    }

    private fun deleteParentAccount(parentEmail: String) {
        db.collection(FIREBASE_PARENTS).document(parentEmail).get().addOnSuccessListener { parent ->
            if (parent.exists()) {
                parent.getString("voiceId")?.let { voiceId -> deleteVoice(voiceId) }
                ////////Collection topcisLearned/////////
                db.collection(FIREBASE_PARENTS).document(parentEmail).collection(FIREBASE_TOPICS).get()
                    .addOnSuccessListener { querySnapshot ->
                        for (documenttopcisLearned in querySnapshot) {
                            // cada documento en topicsLearned
                            db.collection(FIREBASE_PARENTS).document(parentEmail).collection(FIREBASE_TOPICS).document(documenttopcisLearned.id).collection(FIREBASE_RECORD_VOICE).get()
                                .addOnSuccessListener {querySnapshotRecordVoiceByEmail ->
                                    ////////Collection recordVoiceByEmail/////////
                                    for (documentRecordVoiceByEmail in querySnapshotRecordVoiceByEmail){
                                        // cada documento en recordVoiceByEmail
                                        documentRecordVoiceByEmail.reference.delete()
                                        //------------------------------------
                                    }
                                    ////////////////////////////////////////////////
                                }
                            //---------------------------------
                            documenttopcisLearned.reference.delete()
                        }
                    }
                ////////////////
                parent.reference.delete()
            }
        }

        authFire.currentUser?.delete()?.addOnSuccessListener { _ ->
            db.collection(FIREBASE_STUDENTS).whereEqualTo("associateParent",parentEmail).get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        document.reference.delete()
                            .addOnSuccessListener {
                                Log.i("deleteAccount", "exito al borrar documento de students")
                            }
                            .addOnFailureListener { exception ->
                                Log.i("deleteAccount", "error al borrar documento de students")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.i("deleteAccount", "error al obtener los estudiantes asociados al padre")
                }
        }

    }

}