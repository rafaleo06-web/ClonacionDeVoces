package dev.brianchuquiruna.clonaciondevoces

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import dev.brianchuquiruna.clonaciondevoces.GetFirebase.db
import dev.brianchuquiruna.clonaciondevoces.GetFirebase.storageFire
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentCloneVoiceBinding
import dev.brianchuquiruna.clonaciondevoces.util.AddAudioDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.File
import java.io.InputStream
import java.net.SocketTimeoutException

class CloneVoiceFragment : Fragment() {
    private lateinit var binding:FragmentCloneVoiceBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var pathVoice:String
    private lateinit var fileVoice:File
    private var starStopVoice:Boolean = true
    private lateinit var actividad:MainActivity
    val args: CloneVoiceFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCloneVoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val createRetrofit = CreateRetrofit()
        mediaPlayer = initMediaPlayer()
        actividad = activity as MainActivity
        getCurrentVoice()
        initListeners(createRetrofit)
        context?.let {
            fileVoice = File(it.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "voice.mp3")
            pathVoice = File(it.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "voice.mp3").path
        }
    }

    private fun getCurrentVoice() {
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getName().collect{
                binding.tvName.text = it
            }
        }
    }

    private fun initMediaPlayer() : MediaPlayer{
        mediaPlayer = MediaPlayer()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA) // Tipo de uso (reproducción de medios)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) // Tipo de contenido (música)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)
        return mediaPlayer
    }

    private fun initListeners(createRetrofit:ApiService) {
        binding.btnGenerateAudio.setOnClickListener {
            textToSpeech(binding.etSpeech.text.toString(),createRetrofit)
            actividad.hideKeyboard()
        }

        binding.btnPlayAudio.setOnClickListener{
            playAudio()
        }

        binding.btnSaveAudio.setOnClickListener {
            showAddAudioDialog()
        }

    }

    private fun showAddAudioDialog() {
        AddAudioDialog(
            onCancelClickListener = {},
            onSubmitClickListener = {title ,date->
                val storageRef = storageFire.reference
                val audioFileRef = storageRef.child("AUDIOS").child(System.currentTimeMillis().toString())
                binding.progressBar.isGone = false
                audioFileRef.putFile(fileVoice.toUri()).addOnSuccessListener {
                    audioFileRef.downloadUrl.addOnSuccessListener {uri ->
                        val myUri = uri.toString()
                        ///////////////////
                        db.collection(FIREBASE_PARENTS).document(args.email)
                            .collection(FIREBASE_TOPICS).document(args.idSession)
                            .collection(FIREBASE_RECORD_VOICE).document().set(
                                hashMapOf(
                                    "urlRecord" to myUri,
                                    "title" to title,
                                    "date" to date
                                )
                            )
                        binding.progressBar.isGone = true
                        ///////////////////
                        Log.i("FirebaseStorage","URI: $myUri")
                    }
                }.addOnFailureListener {
                    binding.progressBar.isGone = true
                    Log.i("FirebaseStorage","Error al subir audio a Firebase: $it")
                    Toast.makeText(actividad, "Error al subir audio a Firebase: $it", Toast.LENGTH_SHORT).show()
                }
            }
        ).show(parentFragmentManager, "addAudioDialog")
    }

    private fun playAudio() {
        if(starStopVoice){
            binding.btnPlayAudio.setImageResource(R.drawable.ic_stop_24)
            mediaPlayer.setDataSource(pathVoice)
            mediaPlayer.prepare()
            mediaPlayer.start()
            starStopVoice = false
            mediaPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                override fun onCompletion(mp: MediaPlayer?) {
                    starStopVoice = true
                    binding.btnPlayAudio.setImageResource(R.drawable.ic_play_24)
                    mediaPlayer.release()
                    mediaPlayer=MediaPlayer()
                }
            })

        }else{
            starStopVoice = true
            binding.btnPlayAudio.setImageResource(R.drawable.ic_play_24)
            mediaPlayer.stop()
            mediaPlayer.release()
            mediaPlayer=MediaPlayer()
        }
    }

    private fun textToSpeech(speechText:String,createRetrofit:ApiService){
        val context = this
        binding.progressBar.isVisible = true
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getVoiceId().collect{voiceId ->
                try{
                    val callMp3: ResponseBody = createRetrofit.textToSpeech(
                        voiceId, 0, "mp3_44100_128", API_KEY_VOICE_SERVICE,
                        TextToSpechDto(speechText,"eleven_multilingual_v2", VoiceSettingsDto(0.3, 0.63, 0.5, true))
                    )
                    Log.i("mp3", callMp3.toString())
                    getContext()?.let { pathVoice = callMp3.saveFile(it) }


                }catch (e: SocketTimeoutException){
                    Log.i("mp3","Fallo en cloncaion de voz: $e")
                }
                actividad.runOnUiThread {
                    binding.progressBar.isVisible = false
                }
            }
        }
    }
}

private fun ResponseBody.saveFile(context: Context) : String {
    val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "voice.mp3")
    byteStream().use { inputStream->
        destinationFile.outputStream().use { outputStream->
            inputStream.copyTo(outputStream)
        }
    }
    return destinationFile.path
}
