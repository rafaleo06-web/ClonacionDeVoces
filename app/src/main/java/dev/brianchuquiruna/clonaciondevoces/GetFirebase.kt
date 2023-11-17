package dev.brianchuquiruna.clonaciondevoces

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object GetFirebase {
    val db = FirebaseFirestore.getInstance()
    val authFire = FirebaseAuth.getInstance()
    var storageFire = FirebaseStorage.getInstance()
}