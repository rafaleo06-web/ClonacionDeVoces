package dev.brianchuquiruna.clonaciondevoces

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dev.brianchuquiruna.clonaciondevoces.GetFirebase.authFire
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentLogInBinding
import dev.brianchuquiruna.clonaciondevoces.util.AlertDialog
import dev.brianchuquiruna.clonaciondevoces.util.PerfilActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogInFragment : Fragment() {

    private lateinit var binding: FragmentLogInBinding
    private lateinit var actividad:MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actividad = activity as MainActivity
        listeners()
    }

//    private fun listeners() {
//        with(binding) {
//            btnSignIn.setOnClickListener {
//                findNavController().navigate(R.id.action_logInFragment_to_studyHallFragment)
//                lifecycleScope.launch(Dispatchers.IO) {
//                    actividad.saveCurrentEmail(etTextEmailAddress.text.toString())
//                }
//            }
//        }
//    }

    private fun listeners() {
        with(binding){
            btnSignIn.setOnClickListener {
                var isAccountInvalid = 0
                if (etTextEmailAddress.text.toString().isNotEmpty() and etTextPassword.text.toString().isNotEmpty()){
                    authFire.signInWithEmailAndPassword(etTextEmailAddress.text.toString(), etTextPassword.text.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful){
                                GetFirebase.db.collection(FIREBASE_TEACHERS).document(etTextEmailAddress.text.toString())
                                    .get().addOnSuccessListener { teacher ->
                                        if (teacher.exists()) {
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                actividad.saveTeacherEmail(etTextEmailAddress.text.toString())
                                                actividad.savePerfilActive(PerfilActive.TEACHER)
                                                actividad.runOnUiThread {
                                                    findNavController().navigate(R.id.action_logInFragment_to_studyHallFragment)
                                                }
                                            }
                                        }else{
                                            isAccountInvalid++

                                        }
                                    }

                                GetFirebase.db.collection(FIREBASE_PARENTS).document(etTextEmailAddress.text.toString())
                                    .get().addOnSuccessListener { parents ->
                                        if (parents.exists()) {
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                actividad.saveParentEmail(etTextEmailAddress.text.toString())
                                                parents.getString("associateProfesor")?.let { teacher -> actividad.saveTeacherEmail(teacher) }
                                                actividad.savePerfilActive(PerfilActive.PARENTS)
                                                actividad.runOnUiThread {
                                                    findNavController().navigate(R.id.action_logInFragment_to_principalParentsFragment)
                                                }
                                            }
                                        }else{
                                            isAccountInvalid++
                                        }
                                    }

                                GetFirebase.db.collection(FIREBASE_STUDENTS).document(etTextEmailAddress.text.toString())
                                    .get().addOnSuccessListener { student ->
                                        if (student.exists()) {
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                actividad.saveStudentEmail(etTextEmailAddress.text.toString())
                                                student.getString("associateParent")?.let { parent -> actividad.saveParentEmail(parent) }
                                                actividad.savePerfilActive(PerfilActive.STUDENT)
                                                actividad.runOnUiThread {
                                                    findNavController().navigate(R.id.action_logInFragment_to_allLearningSesionsFragment)
                                                }
                                            }
                                        }
//                                        else{
//                                            isAccountInvalid++
//                                            if(isAccountInvalid==3){
//                                                AlertDialog(
//                                                    message = "El padre asociado a esta cuenta borro su cuenta, por lo tanto esta cuenta tambien sera borrada",
//                                                    onSubmitClickListener = {authFire.currentUser?.delete()},
//                                                    onCancelClickListener = {authFire.currentUser?.delete()}
//                                                ).show(parentFragmentManager, "myAlertDialog")
//                                            }
//                                        }
                                    }
                            }else{
                                Toast.makeText(context, "Credenciales incorrectos y/o no hay internet", Toast.LENGTH_SHORT).show()
                            }

                        }
                }else{
                    Toast.makeText(context, "Algun campo de texto esta vacio", Toast.LENGTH_SHORT).show()
                }
            }

            btnSignUpLink.setOnClickListener {
                findNavController().navigate(R.id.action_logInFragment_to_signUpFragment)
            }
        }
    }

}