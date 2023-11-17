package dev.brianchuquiruna.clonaciondevoces.parents.loadarchives

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import dev.brianchuquiruna.clonaciondevoces.API_KEY_VOICE_SERVICE
import dev.brianchuquiruna.clonaciondevoces.CreateRetrofit
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_PARENTS
import dev.brianchuquiruna.clonaciondevoces.GetFirebase
import dev.brianchuquiruna.clonaciondevoces.GetFirebase.db
import dev.brianchuquiruna.clonaciondevoces.MainActivity
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentLoadArchivesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.google.gson.Gson
import dev.brianchuquiruna.clonaciondevoces.onlyonesession.RecordVoiceResponse
import retrofit2.Response


class LoadArchivesFragment : Fragment() {

    private lateinit var binding: FragmentLoadArchivesBinding
    private var photoUri: Uri? = null
    private var voiceUri: Uri? = null
    private lateinit var actividad: MainActivity
    private var realTimeRecord:MediaRecorder? = null
    private var pathRealtimeRecord:String=""
    val pickMedia = registerForActivityResult(PickVisualMedia()){ uri ->
        uri?.let{
            photoUri= uri
            binding.imLoadImage.setImageURI(uri)
        }
    }
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var mediaplayerRecord:MediaPlayer = MediaPlayer()
    private var firstime:Boolean = true
    private val createRetrofit = CreateRetrofit()

    private val audioPicker = registerForActivityResult(GetContent()) { uri->
        uri?.let {
            if(firstime){
                firstime=false
            }else{
                mediaPlayer.release()
            }
            binding.btnPlayAudio.isGone = false
            binding.imLoadAudio.setImageResource(R.drawable.ic_audio_48)
            mediaPlayer = MediaPlayer.create(context, it)
            voiceUri = it
            mediaPlayer.start()
            binding.btnPlayAudio.setOnClickListener{
                pauseAudio(mediaPlayer)
            }
        }
    }

    private fun getPathFromAudioUri(audioUri: Uri): String? {
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor = context?.applicationContext?.contentResolver?.query(audioUri, projection, null, null, null)
        return cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            it.moveToFirst()
            it.getString(columnIndex)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoadArchivesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actividad = activity as MainActivity
        binding.btnSendVoice.isEnabled = false
        binding.btnSendVoice.isEnabled = true
        initListeners()
    }

    private fun initListeners() {
        with(binding){
            imLoadImage.setOnClickListener() {
                pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }
            imLoadAudio.setOnClickListener() {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "audio/*"
                audioPicker.launch(intent.toString())
            }

            btnSendPhoto.setOnClickListener {
                photoUri?.let {photoUri ->
                    val storageRef = GetFirebase.storageFire.reference
                    val audioFileRef = storageRef.child("PHOTOS").child(System.currentTimeMillis().toString())
                    binding.progressBarPhoto.isGone = false
                    audioFileRef.putFile(photoUri).addOnSuccessListener {
                        audioFileRef.downloadUrl.addOnSuccessListener {uri ->
                            val myUri = uri.toString()
                            lifecycleScope.launch(Dispatchers.IO) {
                                actividad.getParentEmail().collect(){parentEmail ->
                                    db.collection(FIREBASE_PARENTS).document(parentEmail).update("image",myUri)
                                }
                            }
                            binding.progressBarPhoto.isGone = true
                            Log.i("FirebaseStorage","URI: $myUri")
                        }
                    }.addOnFailureListener {
                        Log.i("FirebaseStorage","Error al subir foto de perfil del padre a Firebase: $it")
                    }
                }
            }

            btnSendVoiceFromRecord.setOnClickListener {
                if (pathRealtimeRecord.isNotEmpty()) uploadAudioToCloneVoiceApi(pathRealtimeRecord.toUri(),false)
            }

            btnSendVoice.setOnClickListener {
                checkReadAudioPermissions()
            }

            btnRecordRealTimeRecordAudio.setOnClickListener{
                checkRecordAudioPermissions()
            }

            btnPlayRealTimeRecordAudio.setOnClickListener {
                playRealTimeRecord()
            }
        }
    }

