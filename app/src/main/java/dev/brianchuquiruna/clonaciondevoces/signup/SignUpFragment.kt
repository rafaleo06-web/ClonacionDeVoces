package dev.brianchuquiruna.clonaciondevoces.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_PARENTS
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_STUDENTS
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_TEACHERS
import dev.brianchuquiruna.clonaciondevoces.GetFirebase
import dev.brianchuquiruna.clonaciondevoces.GetFirebase.db
import dev.brianchuquiruna.clonaciondevoces.MainActivity
import dev.brianchuquiruna.clonaciondevoces.PARENTS
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.STUDENT
import dev.brianchuquiruna.clonaciondevoces.TEACHER
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentLogInBinding
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentSignUpBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var actividad: MainActivity
    private var isTeacherOrParents: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actividad = activity as MainActivity
        setupSpinner()
        listeners()
    }

    private fun setupSpinner() {
        with(binding) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                listOf(TEACHER, PARENTS, STUDENT)
            )
            spRol.setAdapter(adapter)
            spRol.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                isTeacherOrParents = adapterView.getItemAtPosition(i).toString()
                if (isTeacherOrParents == PARENTS) {
                    binding.tiAsociateTeacher.isGone = false
                } else if (isTeacherOrParents == TEACHER) {
                    binding.tiAsociateTeacher.isGone = true
                }else if (isTeacherOrParents == STUDENT) {
                    binding.tiAsociateTeacher.isGone = false
                }
            }
        }
    }

    private fun listeners() {
        with(binding) {
            btnSignUp.setOnClickListener {
                if (isTeacherOrParents == TEACHER) {
                    GetFirebase.authFire.createUserWithEmailAndPassword(
                        etTextEmailAddress.text.toString(),
                        etTextPassword.text.toString()
                    )
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                db.collection(FIREBASE_TEACHERS).document(etTextEmailAddress.text.toString()).set(
                                    hashMapOf("name" to etUsername.text.toString())
                                )
                                lifecycleScope.launch(Dispatchers.IO) {
                                    actividad.saveTeacherEmail(etTextEmailAddress.text.toString())
                                }
                                Toast.makeText(context, "Se registro exitosamente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "No se pudo registrar", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else if (isTeacherOrParents == PARENTS) {
                    db.collection(FIREBASE_TEACHERS).document(etAsociateTeacher.text.toString())
                        .get().addOnSuccessListener { teacher ->
                        if (teacher.exists()) {
                            GetFirebase.authFire.createUserWithEmailAndPassword(etTextEmailAddress.text.toString(), etTextPassword.text.toString())
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        db.collection(FIREBASE_PARENTS).document(etTextEmailAddress.text.toString()).set(
                                            hashMapOf(
                                                "name" to etUsername.text.toString(),
                                                "associateProfesor" to etAsociateTeacher.text.toString()
                                            )
                                        )
                                        Toast.makeText(context, "Cuenta creada con exito", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, "El correo de profesor no existe", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else if (isTeacherOrParents == STUDENT) {
                    db.collection(FIREBASE_PARENTS).document(etAsociateTeacher.text.toString())
                        .get().addOnSuccessListener { parent ->
                            if (parent.exists()) {
                                GetFirebase.authFire.createUserWithEmailAndPassword(etTextEmailAddress.text.toString(), etTextPassword.text.toString())
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            db.collection(FIREBASE_STUDENTS).document(etTextEmailAddress.text.toString()).set(
                                                hashMapOf(
                                                    "name" to etUsername.text.toString(),
                                                    "associateParent" to etAsociateTeacher.text.toString()
                                                )
                                            )
                                        Toast.makeText(context, "Cuenta creada con exito", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "El correo de padre no existe", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }

            btnSignInLink.setOnClickListener {
                findNavController().navigate(R.id.action_signUpFragment_to_logInFragment)
            }
        }
    }
}