    private fun checkRecordAudioPermissions(){
        context?.let {
            val versionApiAndroid = if (Build.VERSION.SDK_INT < 33) Manifest.permission.WRITE_EXTERNAL_STORAGE else Manifest.permission.READ_MEDIA_AUDIO
            if(ContextCompat.checkSelfPermission(it, versionApiAndroid) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(it, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                //permiso no aceptado por el momento
                requestReadAudioPermission(versionApiAndroid)
            }else{
                recordAudio()
            }
        }
    }

    private fun recordAudio() {
        if(realTimeRecord==null){
            pathRealtimeRecord = Environment.getExternalStorageDirectory().absolutePath+"/audioRecord.mp3"
            realTimeRecord = MediaRecorder()
            realTimeRecord!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            realTimeRecord!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            realTimeRecord!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            realTimeRecord!!.setOutputFile(pathRealtimeRecord)

            try{
                realTimeRecord!!.prepare()
                realTimeRecord!!.start()
            }catch (e:IOException){
                Log.i("recordVoice", "Error al iniciar grabacion de audio: $e")
            }

            binding.btnRecordRealTimeRecordAudio.setColorFilter(Color.GREEN)
        }else{
            realTimeRecord!!.stop()
            realTimeRecord!!.release()
            realTimeRecord=null
            binding.btnRecordRealTimeRecordAudio.setColorFilter(Color.BLACK)
        }
    }

    private fun playRealTimeRecord(){
        if (pathRealtimeRecord.isNotEmpty()){
            try {
                if (mediaplayerRecord.isPlaying){
                    mediaplayerRecord.stop()
                    mediaplayerRecord.reset()
                    binding.btnPlayRealTimeRecordAudio.setImageResource(R.drawable.ic_play_24)
                }else{
                    binding.btnPlayRealTimeRecordAudio.setImageResource(R.drawable.ic_stop_24)
                    mediaplayerRecord.setDataSource(pathRealtimeRecord)
                    mediaplayerRecord.prepare()
                    mediaplayerRecord.start()

                    mediaplayerRecord.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                        override fun onCompletion(mp: MediaPlayer?) {
                            mediaplayerRecord.reset()
                            binding.btnPlayRealTimeRecordAudio.setImageResource(R.drawable.ic_play_24)
                        }
                    })
                }
            }catch (e:IOException){
                Log.i("recordVoice", "Error al iniciar reproducir audio: $e")
            }
        }else{
            Toast.makeText(actividad, "Necesitas grabar un audio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkReadAudioPermissions(){
        context?.let {

            val versionApiAndroid = if (Build.VERSION.SDK_INT < 33) Manifest.permission.WRITE_EXTERNAL_STORAGE else Manifest.permission.READ_MEDIA_AUDIO

            if(ContextCompat.checkSelfPermission(it, versionApiAndroid) != PackageManager.PERMISSION_GRANTED){
                //permiso no aceptado por el momento
                requestReadAudioPermission(versionApiAndroid)
            }else{
                uploadAudioToCloneVoiceApi(voiceUri,true)
            }
        }
    }

    private suspend fun deleteVoice(voiceId:String){
        val response = createRetrofit.deleteVoice(API_KEY_VOICE_SERVICE, voiceId)
        Log.i("deleteVoice","Borrar voz status: ${response.body()?.status.orEmpty()}")
    }

    private fun uploadAudioToCloneVoiceApi(sendVoiceUri: Uri?, fileOrMic:Boolean) {
        binding.progressBarVoice.isGone = false
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getParentEmail().collect() { parentEmail ->
                db.collection(FIREBASE_PARENTS).document(parentEmail).get()
                    .addOnSuccessListener { parent ->
                        if (parent.exists()) {
                            parent.getString("name")?.let { name ->
                                sendVoiceUri?.let { requestUri ->
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        try {
                                            parent.getString("voiceId")?.let { voiceId -> deleteVoice(voiceId) }
                                            val voiceFile = if (fileOrMic) File(getPathFromAudioUri(requestUri)) else File(requestUri.path)
                                            val reqFile: RequestBody = voiceFile.asRequestBody("audio/*".toMediaTypeOrNull())
                                            val body = MultipartBody.Part.createFormData("files", voiceFile.name, reqFile)
                                            val response = createRetrofit.addVoice(API_KEY_VOICE_SERVICE, name, body, "una descripcion breve")
                                            if(response.code() == 200){
                                                db.collection(FIREBASE_PARENTS).document(parentEmail).update("voiceId",response.body()?.voice_id)
                                                actividad.runOnUiThread {
                                                    Toast.makeText(actividad, "Voz clonada con exito", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.i("addVoice", "error al agregar/eliminar voz : $e")
                                        }
                                        actividad.runOnUiThread {
                                            binding.progressBarVoice.isGone = true
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun showErrorResponse(response:Response<VoiceIdResponse>){
        Log.i("addVoice", "Error body: ${response.errorBody().toString()}")
        Log.i("addVoice", "Response code: ${response.code()}")
        Log.i("addVoice", "Response message: ${response.message()}")
        Log.i("addVoice", "response : ${response.body()}")
    }

    private fun requestReadAudioPermission(versionApiAndroid:String) {
        if(ActivityCompat.shouldShowRequestPermissionRationale(actividad,versionApiAndroid) &&
            ActivityCompat.shouldShowRequestPermissionRationale(actividad,Manifest.permission.RECORD_AUDIO)){
            //El usuario ya ha rechazado los permisos
            Toast.makeText(actividad, "Permisos rechazados", Toast.LENGTH_SHORT).show()
        }else{
            //pedir permisos
            ActivityCompat.requestPermissions(actividad, arrayOf(versionApiAndroid,Manifest.permission.RECORD_AUDIO), 777)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 777){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                uploadAudioToCloneVoiceApi(voiceUri,true)
            }else{
                Toast.makeText(actividad, "Permisos rechazados por primera vez", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pauseAudio(mediaPlayer:MediaPlayer) {
        mediaPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                binding.btnPlayAudio.isGone = true
            }
        })
        binding.btnPlayAudio.isGone = true
        mediaPlayer.stop()
    }


}